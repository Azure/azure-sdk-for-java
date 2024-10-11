package com.azure.ai.openai.implementation.websocket;

public final class CloseReason {

    private final int closeCode;
    private final String reasonPhrase;

    public CloseReason() {
        closeCode = 1000;
        reasonPhrase = null;
    }

    public CloseReason(int closeCode, String reasonPhrase) {
        this.closeCode = closeCode;
        this.reasonPhrase = reasonPhrase;
    }

    public int getCloseCode() {
        return closeCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }
}
