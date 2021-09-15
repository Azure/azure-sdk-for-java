// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.autoconfigure.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.spring.cloud.actuate.cosmos.CosmosHealthIndicator;
import com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CosmosHealthConfiguration
 */
@Configuration
@ConditionalOnClass({ CosmosAsyncClient.class, HealthIndicator.class})
@ConditionalOnBean(CosmosAsyncClient.class)
@AutoConfigureAfter(AzureCosmosAutoConfiguration.class)
//TODO (xiada) ConditionalOnProperty
public class CosmosHealthConfiguration {

    @Bean
    @ConditionalOnEnabledHealthIndicator("azure-cosmos")
    public HealthIndicator cosmosHealthContributor(CosmosAsyncClient cosmosAsyncClient) {
        return new CosmosHealthIndicator(cosmosAsyncClient, null, null);
    }

}
