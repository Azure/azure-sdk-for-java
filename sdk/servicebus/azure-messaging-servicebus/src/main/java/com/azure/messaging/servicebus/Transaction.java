package com.azure.messaging.servicebus;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class Transaction {
    private ByteBuffer txnId;
    //private Consumer<Boolean> onCompleteNotify;

    Transaction(ByteBuffer txnId){
        this.txnId = txnId;
    }
    /**
     * Represents the service-side transactionID
     * @return transaction ID
     */
    public ByteBuffer getTransactionId() {
        return this.txnId;
    }



    /*public Transaction onCompletionNotify(Consumer<Boolean> onCompleteNotify) {
        this.onCompleteNotify = onCompleteNotify;
        return null;
    }

    void notifyCompletion(boolean commit){
        if (onCompleteNotify != null) {
            onCompleteNotify.accept(commit);
        }
    }*/
}
