// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core.listener;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsErrorHandler;
import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.messaging.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.properties.EventHubsContainerProperties;
import com.azure.spring.messaging.listener.AbstractMessageListenerContainer;

/**
 * EventHubs message listener container using {@link EventProcessorClient} to subscribe to event hubs and consume events
 * from all the partitions of each event hub.
 *
 * <p>
 * For different combinations of event hubs instance and consumer group, different {@link EventProcessorClient}s will be
 * created to subscribe to it.
 * </p>
 * <p>
 * Implementation of {@link AbstractMessageListenerContainer} is required when using {@link
 * EventProcessorClient} to consume events.
 *
 * @see AbstractMessageListenerContainer
 */
public class EventHubsMessageListenerContainer extends AbstractMessageListenerContainer {

    private final EventHubsProcessorFactory processorFactory;
    private final EventHubsContainerProperties containerProperties;
    private EventHubsErrorHandler errorHandler;
    private EventProcessorClient delegate;

    /**
     * Create an instance using the supplied processor factory and container properties.
     *
     * @param processorFactory the processor factory.
     * @param containerProperties the container properties
     */
    public EventHubsMessageListenerContainer(EventHubsProcessorFactory processorFactory,
                                             EventHubsContainerProperties containerProperties) {
        this.processorFactory = processorFactory;
        this.containerProperties = containerProperties == null ? new EventHubsContainerProperties() : containerProperties;
    }

    @Override
    protected void doStart() {
        String eventHubName = this.containerProperties.getEventHubName();
        String consumerGroup = this.containerProperties.getConsumerGroup();
        if (this.errorHandler != null) {
            this.containerProperties.setErrorHandler(this.errorHandler);
        }
        this.delegate = this.processorFactory.createProcessor(eventHubName, consumerGroup, this.containerProperties);

        this.delegate.start();
    }

    @Override
    protected void doStop() {
        if (this.delegate != null) {
            this.delegate.stop();
        }
    }

    @Override
    public void setupMessageListener(MessageListener<?> messageListener) {
        this.containerProperties.setMessageListener(messageListener);
    }

    @Override
    public EventHubsContainerProperties getContainerProperties() {
        return containerProperties;
    }

    /**
     * Set the error handler to call when the listener throws an exception.
     * @param errorHandler the error handler.
     */
    public void setErrorHandler(EventHubsErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
}
