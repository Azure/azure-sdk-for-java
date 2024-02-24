package com.azure.cosmos.kafka.connect.implementation.sink;

import org.apache.kafka.connect.errors.ConnectException;

public class CosmosDBWriteException extends ConnectException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CosmosDBWriteException(String message) {
        super(message);
    }
}
