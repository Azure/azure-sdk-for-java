package com.microsoft.windowsazure.services.serviceBus.models;

import com.microsoft.windowsazure.services.serviceBus.Message;

public class ReceiveQueueMessageResult {

    private Message value;

    public ReceiveQueueMessageResult(Message value) {
        this.setValue(value);
    }

    public void setValue(Message value) {
        this.value = value;
    }

    public Message getValue() {
        return value;
    }

}
