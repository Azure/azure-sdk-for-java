// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.core.config;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.spring.messaging.implementation.config.AbstractAzureListenerEndpoint;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpoint;
import com.azure.spring.messaging.implementation.config.AzureMessageListenerContainerFactoryAdapter;
import com.azure.spring.messaging.listener.AbstractMessageListenerContainer;
import com.azure.spring.messaging.implementation.listener.MessageListenerContainerFactory;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.listener.ServiceBusMessageListenerContainer;
import com.azure.spring.messaging.servicebus.core.properties.ServiceBusContainerProperties;
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

    private ServiceBusErrorHandler errorHandler = new LoggingErrorHandler();

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
        containerProperties.setErrorHandler(this.errorHandler);

        if (endpoint instanceof AbstractAzureListenerEndpoint) {
            String concurrency = ((AbstractAzureListenerEndpoint) endpoint).getConcurrency();
            try {
                containerProperties.setMaxConcurrentCalls(Integer.parseInt(concurrency));
            } catch (NumberFormatException e) {
                LOGGER.debug("The set concurrency {} must be an integer!", concurrency);
            }
        }

        return new ServiceBusMessageListenerContainer(processorFactory, containerProperties);
    }

    /**
     * Set the error handler.
     * @param errorHandler the error handler.
     */
    public void setErrorHandler(ServiceBusErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    static class LoggingErrorHandler implements ServiceBusErrorHandler {

        @Override
        public void accept(ServiceBusErrorContext errorContext) {
            LOGGER.error("Error occurred on entity {}. Error: {}",
                errorContext.getEntityPath(),
                errorContext.getException());
        }
    }
}
