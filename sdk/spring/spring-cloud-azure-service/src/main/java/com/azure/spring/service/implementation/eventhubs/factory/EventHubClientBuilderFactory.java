// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.eventhubs.factory;

import com.azure.core.util.ClientOptions;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.core.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.PropertyMapper;
import com.azure.spring.service.implementation.eventhubs.properties.EventHubClientCommonProperties;
import com.azure.spring.service.implementation.eventhubs.properties.EventHubConsumerProperties;
import com.azure.spring.service.implementation.eventhubs.properties.EventHubsNamespaceProperties;

import java.util.function.BiConsumer;

/**
 * Event Hub client builder factory, it builds the {@link EventHubClientBuilder} according the configuration context and
 * blob properties.
 */
public class EventHubClientBuilderFactory extends AbstractAzureAmqpClientBuilderFactory<EventHubClientBuilder> {

    private final EventHubClientCommonProperties eventHubsProperties;

    /**
     * Create a {@link EventHubClientBuilderFactory} instance with a {@link EventHubClientCommonProperties}.
     * @param eventHubsProperties the properties common to an Event Hubs producer or consumer.
     */
    public EventHubClientBuilderFactory(EventHubClientCommonProperties eventHubsProperties) {
        this.eventHubsProperties = eventHubsProperties;
    }

    @Override
    protected BiConsumer<EventHubClientBuilder, ClientOptions> consumeClientOptions() {
        return EventHubClientBuilder::clientOptions;
    }

    @Override
    protected EventHubClientBuilder createBuilderInstance() {
        return new EventHubClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.eventHubsProperties;
    }


    // Endpoint=sb://<FQDN>/;SharedAccessKeyName=<KeyName>;SharedAccessKey=<KeyValue>

    @Override
    protected void configureService(EventHubClientBuilder builder) {
        PropertyMapper mapper = new PropertyMapper();

        mapper.from(eventHubsProperties.getCustomEndpointAddress()).to(builder::customEndpointAddress);
        /*mapper.from(eventHubsProperties.getFullyQualifiedNamespace()).to(builder::fullyQualifiedNamespace);
        mapper.from(eventHubsProperties.getEventHubName()).to(builder::eventHubName);*/
        mapper.from(eventHubsProperties.getCustomEndpointAddress()).to(builder::customEndpointAddress);

        if (this.eventHubsProperties instanceof EventHubsNamespaceProperties) {
            mapper.from(((EventHubsNamespaceProperties) this.eventHubsProperties).getSharedConnection())
                  .whenTrue()
                  .to(t -> builder.shareConnection());
        }

        if (this.eventHubsProperties instanceof EventHubConsumerProperties) {
            EventHubConsumerProperties consumerProperties = (EventHubConsumerProperties) this.eventHubsProperties;
            mapper.from(consumerProperties.getConsumerGroup()).to(builder::consumerGroup);
            mapper.from(consumerProperties.getPrefetchCount()).to(builder::prefetchCount);
        }
    }

}
