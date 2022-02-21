// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.config;

import com.azure.spring.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.eventhubs.core.processor.EventHubsProcessorFactory;
import com.azure.spring.eventhubs.core.properties.EventHubsContainerProperties;
import com.azure.spring.messaging.config.AzureMessageListenerContainerFactoryAdapter;
import com.azure.spring.messaging.listener.AbstractMessageListenerContainer;
import com.azure.spring.messaging.listener.MessageListenerContainer;
import com.azure.spring.messaging.listener.MessageListenerContainerFactory;
import com.azure.spring.messaging.config.AzureListenerEndpoint;

/**
 * A {@link MessageListenerContainerFactory} implementation to build a standard {@link MessageListenerContainer}.
 */
public class EventHubsMessageListenerContainerFactory
    extends AzureMessageListenerContainerFactoryAdapter<AbstractMessageListenerContainer> {

    private final EventHubsProcessorFactory processorFactory;

    /**
     * Construct the listener container factory with the {@link EventHubsMessageListenerContainer}.
     *
     * @param processorFactory the {@link EventHubsMessageListenerContainer}.
     */
    public EventHubsMessageListenerContainerFactory(EventHubsProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    protected EventHubsMessageListenerContainer createContainerInstance(AzureListenerEndpoint endpoint) {
        EventHubsContainerProperties containerProperties = new EventHubsContainerProperties();
        containerProperties.setEventHubName(endpoint.getDestination());
        containerProperties.setConsumerGroup(endpoint.getGroup());

        return new EventHubsMessageListenerContainer(processorFactory, containerProperties);
    }

}
