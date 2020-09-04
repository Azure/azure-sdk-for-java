// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.implementation.EntityHelper;
import org.apache.qpid.proton.message.Message;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents the result of a receive message operation with context from Service Bus.
 */
public class ServiceBusReceivedMessageContext {
    private final ServiceBusReceivedMessage message;
    private final String sessionId;
    private final Throwable error;

    static {
        // This is used by classes in different packages to get access to private and package-private methods.
        EntityHelper.setServiceBusReceivedMessageContextAccessor(
            new EntityHelper.ServiceBusReceivedMessageContextAccessor() {
                @Override
                public ServiceBusReceivedMessageContext toModel(ServiceBusReceivedMessage message, String sessionId,
                    Throwable error) {
                    System.out.println("!!!! Init ServiceBusReceivedMessageContext toModel ");
                    ServiceBusReceivedMessageContext messageContext;
                    if (message != null) {
                        messageContext = new ServiceBusReceivedMessageContext(message);
                    } else {
                        messageContext = new ServiceBusReceivedMessageContext(sessionId, error);
                    }
                    return messageContext;
                }
        });
    }

    /**
     * Creates an instance where a message was successfully received.
     *
     * @param message Message received.
     */
    ServiceBusReceivedMessageContext(ServiceBusReceivedMessage message) {
        this.message = Objects.requireNonNull(message, "'message' cannot be null.");
        this.sessionId = message.getSessionId();
        this.error = null;
    }

    /**
     * Creates an instance where an error occurred such as session-lock-lost.
     *
     * @param sessionId Session id that the error occurred in.
     * @param error AMQP exception that occurred in session.
     */
    ServiceBusReceivedMessageContext(String sessionId, Throwable error) {
        this.sessionId = Objects.requireNonNull(sessionId, "'sessionId' cannot be null.");
        this.error = Objects.requireNonNull(error, "'error' cannot be null.");
        this.message = null;
    }

    /**
     * Gets the session id of the message or that the error occurred in.
     *
     * @return The session id associated with the error or message. {@code null} if there is no session.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the throwable that occurred.
     *
     * @return The throwable that occurred or {@code null} if there was no error.
     */
    public Throwable getThrowable() {
        return error;
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
     * Gets whether or not an error occurred while receiving the next message.
     *
     * @return {@code true} if there was an error when receiving the next message; {@code false} otherwise.
     */
    public boolean hasError() {
        return error != null;
    }
}
