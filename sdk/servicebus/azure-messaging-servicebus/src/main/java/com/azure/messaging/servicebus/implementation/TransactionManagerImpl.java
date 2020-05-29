// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.AmqpTransaction;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Implements {@link TransactionManager} which provide utility for transaction API.
 */
public class TransactionManagerImpl implements TransactionManager {

    private final Mono<AmqpSession> session;
    private final String fullyQualifiedNamespace;
    private final String linkName;
    private final ClientLogger logger =  new ClientLogger(TransactionManagerImpl.class);

    TransactionManagerImpl(Mono<AmqpSession> session, String fullyQualifiedNamespace, String linkName) {
        this.session = Objects.requireNonNull(session, "'session' cannot be null.");
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.linkName = Objects.requireNonNull(linkName, "'linkName' cannot be null.");

    }

    @Override
    public Mono<ByteBuffer> createTransaction() {
        return  session.flatMap(session ->
            session.createTransaction()).map(transaction -> transaction.getTransactionId());
    }

    @Override
    public Mono<Void> commitTransaction(ServiceBusTransactionContext transactionContext) {
        return session.flatMap(session ->
            session.commitTransaction(new AmqpTransaction(transactionContext.getTransactionId()))).then();
    }

    @Override
    public Mono<Void> rollbackTransaction(ServiceBusTransactionContext transactionContext) {
        return session.flatMap(session ->
            session.rollbackTransaction(new AmqpTransaction(transactionContext.getTransactionId()))).then();
    }

    private AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(fullyQualifiedNamespace, linkName);
    }
}
