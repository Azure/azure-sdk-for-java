// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Event Hubs support.
 *
 * @since 4.0.0
 */
@ConditionalOnClass(EventHubClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs", name = { "connection-string", "namespace" })
@Import({
    AzureEventHubsClientBuilderConfiguration.class,
    AzureEventHubsConsumerClientConfiguration.class,
    AzureEventHubsProducerClientConfiguration.class,
    AzureBlobCheckpointStoreConfiguration.class,
    AzureEventHubsProcessorClientConfiguration.class
})
@EnableConfigurationProperties
public class AzureEventHubsAutoConfiguration extends AzureServiceConfigurationBase {

    AzureEventHubsAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureEventHubsProperties.PREFIX)
    AzureEventHubsProperties azureEventHubsProperties() {
        return loadProperties(getAzureGlobalProperties(), new AzureEventHubsProperties());
    }

    @Bean
    @ConditionalOnMissingBean(AzureEventHubsConnectionDetails.class)
    PropertiesAzureEventHubsConnectionDetails azureEventHubsConnectionDetails(AzureEventHubsProperties properties) {
        return new PropertiesAzureEventHubsConnectionDetails(properties);
    }

    @Bean
    @ConditionalOnProperty("spring.cloud.azure.eventhubs.connection-string")
    StaticConnectionStringProvider<AzureServiceType.EventHubs> eventHubsStaticConnectionStringProvider(
        AzureEventHubsConnectionDetails connectionDetails) {
        return new StaticConnectionStringProvider<>(AzureServiceType.EVENT_HUBS,
            connectionDetails.getConnectionString());
    }

    static class PropertiesAzureEventHubsConnectionDetails implements AzureEventHubsConnectionDetails {

        private final AzureEventHubsProperties properties;

        PropertiesAzureEventHubsConnectionDetails(AzureEventHubsProperties properties) {
            this.properties = properties;
        }

        @Override
        public String getConnectionString() {
            return this.properties.getConnectionString();
        }
    }

}
