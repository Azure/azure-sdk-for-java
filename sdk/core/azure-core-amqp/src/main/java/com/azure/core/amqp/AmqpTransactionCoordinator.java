// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import reactor.core.publisher.Mono;

/**
 * Provides an API to manage AMQP transaction on message broker. A transaction is used where one or more operation in
 * messaging broker is part of one unit of work. In general a transaction involve with many operations on one message
 * broker entity.
 *<p>
 * Distributed Transactions: A distributed transaction where operations spans over different message broker entities.
 *
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transactions-v1.0-os.html#choice-txn-capability-distributed-transactions">Distributed Transactions</a>
 * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transactions-v1.0-os.html#section-transactions">Transactions</a>
 */
public interface AmqpTransactionCoordinator {

    /**
     * Completes the transaction. All the work in this transaction will either rollback or committed as one unit of
     * work.
     * @param transaction that needs to be completed on message broker.
     * @param isCommit this flag indicates that the work associated with this transaction should commit or rollback.
     * @return a completable {@link Mono}.
     */
    Mono<Void> discharge(AmqpTransaction transaction, boolean isCommit);

    /**
     * Creates the transaction in message broker. Successfully completion of this API indicates that a transaction
     * identifier has successfully been created on the message broker. Once a transaction has been created, it must be
     * completed by using {@link AmqpTransactionCoordinator#discharge(AmqpTransaction, boolean)} API.
     * @return the created transaction id represented by {@link AmqpTransaction}.
     */
    Mono<AmqpTransaction> declare();
}
