// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import reactor.core.publisher.Mono;

/**
 * A wrapper class for {@link ServiceBusSenderAsyncClient}.
 */
public class ServiceBusProducer {

    private final ServiceBusSenderAsyncClient delegate;

    /**
     * Creates a new instance of this {@link ServiceBusProducer} that sends messages to a Service Bus entity.
     *
     * @param delegate The delegate of {@link ServiceBusSenderAsyncClient}。
     */
    public ServiceBusProducer(ServiceBusSenderAsyncClient delegate) {
        this.delegate = delegate;
    }

    /**
     * Sends a message to a Service Bus queue or topic.
     *
     * @param message Message to be sent to Service Bus queue or topic.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> sendMessage(ServiceBusMessage message) {
        return delegate.sendMessage(message);
    }

    /**
     * Close the resource.
     */
    public void close() {
        this.delegate.close();
    }
}
