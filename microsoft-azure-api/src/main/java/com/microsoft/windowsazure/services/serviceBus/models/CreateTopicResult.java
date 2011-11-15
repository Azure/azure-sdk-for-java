package com.microsoft.windowsazure.services.serviceBus.models;


public class CreateTopicResult {

    private Topic value;

    public CreateTopicResult(Topic value) {
        this.setValue(value);
    }

    public void setValue(Topic value) {
        this.value = value;
    }

    public Topic getValue() {
        return value;
    }

}
