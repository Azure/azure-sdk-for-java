// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import reactor.core.publisher.Mono;

/**
 * Provides an API to manage AMQP transaction on the message broker. A transaction is used where two or more operations
 * in the messaging broker are part of one unit of work. The transaction must ensure that all operations belonging to a
 * given transaction either succeed or fail jointly. In general a transaction is involved with many operations on one
 * message broker entity. Sometime a transaction can span over multiple message broker entities explained as follows.
 *<p>
 * Distributed Transactions: A distributed transaction where operations spans over different message broker entities.
 * For example an application receive from entity 'A' and sends to entity 'B' and 'C' and all these operations are part
 * of one transaction.
 *
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transactions-v1.0-os.html#choice-txn-capability-distributed-transactions">Distributed Transactions</a>
 * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transactions-v1.0-os.html#section-transactions">Transactions</a>
 */
public interface AmqpTransactionCoordinator {

    /**
     * This completes the transaction on the message broker. All the operations belonging to this transaction will
     * either rollback or committed as one unit of work.
     *
     * @param transaction that needs to be completed on the message broker.
     * @param isCommit this flag indicates that the operations associated with this transaction should commit or
     * rollback.
     * @return a completable {@link Mono}.
     */
    Mono<Void> discharge(AmqpTransaction transaction, boolean isCommit);

    /**
     * This creates the transaction on the message broker. Successful completion of this API indicates that a
     * transaction identifier has successfully been created on the message broker. Once a transaction has been created,
     * it must be completed by using {@link AmqpTransactionCoordinator#discharge(AmqpTransaction, boolean)} API.
     * @return the created transaction id represented by {@link AmqpTransaction}.
     */
    Mono<AmqpTransaction> declare();
}
