package com.azure.messaging.servicebus.receivesettle;

/**
 * The exception that the order service throws during processing an order or orders.
 */
public class OrderServiceFailureException extends Exception {
    private final String error;
    public OrderServiceFailureException(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "OrderServiceFailureException{" +
            "error='" + error + '\'' +
            '}';
    }
}
