package com.charginghive.station.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    //user service name
    private static final String USER_SERVICE_URL = "lb://USER-SERVICE";

    @Bean
    @LoadBalanced
    public RestClient userRestClient() {
        return RestClient.builder()
                .baseUrl(USER_SERVICE_URL)
                .build();
    }
}
