package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusTransactionContext;

import java.util.Map;

final public class DeferOptions extends SettlementOptions {
    private Map<String, Object> propertiesToModify;

    public DeferOptions() {
        this(null);
    }

    public DeferOptions(ServiceBusTransactionContext transactionContext) {
        super(transactionContext);
    }

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
}
