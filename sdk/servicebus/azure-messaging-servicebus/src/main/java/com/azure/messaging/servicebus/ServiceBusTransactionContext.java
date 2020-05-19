package com.azure.messaging.servicebus;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class ServiceBusTransactionContext {
    private ByteBuffer txnId;

    ServiceBusTransactionContext(ByteBuffer txnId){
        this.txnId = txnId;
    }
    /**
     * Represents the service-side transactionID
     * @return transaction ID
     */
    public ByteBuffer getTransactionId() {
        return this.txnId;
    }
}
