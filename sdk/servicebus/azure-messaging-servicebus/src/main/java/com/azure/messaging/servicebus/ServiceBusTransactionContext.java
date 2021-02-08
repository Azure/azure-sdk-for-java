// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.annotation.Immutable;

import java.nio.ByteBuffer;

/**
 * Represents transaction in service. This object just contains transaction id. Transaction management operations
 * like create transaction, rollback, and commit operation need to be done using the sender or receiver clients.
 * <p>
 * A transaction times out after 2 minutes. The transaction timer starts when the first operation in the transaction
 * starts.
 * </p>
 *
 * <p><strong>Creating and using a transaction</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext}
 *
 * @see ServiceBusReceiverClient#createTransaction()
 * @see ServiceBusReceiverAsyncClient#createTransaction()
 * @see ServiceBusSenderClient#createTransaction()
 * @see ServiceBusSenderAsyncClient#createTransaction()
 * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-transactions">Transaction
 *      Overview</a>
 */
@Immutable
public final class ServiceBusTransactionContext {
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
