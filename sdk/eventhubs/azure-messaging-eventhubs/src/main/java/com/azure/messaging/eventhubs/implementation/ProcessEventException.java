package com.azure.messaging.eventhubs.implementation;

import com.azure.core.exception.AzureException;

public class ProcessEventException extends AzureException {
    public ProcessEventException(String message) {
        super(message);
    }

    public ProcessEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
