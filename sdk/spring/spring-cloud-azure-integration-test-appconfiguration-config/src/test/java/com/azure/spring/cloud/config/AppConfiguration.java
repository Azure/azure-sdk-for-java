package com.azure.spring.cloud.config;

import com.azure.spring.cloud.config.aad.CustomClient;
import com.azure.spring.cloud.config.connectionstring.CustomSecretClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.azure.spring.cloud.feature.management.filters.PercentageFilter;

@Configuration
public class AppConfiguration {

    @Value("${authMethod}")
    private String authMethod;

    @Bean
    public BaseCustomClient azureCredentials(Environment environment) {
        return Constants.AZURE_ACTIVE_DIRECTORY.equals(authMethod) ? new CustomClient(environment) : new CustomSecretClient(environment);
    }

    @Bean
    public PercentageFilter percentageFilter() {
        return new PercentageFilter();
    }
}
