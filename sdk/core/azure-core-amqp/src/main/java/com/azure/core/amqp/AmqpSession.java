// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.AsyncCloseable;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * An AMQP session representing bidirectional communication that supports multiple {@link AmqpLink AMQP links}.
 */
public interface AmqpSession extends Disposable, AsyncCloseable {
    /**
     * Gets the name for this AMQP session.
     *
     * @return The name for the AMQP session.
     */
    String getSessionName();

    /**
     * Gets the operation timeout for starting the AMQP session.
     *
     * @return The timeout for starting the AMQP session.
     */
    Duration getOperationTimeout();

    /**
     * Creates a new AMQP link that publishes events to the message broker.
     *
     * @param linkName Name of the link.
     * @param entityPath The entity path this link connects to when producing events.
     * @param timeout Timeout required for creating and opening AMQP link.
     * @param retryPolicy The retry policy to use when sending messages.
     *
     * @return A newly created AMQP link.
     */
    Mono<AmqpLink> createProducer(String linkName, String entityPath, Duration timeout, AmqpRetryPolicy retryPolicy);

    /**
     * Creates a new AMQP link that consumes events from the message broker.
     *
     * @param linkName Name of the link.
     * @param entityPath The entity path this link connects to, so that it may read events from the message broker.
     * @param timeout Timeout required for creating and opening an AMQP link.
     * @param retryPolicy The retry policy to use when consuming messages.
     *
     * @return A newly created AMQP link.
     */
    Mono<AmqpLink> createConsumer(String linkName, String entityPath, Duration timeout, AmqpRetryPolicy retryPolicy);

    /**
     * Removes an {@link AmqpLink} with the given {@code linkName}.
     *
     * @param linkName Name of the link to remove.
     *
     * @return {@code true} if the link was removed; {@code false} otherwise.
     */
    boolean removeLink(String linkName);

    /**
     * Gets the endpoint states for the AMQP session. {@link AmqpException AmqpExceptions} that occur on the link are
     * reported in the connection state. When the stream terminates, the session is closed.
     *
     * @return A stream of endpoint states for the AMQP session.
     */
    Flux<AmqpEndpointState> getEndpointStates();

    /**
     * Creates the transaction on the message broker.
     *
     * @return A newly created AMQPTransaction.
     */
    Mono<AmqpTransaction> createTransaction();

    /**
     * Commit the transaction on the message broker.
     *
     * @param transaction to commit.
     * @return A completable mono.
     */
    Mono<Void> commitTransaction(AmqpTransaction transaction);

    /**
     * Rollback the transaction on the message broker.
     *
     * @param transaction to rollback
     * @return A completable mono.
     */
    Mono<Void> rollbackTransaction(AmqpTransaction transaction);

    /**
     * Gets an existing or newly created {@link AmqpTransactionCoordinator} on the {@link AmqpSession} which maintains
     * one instance of the {@link AmqpTransactionCoordinator} object. The {@link AmqpTransactionCoordinator} is used to
     * create/commit or rollback the transaction which can span over one or more message broker entities.
     * The interface {@link AmqpSession} provides default implementation for back-word compatibility but it throws
     * {@link RuntimeException} to warn that an implementing class must override and provide implementation of this API.
     * Azure SDK already provides implementation for this API.
     *
     * @return An existing or if it does not exists newly created {@link AmqpTransactionCoordinator}.
     * @throws UnsupportedOperationException Indicting implementation not found error. Azure SDK should provide
     * implementation of this API but if runtime is not able to find it in its classpath or version mismatch can cause
     * this exception.
     *
     * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transactions-v1.0-os.html#section-coordination">Transaction Coordination</a>
     */
    default Mono<? extends AmqpTransactionCoordinator> getOrCreateTransactionCoordinator() {
        return Mono.error(new UnsupportedOperationException("Implementation not found error."));
    }

    @Override
    default Mono<Void> closeAsync() {
        return Mono.fromRunnable(() -> dispose());

    }
}
