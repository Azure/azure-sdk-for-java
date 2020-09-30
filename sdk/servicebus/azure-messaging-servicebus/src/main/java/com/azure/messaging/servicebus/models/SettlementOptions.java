package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusTransactionContext;

class SettlementOptions {
    final private ServiceBusTransactionContext transactionContext;

    SettlementOptions(ServiceBusTransactionContext transactionContext) {
        this.transactionContext = transactionContext;
    }

    public ServiceBusTransactionContext getTransactionContext(){
        return transactionContext;
    }
}
