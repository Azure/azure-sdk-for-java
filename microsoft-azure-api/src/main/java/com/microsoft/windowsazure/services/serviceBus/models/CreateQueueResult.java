package com.microsoft.windowsazure.services.serviceBus.models;

import com.microsoft.windowsazure.services.serviceBus.Queue;

public class CreateQueueResult {

    private Queue value;

    public CreateQueueResult(Queue value) {
        this.setValue(value);
    }

    public void setValue(Queue value) {
        this.value = value;
    }

    public Queue getValue() {
        return value;
    }

}
