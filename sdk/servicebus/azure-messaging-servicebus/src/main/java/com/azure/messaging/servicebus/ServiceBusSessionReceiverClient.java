// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.time.Duration;
import java.util.Objects;

/**
 * This session receiver client is used to acquire session locks from a queue or topic and create
 * {@link ServiceBusReceiverClient} instances that are tied to the locked sessions.
 * <ul>
 *     <li>
 *         Use {@link #acceptSession(String)} to acquire the lock of a session if you know the session id.
 *         <p>
 *         {@codesnippet com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#nextsession}
 *         </p>
 *    </li>
 *    <li>
 *        Use {@link #acceptNextSession()} to acquire the lock of the next availabe session
 *        without specifying the session id.
 *        <p>
 *        {@codesnippet com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId}
 *        </p>
 *    </li>
 * </ul>
 *
 */
public class ServiceBusSessionReceiverClient implements AutoCloseable {
    private final ServiceBusSessionReceiverAsyncClient sessionAsyncClient;

    ServiceBusSessionReceiverClient(ServiceBusSessionReceiverAsyncClient asyncClient) {
        this.sessionAsyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
    }

    /**
     * Acquires a session lock for the next available session and create a {@link ServiceBusReceiverClient}
     * to receive messages from the session. It will wait if no session is available.
     * <p>Accept next available session</p>
     * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#nextsession}
     * </p>
     * @return A {@link ServiceBusReceiverClient} that is tied to the available session.
     * @throws UnsupportedOperationException if the queue or topic subscription is not session-enabled.
     */
    public ServiceBusReceiverClient acceptNextSession() {
        return sessionAsyncClient.acceptNextSession()
            .map(asyncClient -> new ServiceBusReceiverClient(asyncClient))
            .block();
    }

    /**
     * Create a link for the next available session and use the link to create a {@link ServiceBusReceiverClient}
     * to receive messages from that session.
     * @param timeout the call is cancelled after this duration.
     * @return A {@link ServiceBusReceiverClient} that is tied to the available session.
     * @throws IllegalStateException if the operation times out.
     */
    public ServiceBusReceiverClient acceptNextSession(Duration timeout) {
        Objects.requireNonNull(timeout, "'timeout' can not be null.");
        return sessionAsyncClient.acceptNextSession()
            .map(asyncClient -> new ServiceBusReceiverClient(asyncClient))
            .block(timeout);
    }

    /**
     * Acquires a session lock for {@code sessionId} and create a {@link ServiceBusReceiverClient}
     * to receive messages from the session.
     * <p>
     * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId}
     * </p>
     * @param sessionId The session Id.
     * @return A {@link ServiceBusReceiverClient} that is tied to the specified session.
     * @throws NullPointerException if {@code sessionId} is null.
     * @throws IllegalArgumentException if {@code sessionId} is empty.
     * @throws UnsupportedOperationException if the queue or topic subscription is not session-enabled.
     * @throws com.azure.core.amqp.exception.AmqpException if the session has been locked by another session receiver.
     */
    public ServiceBusReceiverClient acceptSession(String sessionId) {
        return sessionAsyncClient.acceptSession(sessionId)
            .map(asyncClient -> new ServiceBusReceiverClient(asyncClient))
            .block();
    }

    /**
     * Acquires a session lock for {@code sessionId} and create a {@link ServiceBusReceiverClient}
     * to receive messages from the session.
     *
     * @param sessionId The session Id.
     * @param timeout the call is cancelled after this duration.
     *
     * @return A {@link ServiceBusReceiverClient} that is tied to the specified session.
     * @throws NullPointerException if {@code sessionId} is null.
     * @throws IllegalArgumentException if {@code sessionId} is empty.
     * @throws UnsupportedOperationException if the queue or topic subscription is not session-enabled.
     * @throws com.azure.core.amqp.exception.AmqpException if the session has been locked by another session receiver.
     * @throws IllegalStateException if the operation times out.
     */
    public ServiceBusReceiverClient acceptSession(String sessionId, Duration timeout) {
        Objects.requireNonNull(timeout, "'timeout' can not be null.");
        return sessionAsyncClient.acceptSession(sessionId)
            .map(asyncClient -> new ServiceBusReceiverClient(asyncClient))
            .block(timeout);
    }

    @Override
    public void close() {
        sessionAsyncClient.close();
    }
}
