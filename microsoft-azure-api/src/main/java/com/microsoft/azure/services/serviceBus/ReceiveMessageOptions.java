package com.microsoft.azure.services.serviceBus;

public class ReceiveMessageOptions {
    Integer timeout;
    private ReceiveMode receiveMode = ReceiveMode.RECEIVE_AND_DELETE;

    public static final ReceiveMessageOptions DEFAULT = new ReceiveMessageOptions();

    public Integer getTimeout() {
        return timeout;
    }

    public ReceiveMessageOptions setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public ReceiveMode getReceiveMode() {
        return receiveMode;
    }

    public ReceiveMessageOptions setReceiveMode(ReceiveMode receiveMode) {
        this.receiveMode = receiveMode;
        return this;
    }

    public boolean isReceiveAndDelete() {
        return receiveMode == ReceiveMode.RECEIVE_AND_DELETE;
    }

    public ReceiveMessageOptions setReceiveAndDelete() {
        this.receiveMode = ReceiveMode.RECEIVE_AND_DELETE;
        return this;
    }

    public boolean isPeekLock() {
        return receiveMode == ReceiveMode.PEEK_LOCK;
    }

    public ReceiveMessageOptions setPeekLock() {
        this.receiveMode = ReceiveMode.PEEK_LOCK;
        return this;
    }
}
