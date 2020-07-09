// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import org.springframework.dao.DataAccessException;

/**
 * General exception for illegal configuration of cosmosdb
 */
public class ConfigurationException extends DataAccessException {

    /**
     * Construct a {@code IllegalQueryException} with the specified detail message.
     * @param msg the detail message
     */
    public ConfigurationException(String msg) {
        super(msg);
    }

    /**
     * Construct a {@code IllegalQueryException} with the specified detail message
     * and nested exception.
     *
     * @param msg the detail message
     * @param cause the nested exception
     */
    public ConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
