// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.processor;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.spring.service.servicebus.processor.consumer.ServiceBusErrorContextConsumer;

import java.util.function.Consumer;

/**
 * A listener to process Service Bus messages.
 */
public interface MessageProcessingListener {

    /**
     * Return the {@link Consumer} of {@link ServiceBusErrorContext} for Event Hubs by default.
     * @return the error context consumer.
     */
    default ServiceBusErrorContextConsumer getErrorContextConsumer() {
        return errorContext -> { };
    }

}
