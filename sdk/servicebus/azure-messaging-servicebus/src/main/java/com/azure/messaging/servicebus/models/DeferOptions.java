// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusTransactionContext;

import java.util.Map;

/**
 * Options to specify while deferring message.
 */
public final class DeferOptions extends SettlementOptions {
    private Map<String, Object> propertiesToModify;

    /**
     * Sets the message properties to modify while abandoning message.
     *
     * @param propertiesToModify Message properties to modify.
     *
     * @return {@link AbandonOptions} object.
     */
    public DeferOptions setPropertiesToModify(Map<String, Object> propertiesToModify) {
        this.propertiesToModify = propertiesToModify;
        return this;
    }

    /**
     * Gets the message properties to modify while putting put message in dead letter sub-queue.
     *
     * @return The message properties to modify while putting message in dead letter sub-queue.
     */
    public Map<String, Object> getPropertiesToModify() {
        return propertiesToModify;
    }

    /**
     * Sets the {@link ServiceBusTransactionContext} to the options.
     *
     * @param transactionContext The {@link ServiceBusTransactionContext} that will be used to defer a message.
     *
     * @return The Updated {@link DeferOptions} object.
     */
    @Override
    public DeferOptions setTransactionContext(ServiceBusTransactionContext transactionContext) {
        super.setTransactionContext(transactionContext);
        return this;
    }
}
