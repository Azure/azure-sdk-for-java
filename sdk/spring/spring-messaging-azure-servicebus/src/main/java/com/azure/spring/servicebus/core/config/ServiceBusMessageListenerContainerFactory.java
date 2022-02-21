// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.config;

import com.azure.spring.messaging.config.AzureMessageListenerContainerFactoryAdapter;
import com.azure.spring.messaging.listener.AbstractMessageListenerContainer;
import com.azure.spring.messaging.listener.MessageListenerContainerFactory;
import com.azure.spring.messaging.config.AzureListenerEndpoint;
import com.azure.spring.servicebus.core.listener.ServiceBusMessageListenerContainer;
import com.azure.spring.servicebus.core.processor.ServiceBusProcessorFactory;
import com.azure.spring.servicebus.core.properties.ServiceBusContainerProperties;

/**
 * A {@link MessageListenerContainerFactory} implementation to build a
 * standard {@link AbstractMessageListenerContainer}.
 *
 */
public class ServiceBusMessageListenerContainerFactory
        extends AzureMessageListenerContainerFactoryAdapter<AbstractMessageListenerContainer> {

    private final ServiceBusProcessorFactory processorFactory;
    /**
     * Construct the listener container factory with the {@link ServiceBusMessageListenerContainer}.
     * @param processorFactory the {@link ServiceBusMessageListenerContainer}.
     */
    public ServiceBusMessageListenerContainerFactory(ServiceBusProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    protected AbstractMessageListenerContainer createContainerInstance(AzureListenerEndpoint endpoint) {
        ServiceBusContainerProperties containerProperties = new ServiceBusContainerProperties();
        containerProperties.setEntityName(endpoint.getDestination());
        containerProperties.setSubscriptionName(endpoint.getGroup());

        return new ServiceBusMessageListenerContainer(processorFactory, containerProperties);
    }
}
