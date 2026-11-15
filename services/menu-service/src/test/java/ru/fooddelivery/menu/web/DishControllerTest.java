package ru.fooddelivery.menu.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.fooddelivery.menu.domain.Category;
import ru.fooddelivery.menu.dto.DishCreateRequest;
import ru.fooddelivery.menu.repository.DishRepository;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class DishControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DishRepository dishRepository;

    @BeforeEach
    void setUp() {
        dishRepository.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveDish() throws Exception {
        DishCreateRequest request = new DishCreateRequest(
                "Филадельфия",
                "Классический ролл с лососем",
                new BigDecimal("590.00"),
                Category.SUSHI,
                true,
                null
        );

        String responseJson = mockMvc.perform(post("/api/v1/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Филадельфия"))
                .andExpect(jsonPath("$.category").value("SUSHI"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdId = objectMapper.readTree(responseJson).get("id").asLong();

        mockMvc.perform(get("/api/v1/dishes/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(590.00));
    }

    @Test
    void shouldReturn400WhenRequestInvalid() throws Exception {
        DishCreateRequest invalidRequest = new DishCreateRequest(
                "",
                null,
                new BigDecimal("-10.00"),
                null,
                true,
                null
        );

        mockMvc.perform(post("/api/v1/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Некорректный запрос"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void shouldReturn404WhenDishNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/dishes/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Ресурс не найден"));
    }
}
