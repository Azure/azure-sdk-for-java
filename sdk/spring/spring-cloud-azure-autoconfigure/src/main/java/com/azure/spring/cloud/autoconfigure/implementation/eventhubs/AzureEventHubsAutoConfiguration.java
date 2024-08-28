// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsPropertiesConfiguration;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Event Hubs support.
 *
 * @since 4.0.0
 */
@ConditionalOnClass(EventHubClientBuilder.class)
@Import({
    AzureEventHubsPropertiesConfiguration.class,
    AzureEventHubsClientBuilderConfiguration.class,
    AzureEventHubsConsumerClientConfiguration.class,
    AzureEventHubsProducerClientConfiguration.class,
    AzureBlobCheckpointStoreConfiguration.class,
    AzureEventHubsProcessorClientConfiguration.class
})
public class AzureEventHubsAutoConfiguration {

    @Bean
    @ConditionalOnProperty("spring.cloud.azure.eventhubs.connection-string")
    StaticConnectionStringProvider<AzureServiceType.EventHubs> eventHubsStaticConnectionStringProvider(
        AzureEventHubsProperties eventHubsProperties) {
        return new StaticConnectionStringProvider<>(AzureServiceType.EVENT_HUBS,
            eventHubsProperties.getConnectionString());
    }

}
