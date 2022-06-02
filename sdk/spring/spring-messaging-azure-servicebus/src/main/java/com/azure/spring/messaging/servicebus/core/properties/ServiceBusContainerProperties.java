// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core.properties;

import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;

/**
 * The properties to describe a Service Bus listener container.
 */
public class ServiceBusContainerProperties extends ProcessorProperties {

    private MessageListener<?> messageListener;
    private ServiceBusErrorHandler errorHandler;

    /**
     * The container properties should have no default value.
     */
    public ServiceBusContainerProperties() {
        this.setMaxConcurrentCalls(null);
        this.setAutoComplete(null);
        this.setMaxAutoLockRenewDuration(null);
        this.setSubQueue(null);
        this.setReceiveMode(null);
    }
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

}
