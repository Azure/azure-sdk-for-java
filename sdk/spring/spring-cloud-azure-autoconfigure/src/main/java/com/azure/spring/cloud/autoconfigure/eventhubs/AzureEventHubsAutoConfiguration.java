// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
    @ConditionalOnProperty("spring.cloud.azure.eventhubs.connection-string")
    StaticConnectionStringProvider<AzureServiceType.EventHubs> eventHubsStaticConnectionStringProvider(
        AzureEventHubsProperties eventHubsProperties) {
        return new StaticConnectionStringProvider<>(AzureServiceType.EVENT_HUBS,
            eventHubsProperties.getConnectionString());
    }

}
