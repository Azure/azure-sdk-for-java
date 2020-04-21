package com.azure.messaging.signalr;

public final class SignalRStatus {
    private final boolean isAvailable;

    SignalRStatus(int statusCode) {
        this.isAvailable = statusCode == 200;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
