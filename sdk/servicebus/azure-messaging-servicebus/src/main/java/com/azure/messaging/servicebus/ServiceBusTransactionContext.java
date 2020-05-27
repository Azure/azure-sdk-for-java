package com.azure.messaging.servicebus;

import java.nio.ByteBuffer;

public class ServiceBusTransactionContext {
    private final ByteBuffer transactionId;

    ServiceBusTransactionContext(ByteBuffer transactionId){
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
