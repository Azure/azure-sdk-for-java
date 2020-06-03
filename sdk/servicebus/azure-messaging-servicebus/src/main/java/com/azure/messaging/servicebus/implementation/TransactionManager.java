// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.AmqpTransaction;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Provide utility for transaction API. It uses {@link AmqpSession} to accomplish this.
 */
public class TransactionManager {

    private final Mono<AmqpSession> session;
    private final String fullyQualifiedNamespace;
    private final String linkName;
    private final ClientLogger logger =  new ClientLogger(TransactionManager.class);

    TransactionManager(Mono<AmqpSession> session, String fullyQualifiedNamespace, String linkName) {
        this.session = Objects.requireNonNull(session, "'session' cannot be null.");
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.linkName = Objects.requireNonNull(linkName, "'linkName' cannot be null.");

    }

    /**
     * Creates the transaction in Service Bus namespace..
     *
     * @return {@link ByteBuffer} which represent transaction in service Bus.
     */
    public Mono<ByteBuffer> createTransaction() {
        return  session.flatMap(session ->
            session.createTransaction()).map(transaction -> transaction.getTransactionId());
    }

    /**
     * Commits the given transaction.
     *
     * @param transactionContext to commit.
     * @return {@link Mono} which user can subscribe.
     */
    public Mono<Void> commitTransaction(ServiceBusTransactionContext transactionContext) {
        return session.flatMap(session ->
            session.commitTransaction(new AmqpTransaction(transactionContext.getTransactionId())));
    }

    /**
     * Rollbacks the given transaction.
     *
     * @param transactionContext to rollback.
     * @return {@link Mono} which user can subscribe.
     */
    public Mono<Void> rollbackTransaction(ServiceBusTransactionContext transactionContext) {
        return session.flatMap(session ->
            session.rollbackTransaction(new AmqpTransaction(transactionContext.getTransactionId())));
    }
}
