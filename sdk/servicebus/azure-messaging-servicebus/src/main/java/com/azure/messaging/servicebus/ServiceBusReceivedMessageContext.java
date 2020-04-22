// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import java.util.Objects;

/**
 * Represents the result of a receive message operation with context from Service Bus.
 */
public class ServiceBusReceivedMessageContext {
    private final ServiceBusReceivedMessage message;
    private final ServiceBusErrorContext errorContext;
    private final boolean hasError;

    /**
     * Creates an instance where a message was successfully received.
     *
     * @param message Message received.
     */
    ServiceBusReceivedMessageContext(ServiceBusReceivedMessage message) {
        this.message = Objects.requireNonNull(message, "'message' cannot be null.");
        this.errorContext = null;
        this.hasError = false;
    }

    /**
     * Creates an instance where an error occurred such as session-lock-lost.
     *
     * @param errorContext Context for that error.
     */
    ServiceBusReceivedMessageContext(ServiceBusErrorContext errorContext) {
        this.message = null;
        this.errorContext = Objects.requireNonNull(errorContext, "'errorContext' cannot be null.");
        this.hasError = true;
    }

    /**
     * Gets the message received from Service Bus.
     *
     * @return The message received from Service Bus or {@code null} if an exception occurred.
     */
    public ServiceBusReceivedMessage getMessage() {
        return message;
    }

    /**
     * Gets the error context associated with the receive operation.
     *
     * @return The error context for the receive operation or {@code null} if there is no error.
     */
    public ServiceBusErrorContext getErrorContext() {
        return errorContext;
    }

    /**
     * Gets whether or not an error occurred while receiving the next message.
     *
     * @return {@code true} if there was an error when receiving the next message; {@code false} otherwise.
     */
    public boolean hasError() {
        return hasError;
    }
}
