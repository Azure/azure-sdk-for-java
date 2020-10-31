// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;

import java.time.Duration;
import java.util.Objects;

/**
 * This session receiver client is used to acquire session locks from a queue or topic and create
 * {@link ServiceBusReceiverClient} instances that are tied to the locked sessions.
 * Use {@link #acceptSession(String)} to acquire the lock of a session if you know the session id.
 *
 * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#nextsession}
 *
 * Use {@link #acceptNextSession()} to acquire the lock of the next available session without specifying the session id.
 *
 * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId}
 *
 */
@ServiceClient(builder = ServiceBusClientBuilder.class)
public final class ServiceBusSessionReceiverClient implements AutoCloseable {
    private final ServiceBusSessionReceiverAsyncClient sessionAsyncClient;
    private final Duration operationTimeout;

    ServiceBusSessionReceiverClient(ServiceBusSessionReceiverAsyncClient asyncClient, Duration operationTimeout) {
        this.sessionAsyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
        this.operationTimeout = operationTimeout;
    }

    /**
     * Acquires a session lock for the next available session and create a {@link ServiceBusReceiverClient}
     * to receive messages from the session. It will wait until a session is available if no one is available
     * immediately.
     *
     * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#nextsession}
     *
     * @return A {@link ServiceBusReceiverClient} that is tied to the available session.
     * @throws UnsupportedOperationException if the queue or topic subscription is not session-enabled.
     * @throws IllegalStateException if the operation times out. The timeout duration is the tryTimeout
     * of when you build this client with the
     * {@link ServiceBusClientBuilder#retryOptions(com.azure.core.amqp.AmqpRetryOptions)}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ServiceBusReceiverClient acceptNextSession() {
        return sessionAsyncClient.acceptNextSession()
            .map(asyncClient -> new ServiceBusReceiverClient(asyncClient, operationTimeout))
            .block(operationTimeout);
    }

    /**
     * Acquires a session lock for {@code sessionId} and create a {@link ServiceBusReceiverClient}
     * to receive messages from the session. If the session is already locked by another client, an
     * {@link com.azure.core.amqp.exception.AmqpException} is thrown immediately.
     *
     * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId}
     *
     * @param sessionId The session Id.
     * @return A {@link ServiceBusReceiverClient} that is tied to the specified session.
     * @throws NullPointerException if {@code sessionId} is null.
     * @throws IllegalArgumentException if {@code sessionId} is empty.
     * @throws UnsupportedOperationException if the queue or topic subscription is not session-enabled.
     * @throws com.azure.core.amqp.exception.AmqpException if the lock cannot be acquired.
     * @throws IllegalStateException if the operation times out. The timeout duration is the tryTimeout
     * of when you build this client with the
     * {@link ServiceBusClientBuilder#retryOptions(com.azure.core.amqp.AmqpRetryOptions)}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ServiceBusReceiverClient acceptSession(String sessionId) {
        return sessionAsyncClient.acceptSession(sessionId)
            .map(asyncClient -> new ServiceBusReceiverClient(asyncClient, operationTimeout))
            .block(operationTimeout);
    }

    @Override
    public void close() {
        sessionAsyncClient.close();
    }
}
