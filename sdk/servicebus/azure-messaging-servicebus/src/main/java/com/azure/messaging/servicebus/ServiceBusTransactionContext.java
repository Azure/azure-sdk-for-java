// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.nio.ByteBuffer;

/**
 * Represents transaction in service. This object just contains transaction id. Transaction management operations
 * like create transaction, rollback and commit operation needs to be done using sender/receiver ServiceBusClients.
 */
public class ServiceBusTransactionContext {
    private final ByteBuffer transactionId;

    ServiceBusTransactionContext(ByteBuffer transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Represents the service-side transactionID
     * @return transaction ID
     */
    public ByteBuffer getTransactionId() {
        return this.transactionId;
    }
}
