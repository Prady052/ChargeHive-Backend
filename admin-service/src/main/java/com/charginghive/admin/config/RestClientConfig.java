package com.charginghive.admin.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    // Using dummy ports as requested.
    //    private static final String USER_SERVICE_URL = "http://localhost:8085";
//    private static final String STATION_SERVICE_URL = "http://localhost:8086";

    @Bean
    @LoadBalanced // This enables service discovery and load balancing
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

//    @Bean("userRestClient")
//    public RestClient userRestClient() {
//        return RestClient.builder()
//                .baseUrl(USER_SERVICE_URL)
//                .build();
//    }
//
//    @Bean("stationRestClient")
//    public RestClient stationRestClient() {
//        return RestClient.builder()
//                .baseUrl(STATION_SERVICE_URL)
//                .build();
//    }
}
