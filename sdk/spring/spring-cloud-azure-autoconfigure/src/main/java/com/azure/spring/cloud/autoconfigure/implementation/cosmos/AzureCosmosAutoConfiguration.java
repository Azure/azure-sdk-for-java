// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.cosmos.CosmosClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Cosmos DB support.
 *
 * @since 4.0.0
 */
@ConditionalOnClass(CosmosClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.cosmos.enabled", havingValue = "true", matchIfMissing = true)
@Conditional(AzureCosmosAutoConfiguration.AzureCosmosCondition.class)
@EnableConfigurationProperties
public class AzureCosmosAutoConfiguration extends AzureServiceConfigurationBase {

    AzureCosmosAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureCosmosProperties.PREFIX)
    AzureCosmosProperties azureCosmosProperties() {
        return loadProperties(getAzureGlobalProperties(), new AzureCosmosProperties());
    }

    @Bean
    @ConditionalOnMissingBean(AzureCosmosConnectionDetails.class)
    PropertiesAzureCosmosConnectionDetails azureCosmosConnectionDetails(AzureCosmosProperties properties) {
        return new PropertiesAzureCosmosConnectionDetails(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    CosmosClient azureCosmosClient(CosmosClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    // TODO (xiada): spring data cosmos also defines a CosmosAsyncClient
    CosmosAsyncClient azureCosmosAsyncClient(CosmosClientBuilder builder) {
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
        AzureCosmosConnectionDetails connectionDetails,
        ObjectProvider<AzureServiceClientBuilderCustomizer<CosmosClientBuilder>> customizers) {
        if (!(connectionDetails instanceof PropertiesAzureCosmosConnectionDetails)) {
            properties.setEndpoint(connectionDetails.getEndpoint());
            properties.setDatabase(connectionDetails.getDatabase());
            properties.setKey(connectionDetails.getKey());
            properties.setConnectionMode(connectionDetails.getConnectionMode());
            properties.setEndpointDiscoveryEnabled(connectionDetails.getEndpointDiscoveryEnabled());
        }
        CosmosClientBuilderFactory factory = new CosmosClientBuilderFactory(properties);
        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_COSMOS);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

    static class AzureCosmosCondition extends AnyNestedCondition {

        AzureCosmosCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(prefix = "spring.cloud.azure.cosmos", name = "endpoint")
        static class PropertiesCondition {

        }

        @ConditionalOnBean(AzureCosmosConnectionDetails.class)
        static class ConnectionDetailsBeanCondition {

        }
    }

    static class PropertiesAzureCosmosConnectionDetails implements AzureCosmosConnectionDetails {

        private final AzureCosmosProperties properties;

        PropertiesAzureCosmosConnectionDetails(AzureCosmosProperties properties) {
            this.properties = properties;
        }

        @Override
        public String getEndpoint() {
            return this.properties.getEndpoint();
        }

        @Override
        public String getKey() {
            return this.properties.getKey();
        }

        @Override
        public String getDatabase() {
            return this.properties.getDatabase();
        }

        @Override
        public Boolean getEndpointDiscoveryEnabled() {
            return this.properties.getEndpointDiscoveryEnabled();
        }

        @Override
        public ConnectionMode getConnectionMode() {
            return this.properties.getConnectionMode();
        }
    }

}
