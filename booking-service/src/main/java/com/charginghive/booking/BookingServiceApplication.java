package com.charginghive.booking;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class BookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }

    @Bean
    ModelMapper modelMapper() {
        System.out.println("creating model mapper");
        ModelMapper mapper= new ModelMapper();
        //to transfer only properties matching by name
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                //transfer not null props
                .setPropertyCondition(Conditions.isNotNull());
        return mapper;
    }

}
