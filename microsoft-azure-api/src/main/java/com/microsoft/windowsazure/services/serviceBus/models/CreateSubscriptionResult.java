package com.microsoft.windowsazure.services.serviceBus.models;

import com.microsoft.windowsazure.services.serviceBus.Subscription;

public class CreateSubscriptionResult {

    private Subscription value;

    public CreateSubscriptionResult(Subscription value) {
        this.setValue(value);
    }

    public void setValue(Subscription value) {
        this.value = value;
    }

    public Subscription getValue() {
        return value;
    }

}
