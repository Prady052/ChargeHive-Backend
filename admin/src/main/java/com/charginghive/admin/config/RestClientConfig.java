package com.charginghive.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    // Using dummy ports as requested. These should be externalized in application.properties.
    private static final String USER_SERVICE_URL = "http://localhost:8085";
    private static final String STATION_SERVICE_URL = "http://localhost:8086";

    @Bean("userRestClient")
    public RestClient userRestClient() {
        return RestClient.builder()
                .baseUrl(USER_SERVICE_URL)
                .build();
    }

    @Bean("stationRestClient")
    public RestClient stationRestClient() {
        return RestClient.builder()
                .baseUrl(STATION_SERVICE_URL)
                .build();
    }
}
