// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusTransactionContext;

/**
 * Options to specify while settling message.
 */
abstract class SettlementOptions {
    private ServiceBusTransactionContext transactionContext;

    /**
     * Gets the transaction associated with the settlement operation.
     *
     * @return The transaction context associated with the settlement operation. {@code null} if there is none.
     */
    public ServiceBusTransactionContext getTransactionContext() {
        return transactionContext;
    }

    /**
     * Sets the {@link ServiceBusTransactionContext} for the settlement operation.
     *
     * @param transactionContext The {@link ServiceBusTransactionContext} that will be used to settle a message.
     *
     * @return The updated {@link SettlementOptions} object.
     */
    public SettlementOptions setTransactionContext(ServiceBusTransactionContext transactionContext) {
        this.transactionContext = transactionContext;
        return this;
    }
}
