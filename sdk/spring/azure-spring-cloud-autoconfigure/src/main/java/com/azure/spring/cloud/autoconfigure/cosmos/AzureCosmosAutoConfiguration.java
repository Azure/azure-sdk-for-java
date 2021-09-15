// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for a {@link CosmosClientBuilder} and cosmos clients.
 */
@ConditionalOnClass(CosmosClientBuilder.class)
@ConditionalOnExpression("${spring.cloud.azure.cosmos.enabled:true} and"
                             + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.cosmos.uri:}')")
public class AzureCosmosAutoConfiguration extends AzureServiceConfigurationBase {

    public AzureCosmosAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureCosmosProperties.PREFIX)
    public AzureCosmosProperties azureCosmosProperties() {
        return loadProperties(this.azureGlobalProperties, new AzureCosmosProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    public CosmosClient azureCosmosClient(CosmosClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    // TODO (xiada): spring data cosmos also defines a CosmosAsyncClient
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
