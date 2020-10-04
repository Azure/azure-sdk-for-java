// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusTransactionContext;

/**
 * Options to specify while completing message.
 */
public final class CompleteOptions extends SettlementOptions {
    protected CompleteOptions self() {
        return this;
    }

    /**
     * Sets the {@link ServiceBusTransactionContext} to the options.
     *
     * @param transactionContext The {@link ServiceBusTransactionContext} that will be used to complete a message.
     *
     * @return The Updated {@link CompleteOptions} object.
     */
    @Override
    public CompleteOptions setTransactionContext(ServiceBusTransactionContext transactionContext) {
        super.setTransactionContext(transactionContext);
        return this;
    }
}
