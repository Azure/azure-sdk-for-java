package com.azure.messaging.servicebus;

import java.nio.ByteBuffer;

public class TransactionContext {

    public ByteBuffer getTransactionId() {
        return null;
    }

    public void commit()  {}

    public void commitAsync() {}

    public void rollback() {}
}

