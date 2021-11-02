package com.azure.spring.cloud.autoconfigure.validation;

import org.springframework.context.annotation.Bean;

/**
 */

public class AzureEndpointValidatorConfig {
    @Bean
    public static AzureEndpointValidator configurationPropertiesValidator() {
        return new AzureEndpointValidator();
    }
}
