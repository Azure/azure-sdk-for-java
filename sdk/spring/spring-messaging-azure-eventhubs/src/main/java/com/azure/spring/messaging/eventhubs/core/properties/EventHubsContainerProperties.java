// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core.properties;

import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsErrorHandler;
import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointConfig;

import java.util.function.Consumer;

/**
 * The properties to describe an Event Hubs listener container.
 */
public class EventHubsContainerProperties extends ProcessorProperties {

    /**
     * Creates an instance of {@link EventHubsContainerProperties}.
     */
    public EventHubsContainerProperties() {
    }

    private MessageListener<?> messageListener;

    private EventHubsErrorHandler errorHandler;

    private Consumer<InitializationContext> initializationContextConsumer;

    private Consumer<CloseContext> closeContextConsumer;

    private CheckpointConfig checkpointConfig = new CheckpointConfig();

    /**
     * Get the message listener of the container.
     * @return the message listener of the container.
     */
    public MessageListener<?> getMessageListener() {
        return messageListener;
    }

    /**
     * Set the message listener for the container.
     * @param messageListener the message listener.
     */
    public void setMessageListener(MessageListener<?> messageListener) {
        this.messageListener = messageListener;
    }

    /**
     * Get the error handler of the container.
     * @return the error handler of the container.
     */
    public EventHubsErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Set the error handler for the container.
     * @param errorHandler the error handler.
     */
    public void setErrorHandler(EventHubsErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Get the consumer to consume initialization context.
     * @return the consumer to consume initialization context.
     */
    public Consumer<InitializationContext> getInitializationContextConsumer() {
        return initializationContextConsumer;
    }

    /**
     * Set the consumer to consume initialization context.
     * @param initializationContextConsumer the consumer to consume initialization context.
     */
    public void setInitializationContextConsumer(Consumer<InitializationContext> initializationContextConsumer) {
        this.initializationContextConsumer = initializationContextConsumer;
    }

    /**
     * Get the consumer to consume close context.
     * @return the consumer to consume close context.
     */
    public Consumer<CloseContext> getCloseContextConsumer() {
        return closeContextConsumer;
    }

    /**
     * Set the consumer to consume close context.
     * @param closeContextConsumer the consumer to consume close context.
     */
    public void setCloseContextConsumer(Consumer<CloseContext> closeContextConsumer) {
        this.closeContextConsumer = closeContextConsumer;
    }

    /**
     * Get the checkpoint config.
     * @return the checkpoint config.
     */
    public CheckpointConfig getCheckpointConfig() {
        return checkpointConfig;
    }

    /**
     * Set the checkpoint config.
     * @param checkpointConfig the checkpoint config.
     */
    public void setCheckpointConfig(CheckpointConfig checkpointConfig) {
        this.checkpointConfig = checkpointConfig;
    }
}
