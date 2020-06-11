// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.annotation.Immutable;

import java.nio.ByteBuffer;

/**
 * Represents transaction in service. This object just contains transaction id. Transaction management operations
 * like create transaction, rollback and commit operation needs to be done using sender/receiver ServiceBusClients.
 * A transaction times out after 2 minutes. The transaction timer starts when the first operation in the transaction
 * starts.
 *
 * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-transactions#transactions-in-service-bus">
 *     Transaction Overciew</a>
 *
 * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-transactions#timeout">
 *     Transaction Timeout</a>
 *
 */
@Immutable
public class ServiceBusTransactionContext {
    private final ByteBuffer transactionId;

    ServiceBusTransactionContext(ByteBuffer transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Gets the transaction id.
     *
     * @return transaction ID
     */
    public ByteBuffer getTransactionId() {
        return this.transactionId;
    }
}
