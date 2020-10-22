// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.time.Duration;
import java.util.Objects;

/**
 * The builder that creates session-related {@link ServiceBusReceiverClient} instances.
 * Use {@link #acceptSession(String)} to create a {@link ServiceBusReceiverClient} that is tied to a known specific
 * session id.
 * Use {@link #acceptNextSession()} to create a {@link ServiceBusReceiverClient} that is tied to an unknown
 * available session.
 * Use {@link #getReceiverClient(int)} to create a {@link ServiceBusReceiverClient} that
 * process events from up to maxConcurrentSessions number of sessions.
 */
public class ServiceBusSessionReceiverClient implements AutoCloseable {
    private final ServiceBusSessionReceiverAsyncClient sessionAsyncClient;
    private final Duration operationTimeout;

    ServiceBusSessionReceiverClient(ServiceBusSessionReceiverAsyncClient asyncClient, Duration operationTimeout) {
        this.sessionAsyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
        this.operationTimeout = Objects.requireNonNull(operationTimeout, "'operationTimeout' cannot be null.");
    }

    /**
     * Create a link for the next available session and use the link to create a {@link ServiceBusReceiverClient}
     * to receive messages from that session.
     * @return A {@link ServiceBusReceiverClient} that is tied to the available session.
     */
    public ServiceBusReceiverClient acceptNextSession() {
        return sessionAsyncClient.acceptNextSession()
            .map(asyncClient -> new ServiceBusReceiverClient(asyncClient, operationTimeout))
            .block(operationTimeout);
    }

    /**
     * Create a link for the "sessionId" and use the link to create a {@link ServiceBusReceiverClient}
     * to receive messages from the session.
     * @param sessionId The session Id.
     * @return A {@link ServiceBusReceiverClient} that is tied to the specified session.
     * @throws IllegalArgumentException if {@code sessionId} is null or empty.
     */
    public ServiceBusReceiverClient acceptSession(String sessionId) {
        return sessionAsyncClient.acceptSession(sessionId)
            .map(asyncClient -> new ServiceBusReceiverClient(asyncClient, operationTimeout))
            .block(operationTimeout);
    }

    /**
     * Create a {@link ServiceBusReceiverClient} that processes at most {@code maxConcurrentSessions} sessions.
     *
     * @param maxConcurrentSessions Maximum number of concurrent sessions to process at any given time.
     *
     * @return The {@link ServiceBusReceiverClient} object that will be used to receive messages.
     * @throws IllegalArgumentException if {@code maxConcurrentSessions} is less than 1.
     */
    public ServiceBusReceiverClient getReceiverClient(int maxConcurrentSessions) {
        return new ServiceBusReceiverClient(sessionAsyncClient.getReceiverClient(maxConcurrentSessions),
            operationTimeout);
    }

    @Override
    public void close() {
        sessionAsyncClient.close();
    }
}
