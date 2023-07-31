package com.azure.cosmos.implementation.batch;

/**
 * Functional interface for notifying when something happens.
 * @param <T> argument for the notify method
 */
public interface Notifiable<T> {
    void notify(T information);
}
