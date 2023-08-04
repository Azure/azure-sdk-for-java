package com.azure.spring.cloud.config.connectionstring;

import com.azure.spring.cloud.feature.management.filters.PercentageFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AppConfiguration {

    @Bean
    public CustomSecretClient azureCredentials(Environment environment) {
        return new CustomSecretClient(environment);
    }

    @Bean
    public PercentageFilter percentageFilter() {
        return new PercentageFilter();
    }
}
