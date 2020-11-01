// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;

/**
 * Additional options to configure {@link ServiceBusProcessorClient}.
 */
@Fluent
public final class ServiceBusProcessorClientOptions {

    private int maxConcurrentCalls = 1;

    /**
     * The max concurrent messages that should be processed by the processor
     * @return The max concurrent message that should be processed by the processor.
     */
    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    /**
     * Sets the max concurrent messages that can be processed by the processor.
     * @param maxConcurrentCalls The max concurrent messages that can be processed by the processor.
     * @return The updated instance of {@link ServiceBusProcessorClientOptions}.
     */
    public ServiceBusProcessorClientOptions setMaxConcurrentCalls(int maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
        return this;
    }
}
