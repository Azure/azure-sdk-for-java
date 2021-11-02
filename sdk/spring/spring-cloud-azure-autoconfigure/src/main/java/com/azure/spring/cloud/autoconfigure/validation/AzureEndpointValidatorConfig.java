package com.azure.spring.cloud.autoconfigure.validation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 */
@Configuration
public class AzureEndpointValidatorConfig {
    @Bean
    public static AzureEndpointValidator configurationPropertiesValidator() {
        return new AzureEndpointValidator();
    }
}
