// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpException;

/**
 * The error context for a receive message operation.
 *
 * @see ServiceBusReceivedMessageContext
 */
public class ServiceBusErrorContext {
    private final String sessionId;
    private final AmqpException exception;

    ServiceBusErrorContext(String sessionId, AmqpException exception) {
        this.sessionId = sessionId;
        this.exception = exception;
    }

    /**
     * Gets the session id the error occurred in.
     *
     * @return The session id associated with the error or {@code null} if there is no session.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the exception that occurred.
     *
     * @return The exception that occurred.
     */
    public AmqpException getThrowable() {
        return exception;
    }
}
