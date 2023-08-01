// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.core.config;

import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.spring.messaging.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.messaging.eventhubs.core.properties.EventHubsContainerProperties;
import com.azure.spring.messaging.implementation.config.AzureListenerEndpoint;
import com.azure.spring.messaging.implementation.config.AzureMessageListenerContainerFactoryAdapter;
import com.azure.spring.messaging.listener.MessageListenerContainer;
import com.azure.spring.messaging.implementation.listener.MessageListenerContainerFactory;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link MessageListenerContainerFactory} implementation to build a standard {@link MessageListenerContainer}.
 */
public class EventHubsMessageListenerContainerFactory
    extends AzureMessageListenerContainerFactoryAdapter<EventHubsMessageListenerContainer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsMessageListenerContainerFactory.class);

    private final EventHubsProcessorFactory processorFactory;

    private EventHubsErrorHandler errorHandler = new LoggingErrorHandler();


    /**
     * Construct the listener container factory with the {@link EventHubsProcessorFactory}.
     *
     * @param processorFactory the {@link EventHubsProcessorFactory}.
     */
    public EventHubsMessageListenerContainerFactory(EventHubsProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    protected EventHubsMessageListenerContainer createContainerInstance(AzureListenerEndpoint endpoint) {
        EventHubsContainerProperties containerProperties = new EventHubsContainerProperties();
        containerProperties.setEventHubName(endpoint.getDestination());
        containerProperties.setConsumerGroup(endpoint.getGroup());
        containerProperties.setErrorHandler(this.errorHandler);

        return new EventHubsMessageListenerContainer(processorFactory, containerProperties);
    }

    /**
     * Set the error handler.
     * @param errorHandler the error handler.
     */
    public void setErrorHandler(EventHubsErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    static class LoggingErrorHandler implements EventHubsErrorHandler {

        @Override
        public void accept(ErrorContext errorContext) {
            LOGGER.error("Error occurred on partition: {}. Error: {}",
                errorContext.getPartitionContext().getPartitionId(),
                errorContext.getThrowable());
        }
    }

}
