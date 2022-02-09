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
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext -->
 * <pre>
 * &#47;&#47; This mono creates a transaction and caches the output value, so we can associate operations with the
 * &#47;&#47; transaction. It does not cache the value if it is an error or completes with no items, effectively retrying
 * &#47;&#47; the operation.
 * Mono&lt;ServiceBusTransactionContext&gt; transactionContext = receiver.createTransaction&#40;&#41;
 *     .cache&#40;value -&gt; Duration.ofMillis&#40;Long.MAX_VALUE&#41;,
 *         error -&gt; Duration.ZERO,
 *         &#40;&#41; -&gt; Duration.ZERO&#41;;
 *
 * transactionContext.flatMap&#40;transaction -&gt; &#123;
 *     &#47;&#47; Process messages and associate operations with the transaction.
 *     Mono&lt;Void&gt; operations = Mono.when&#40;
 *         receiver.receiveDeferredMessage&#40;sequenceNumber&#41;.flatMap&#40;message -&gt;
 *             receiver.complete&#40;message, new CompleteOptions&#40;&#41;.setTransactionContext&#40;transaction&#41;&#41;&#41;,
 *         receiver.abandon&#40;receivedMessage, new AbandonOptions&#40;&#41;.setTransactionContext&#40;transaction&#41;&#41;&#41;;
 *
 *     &#47;&#47; Finally, either commit or rollback the transaction once all the operations are associated with it.
 *     return operations.flatMap&#40;transactionOperations -&gt; receiver.commitTransaction&#40;transaction&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext -->
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
