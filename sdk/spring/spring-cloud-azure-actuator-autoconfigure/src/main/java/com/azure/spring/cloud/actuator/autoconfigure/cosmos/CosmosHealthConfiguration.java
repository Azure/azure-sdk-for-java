// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.spring.cloud.actuator.cosmos.CosmosHealthIndicator;
import com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
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
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ CosmosAsyncClient.class, HealthIndicator.class})
@ConditionalOnBean(CosmosAsyncClient.class)
@AutoConfigureAfter(AzureCosmosAutoConfiguration.class)
@ConditionalOnEnabledHealthIndicator("azure-cosmos")
public class CosmosHealthConfiguration {

    @Bean
    HealthIndicator cosmosHealthContributor(AzureCosmosProperties azureCosmosProperties,
                                            CosmosAsyncClient cosmosAsyncClient) {
        return new CosmosHealthIndicator(cosmosAsyncClient,
                                         azureCosmosProperties.getDatabase(),
                                         azureCosmosProperties.getEndpoint());
    }

}
