package ru.fooddelivery.aggregate.web;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class MenuAggregateControllerTest {

    @RegisterExtension
    static WireMockExtension menuMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @RegisterExtension
    static WireMockExtension reviewsMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("clients.menu-service.url", menuMock::baseUrl);
        registry.add("clients.reviews-service.url", reviewsMock::baseUrl);
    }

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        menuMock.resetAll();
        reviewsMock.resetAll();
    }

    @Test
    void shouldAggregateDishAndRating() {
        menuMock.stubFor(get(urlEqualTo("/api/v1/dishes/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 1,
                                  "name": "Маргарита",
                                  "description": "Классическая пицца",
                                  "price": 490.00,
                                  "category": "PIZZA",
                                  "available": true
                                }
                                """)));

        reviewsMock.stubFor(get(urlEqualTo("/api/v1/dishes/1/rating"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "dishId": 1,
                                  "averageRating": 4.85,
                                  "weightedRating": 4.61,
                                  "reviewsCount": 14
                                }
                                """)));

        webTestClient.get()
                .uri("/api/v1/menu-aggregate/dishes/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Маргарита")
                .jsonPath("$.averageRating").isEqualTo(4.85)
                .jsonPath("$.reviewsCount").isEqualTo(14);
    }

    @Test
    void shouldUseFallbackWhenReviewsServiceFails() {
        menuMock.stubFor(get(urlEqualTo("/api/v1/dishes/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 1,
                                  "name": "Маргарита",
                                  "price": 490.00,
                                  "category": "PIZZA",
                                  "available": true
                                }
                                """)));

        reviewsMock.stubFor(get(urlEqualTo("/api/v1/dishes/1/rating"))
                .willReturn(aResponse().withStatus(500)));

        webTestClient.get()
                .uri("/api/v1/menu-aggregate/dishes/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Маргарита")
                .jsonPath("$.averageRating").isEqualTo(0)
                .jsonPath("$.reviewsCount").isEqualTo(0);
    }
}
