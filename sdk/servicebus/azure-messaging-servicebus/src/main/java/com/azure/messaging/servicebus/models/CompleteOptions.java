package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusTransactionContext;

final public class CompleteOptions extends SettlementOptions {
    public CompleteOptions() {
        this(null);
    }

    public CompleteOptions(ServiceBusTransactionContext transactionContext) {
        super(transactionContext);
    }
}
