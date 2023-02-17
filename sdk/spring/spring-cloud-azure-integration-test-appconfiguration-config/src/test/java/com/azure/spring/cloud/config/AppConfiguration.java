package com.azure.spring.cloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public CustomClient azureCredentials() {
        return new CustomClient();
    }
}