// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpLink;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * This is AMQP coordinator link which allow operations related to transactions.
 */
public interface AmqpCoordinatorLink extends AmqpLink {

    /**
     * Creates transaction in Service Bus namespace.
     *
     * @return {@link DeliveryState} after creating transaction which would have transactionId.
     */
    Mono<DeliveryState> createTransaction();

    /**
     * Commit or rollback the transaction.
     *
     * @param transactionId to commit or rollback.
     * @param isCommit commit the transaction if true otherwise rollback.
     * @return
     */
    Mono<DeliveryState> completeTransaction(ByteBuffer transactionId, boolean isCommit);

}
