// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusTransactionContext;

/**
 * Options to specify while settling message.
 */
abstract class SettlementOptions {
    private ServiceBusTransactionContext transactionContext;

    public ServiceBusTransactionContext getTransactionContext() {
        return transactionContext;
    }

    /**
     * Sets the {@link ServiceBusTransactionContext} to the options.
     *
     * @param transactionContext The {@link ServiceBusTransactionContext} that will be used to settle a message.
     *
     * @return The Updated {@link SettlementOptions} object.
     */
    public SettlementOptions setTransactionContext(ServiceBusTransactionContext transactionContext) {
        this.transactionContext = transactionContext;
        return this;
    }
}
