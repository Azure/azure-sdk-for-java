package com.azure.messaging.servicebus.implementation.models;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;

/**
 * Additional options to configure {@link ServiceBusProcessorClient}.
 */
public final class ServiceBusProcessorClientOptions {

    private int maxConcurrentCalls;

    /**
     * The max concurrent messages that can be processed by the processor
     * @return
     */
    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    /**
     * Sets the max concurrent messages that can be processed by the processor.
     * @param maxConcurrentCalls The max concurrent messages that can be processed by the processor.
     */
    public void setMaxConcurrentCalls(int maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
    }
}
