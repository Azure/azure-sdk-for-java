package com.azure.messaging.servicebus.receivesettle;

/**
 * Used to simulate a network error during calling the order service.
 */
public class NetworkFailureException extends Exception{
    private final String error;
    public NetworkFailureException(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "NetworkFailureException{" +
            "error='" + error + '\'' +
            '}';
    }
}
