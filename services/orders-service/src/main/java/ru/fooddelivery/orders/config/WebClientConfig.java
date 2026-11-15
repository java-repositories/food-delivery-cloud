package ru.fooddelivery.orders.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient menuWebClient(
            WebClient.Builder builder,
            @Value("${clients.menu-service.url}") String menuServiceUrl
    ) {
        return builder
                .baseUrl(menuServiceUrl)
                .build();
    }
}
