package com.azure.ai.openai.implementation.websocket;

public final class SendResult {

    private final Throwable throwable;

    public SendResult() {
        this.throwable = null;
    }

    public SendResult(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getException() {
        return throwable;
    }

    public boolean isOK() {
        return throwable == null;
    }
}
