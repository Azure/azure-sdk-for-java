package com.azure.spring.cloud.feature.manager;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
public class TestConfiguration {

    @Bean
    public FeatureManagementConfigProperties properties() {
        return new FeatureManagementConfigProperties();
    }
}
