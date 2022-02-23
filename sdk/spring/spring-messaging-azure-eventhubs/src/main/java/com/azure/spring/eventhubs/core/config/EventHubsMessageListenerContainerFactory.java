// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.config;

import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.spring.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.eventhubs.core.processor.EventHubsProcessorFactory;
import com.azure.spring.eventhubs.core.properties.EventHubsContainerProperties;
import com.azure.spring.messaging.config.AzureListenerEndpoint;
import com.azure.spring.messaging.config.AzureMessageListenerContainerFactoryAdapter;
import com.azure.spring.messaging.listener.MessageListenerContainer;
import com.azure.spring.messaging.listener.MessageListenerContainerFactory;
import com.azure.spring.service.eventhubs.processor.consumer.EventProcessorErrorContextConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link MessageListenerContainerFactory} implementation to build a standard {@link MessageListenerContainer}.
 */
public class EventHubsMessageListenerContainerFactory
    extends AzureMessageListenerContainerFactoryAdapter<EventHubsMessageListenerContainer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsMessageListenerContainerFactory.class);

    private final EventHubsProcessorFactory processorFactory;

    private EventProcessorErrorContextConsumer errorHandler = new LoggingErrorHandler();


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
        containerProperties.setErrorContextConsumer(this.errorHandler);

        return new EventHubsMessageListenerContainer(processorFactory, containerProperties);
    }

    public void setErrorHandler(EventProcessorErrorContextConsumer errorHandler) {
        this.errorHandler = errorHandler;
    }

    static class LoggingErrorHandler implements EventProcessorErrorContextConsumer {

        @Override
        public void accept(ErrorContext errorContext) {
            LOGGER.error("Error occurred on partition: {}. Error: {}",
                errorContext.getPartitionContext().getPartitionId(),
                errorContext.getThrowable());
        }
    }

}
