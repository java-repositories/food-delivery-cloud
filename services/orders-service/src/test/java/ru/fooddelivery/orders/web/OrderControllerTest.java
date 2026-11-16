package ru.fooddelivery.orders.web;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.fooddelivery.orders.dto.OrderCreateRequest;
import ru.fooddelivery.orders.dto.OrderItemRequest;
import ru.fooddelivery.orders.dto.OrderResponse;
import ru.fooddelivery.orders.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class OrderControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("clients.menu-service.url", wireMock::baseUrl);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("spring.cloud.loadbalancer.enabled", () -> "false");
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll().block();
        wireMock.resetAll();
    }

    @Test
    void shouldCreateOrderWhenDishesAreAvailable() {
        wireMock.stubFor(get(urlEqualTo("/api/v1/dishes/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 1,
                                  "name": "Пепперони",
                                  "price": 650.00,
                                  "available": true
                                }
                                """)));

        wireMock.stubFor(get(urlEqualTo("/api/v1/dishes/2"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 2,
                                  "name": "Кола",
                                  "price": 150.00,
                                  "available": true
                                }
                                """)));

        OrderCreateRequest request = new OrderCreateRequest(
                "user-123",
                "ул. Пушкина, д. 10, кв. 5",
                List.of(
                        new OrderItemRequest(1L, 2),
                        new OrderItemRequest(2L, 1)
                )
        );

        OrderResponse created = webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        assertThat(created.totalPrice()).isEqualByComparingTo(new BigDecimal("1450.00"));
        assertThat(created.items()).hasSize(2);

        // Проверяем получение заказа по ID
        webTestClient.get()
                .uri("/api/v1/orders/{id}", created.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.customerId").isEqualTo("user-123")
                .jsonPath("$.status").isEqualTo("NEW");
    }

    @Test
    void shouldReturn400WhenDishIsUnavailable() {
        wireMock.stubFor(get(urlEqualTo("/api/v1/dishes/5"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 5,
                                  "name": "Тирамису",
                                  "price": 320.00,
                                  "available": false
                                }
                                """)));

        OrderCreateRequest request = new OrderCreateRequest(
                "user-123",
                "ул. Ленина, д. 1",
                List.of(new OrderItemRequest(5L, 1))
        );

        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Блюдо недоступно");
    }
}
