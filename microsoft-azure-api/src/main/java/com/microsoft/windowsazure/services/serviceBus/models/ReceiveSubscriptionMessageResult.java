package com.microsoft.windowsazure.services.serviceBus.models;

public class ReceiveSubscriptionMessageResult {

    private Message value;

    public ReceiveSubscriptionMessageResult(Message value) {
        this.setValue(value);
    }

    public void setValue(Message value) {
        this.value = value;
    }

    public Message getValue() {
        return value;
    }

}
