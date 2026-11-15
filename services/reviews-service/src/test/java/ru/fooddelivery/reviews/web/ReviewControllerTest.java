package ru.fooddelivery.reviews.web;

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
import ru.fooddelivery.reviews.dto.ReviewCreateRequest;
import ru.fooddelivery.reviews.repository.ReviewRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class ReviewControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
    }

    @Test
    void shouldRecalculateDishRatingViaPostgresTrigger() throws Exception {
        ReviewCreateRequest first = new ReviewCreateRequest(10L, "user-1", 5, "Очень вкусно!");
        ReviewCreateRequest second = new ReviewCreateRequest(10L, "user-2", 4, "Хорошо, но мало соуса");

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isCreated());

        // Проверяем, что триггер PostgreSQL автоматически пересчитал средний рейтинг: (5 + 4) / 2 = 4.50
        mockMvc.perform(get("/api/v1/dishes/10/rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dishId").value(10))
                .andExpect(jsonPath("$.averageRating").value(4.50))
                .andExpect(jsonPath("$.reviewsCount").value(2));
    }

    @Test
    void shouldReturn409OnDuplicateReview() throws Exception {
        ReviewCreateRequest review = new ReviewCreateRequest(10L, "user-1", 5, "Супер");

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Повторный отзыв"));
    }
}
