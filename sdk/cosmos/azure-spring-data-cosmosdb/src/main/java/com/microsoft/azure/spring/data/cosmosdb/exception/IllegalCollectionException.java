// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.lang.Nullable;

/**
 * General exception for illegal collection of cosmosdb
 */
public class IllegalCollectionException extends DataAccessException {

    /**
     * Construct a {@code IllegalQueryException} with the specified detail message.
     * @param msg the detail message
     */
    public IllegalCollectionException(String msg) {
        super(msg);
    }

    /**
     * Construct a {@code IllegalQueryException} with the specified detail message
     * and nested exception.
     *
     * @param msg the detail message
     * @param cause the nested exception
     */
    public IllegalCollectionException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}
