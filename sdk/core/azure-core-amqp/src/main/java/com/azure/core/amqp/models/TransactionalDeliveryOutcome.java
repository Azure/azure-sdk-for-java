// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.amqp.AmqpTransaction;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * A transaction delivery outcome.
 *
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transactions-v1.0-os.html#type-transactional-state">Transactional
 *     state</a>
 */
@Fluent
public final class TransactionalDeliveryOutcome extends DeliveryOutcome {
    private DeliveryOutcome outcome;
    private final AmqpTransaction amqpTransaction;

    /**
     * Creates an outcome with the given transaction.
     *
     * @param transaction The transaction.
     * @throws NullPointerException if {@code transaction} is {@code null}.
     */
    public TransactionalDeliveryOutcome(AmqpTransaction transaction) {
        super(null);
        this.amqpTransaction = Objects.requireNonNull(transaction, "'transaction' cannot be null.");
    }

    /**
     * Gets the transaction id associated with this delivery outcome.
     *
     * @return The transaction id.
     */
    public ByteBuffer getTransactionId() {
        return amqpTransaction.getTransactionId();
    }

    /**
     * Gets the delivery state associated with this transaction outcome.
     *
     * @return the delivery state associated with this transaction, {@code null} if there is no delivery state
     *     associated with this transaction yet.
     */
    @Override
    public DeliveryState getDeliveryState() {
        return super.getDeliveryState();
    }

    /**
     * Gets the delivery outcome associated with this transaction.
     *
     * @return the delivery outcome associated with this transaction, {@code null} if there is no outcome.
     */
    public DeliveryOutcome getOutcome() {
        return outcome;
    }

    /**
     * Sets the outcome associated with this delivery state.
     *
     * @param outcome Outcome associated with this transaction delivery.
     *
     * @return The updated {@link TransactionalDeliveryOutcome} object.
     *
     * @throws IllegalArgumentException if {@code outcome} is an instance of {@link TransactionalDeliveryOutcome}.
     *     Cannot have nested transaction outcomes.
     */
    public TransactionalDeliveryOutcome setOutcome(DeliveryOutcome outcome) {
        if (outcome instanceof TransactionalDeliveryOutcome) {
            throw new ClientLogger(TransactionalDeliveryOutcome.class).logExceptionAsError(
                new IllegalArgumentException("Cannot set the outcome as another nested transaction outcome."));
        }

        this.outcome = outcome;
        if (outcome != null) {
            setDeliveryState(outcome.getDeliveryState());
        }

        return this;
    }
}
