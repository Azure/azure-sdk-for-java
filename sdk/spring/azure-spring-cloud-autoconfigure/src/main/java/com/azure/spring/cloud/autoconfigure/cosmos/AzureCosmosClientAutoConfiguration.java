// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for a {@link CosmosClientBuilder} and cosmos clients.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(CosmosClientBuilder.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure.cosmos", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureCosmosProperties.class)
public class AzureCosmosClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CosmosClient azureCosmosClient(CosmosClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public CosmosAsyncClient azureCosmosAsyncClient(CosmosClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public CosmosClientBuilder cosmosClientBuilder(CosmosClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CosmosClientBuilderFactory cosmosClientBuilderFactory(AzureCosmosProperties properties) {
        return new CosmosClientBuilderFactory(properties);
    }

}
