// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for a {@link CosmosClientBuilder} and cosmos clients.
 */
@ConditionalOnClass(CosmosClientBuilder.class)
@ConditionalOnProperty(prefix = AzureCosmosProperties.PREFIX, name = "enabled", matchIfMissing = true)
@ConditionalOnBean(AzureConfigurationProperties.class)
public class AzureCosmosAutoConfiguration extends AzureServiceConfigurationBase {

    public AzureCosmosAutoConfiguration(AzureConfigurationProperties azureProperties) {
        super(azureProperties);
    }

    @Bean
    @ConfigurationProperties(AzureCosmosProperties.PREFIX)
    public AzureCosmosProperties azureCosmosProperties() {
        return loadProperties(this.azureProperties, new AzureCosmosProperties());
    }

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
