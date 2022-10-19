// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.cosmos.CosmosClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Cosmos DB support.
 *
 * @since 4.0.0
 */
@ConditionalOnClass(CosmosClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.cosmos.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.cosmos", name = "endpoint")
public class AzureCosmosAutoConfiguration extends AzureServiceConfigurationBase {

    AzureCosmosAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureCosmosProperties.PREFIX)
    AzureCosmosProperties azureCosmosProperties() {
        return loadProperties(getAzureGlobalProperties(), new AzureCosmosProperties());
    }

    /**
     * Autoconfigure the {@link CosmosClient} instance.
     * @param builder the {@link CosmosClientBuilder} to build the instance.
     * @return the cosmos client instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public CosmosClient azureCosmosClient(CosmosClientBuilder builder) {
        return builder.buildClient();
    }

    /**
     * Autoconfigure the {@link CosmosAsyncClient} instance.
     * @param builder the {@link CosmosClientBuilder} to build the instance.
     * @return the cosmos async client instance.
     */
    @Bean
    @ConditionalOnMissingBean
    // TODO (xiada): spring data cosmos also defines a CosmosAsyncClient
    public CosmosAsyncClient azureCosmosAsyncClient(CosmosClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    CosmosClientBuilder cosmosClientBuilder(CosmosClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    CosmosClientBuilderFactory cosmosClientBuilderFactory(AzureCosmosProperties properties,
        ObjectProvider<AzureServiceClientBuilderCustomizer<CosmosClientBuilder>> customizers) {
        CosmosClientBuilderFactory factory = new CosmosClientBuilderFactory(properties);
        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_COSMOS);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

}
