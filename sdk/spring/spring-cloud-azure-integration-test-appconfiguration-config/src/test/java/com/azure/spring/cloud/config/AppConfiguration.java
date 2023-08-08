package com.azure.spring.cloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.azure.spring.cloud.feature.management.filters.PercentageFilter;

@Configuration
public class AppConfiguration {

    @Bean
    public CustomClient azureCredentials(Environment environment) {
        return new CustomClient(environment);
    }
    
    @Bean
    public PercentageFilter percentageFilter() {
        return new PercentageFilter();
    }
}