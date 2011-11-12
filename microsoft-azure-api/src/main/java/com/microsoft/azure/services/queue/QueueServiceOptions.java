package com.microsoft.azure.services.queue;

public class QueueServiceOptions {
    // Nullable because it is optional
    private Integer timeout;

    public Integer getTimeout() {
        return timeout;
    }

    public QueueServiceOptions setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }
}
