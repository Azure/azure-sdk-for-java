package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.core.exception.AzureException;

public class AzureUnsupportedDatabaseTypeException extends AzureException {

    public AzureUnsupportedDatabaseTypeException(String message) {
        super(message);
    }
}
