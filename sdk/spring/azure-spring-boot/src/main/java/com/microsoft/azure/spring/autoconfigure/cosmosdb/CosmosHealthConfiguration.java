// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.cosmosdb;

import com.azure.data.cosmos.CosmosClient;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * CosmosHealthConfiguration
 */
@Configuration
@ConditionalOnClass({CosmosClient.class, HealthIndicator.class})
@PropertySource("classpath:/azure-spring-actuator.properties")
@AutoConfigureAfter(CosmosAutoConfiguration.class)
public class CosmosHealthConfiguration {

    @Bean
    @ConditionalOnEnabledHealthIndicator("azure-cosmos")
    public HealthIndicator cosmosHealthContributor(CosmosClient cosmosClient) {
        return new CosmosHealthIndicator(cosmosClient);
    }

}
