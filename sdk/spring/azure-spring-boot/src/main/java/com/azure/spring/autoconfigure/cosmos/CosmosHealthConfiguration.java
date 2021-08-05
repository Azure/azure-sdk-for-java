// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
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
@ConditionalOnClass({ CosmosAsyncClient.class, HealthIndicator.class})
@AutoConfigureAfter(CosmosAutoConfiguration.class)
public class CosmosHealthConfiguration {

    @Bean
    @ConditionalOnEnabledHealthIndicator("azure-cosmos")
    public HealthIndicator cosmosHealthContributor(CosmosAsyncClient cosmosAsyncClient) {
        return new CosmosHealthIndicator(cosmosAsyncClient);
    }

}
