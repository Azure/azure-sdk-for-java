// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core.properties;

import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;

/**
 * The properties to describe a Service Bus listener container.
 */
public class ServiceBusContainerProperties extends ProcessorProperties {

    private MessageListener<?> messageListener;
    private ServiceBusErrorHandler errorHandler;
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
    public ServiceBusErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Set the error handler for the container.
     * @param errorHandler the error handler.
     */
    public void setErrorHandler(ServiceBusErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
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
