package com.charginghive.station.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    //user service name
//    private static final String USER_SERVICE_URL = "http://USER-SERVICE";
    //eureka not working not resolve AUTH-SERVICE
//    private static final String USER_SERVICE_URL = "http://localhost:8085";
    @Bean
    @LoadBalanced // This enables service discovery and load balancing
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    //This bean does NOT work with service discovery
// Even though it's annotated with @LoadBalanced, it won't work correctly
// because Spring does NOT enhance RestClient instances, only RestClient.Builder beans
//
// What’s wrong here:
// - You build the RestClient immediately (via builder.build())
// - Spring never gets the chance to inject the load balancer
// - So when you call http://AUTH-SERVICE, it tries to resolve it as a real DNS host (which fails)
//    @Bean
//    @LoadBalanced
//    public RestClient userRestClient() {
//        return RestClient.builder()
//                .baseUrl(USER_SERVICE_URL)
//                .build();
//    }
}
