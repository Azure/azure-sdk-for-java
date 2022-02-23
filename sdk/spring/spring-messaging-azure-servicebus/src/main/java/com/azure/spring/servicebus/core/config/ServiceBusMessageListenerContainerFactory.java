// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.config;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.spring.messaging.config.AzureListenerEndpoint;
import com.azure.spring.messaging.config.AzureMessageListenerContainerFactoryAdapter;
import com.azure.spring.messaging.listener.AbstractMessageListenerContainer;
import com.azure.spring.messaging.listener.MessageListenerContainerFactory;
import com.azure.spring.service.servicebus.processor.consumer.ServiceBusProcessorErrorContextConsumer;
import com.azure.spring.servicebus.core.listener.ServiceBusMessageListenerContainer;
import com.azure.spring.servicebus.core.processor.ServiceBusProcessorFactory;
import com.azure.spring.servicebus.core.properties.ServiceBusContainerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link MessageListenerContainerFactory} implementation to build a standard {@link
 * AbstractMessageListenerContainer}.
 */
public class ServiceBusMessageListenerContainerFactory
    extends AzureMessageListenerContainerFactoryAdapter<ServiceBusMessageListenerContainer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusMessageListenerContainerFactory.class);

    private final ServiceBusProcessorFactory processorFactory;

    private ServiceBusProcessorErrorContextConsumer errorHandler = new LoggingErrorHandler();

    /**
     * Construct the listener container factory with the {@link ServiceBusProcessorFactory}.
     *
     * @param processorFactory the {@link ServiceBusProcessorFactory}.
     */
    public ServiceBusMessageListenerContainerFactory(ServiceBusProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    protected ServiceBusMessageListenerContainer createContainerInstance(AzureListenerEndpoint endpoint) {
        ServiceBusContainerProperties containerProperties = new ServiceBusContainerProperties();
        containerProperties.setEntityName(endpoint.getDestination());
        containerProperties.setSubscriptionName(endpoint.getGroup());
        containerProperties.setErrorContextConsumer(this.errorHandler);

        return new ServiceBusMessageListenerContainer(processorFactory, containerProperties);
    }

    static class LoggingErrorHandler implements ServiceBusProcessorErrorContextConsumer {

        @Override
        public void accept(ServiceBusErrorContext errorContext) {
            LOGGER.error("Error occurred on entity {}. Error: {}",
                errorContext.getEntityPath(),
                errorContext.getException());
        }
    }
}
