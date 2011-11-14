package com.microsoft.windowsazure.services.serviceBus.models;


public class GetSubscriptionResult {

    private Subscription value;

    public GetSubscriptionResult(Subscription value) {
        this.setValue(value);
    }

    public void setValue(Subscription value) {
        this.value = value;
    }

    public Subscription getValue() {
        return value;
    }

}
