// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.lang.Nullable;

/**
 * General exception for illegal query of cosmos db
 */
public class IllegalQueryException extends DataAccessException {

    /**
     * Construct a {@code IllegalQueryException} with the specified detail message.
     * @param msg the detail message
     */
    public IllegalQueryException(String msg) {
        super(msg);
    }

    /**
     * Construct a {@code IllegalQueryException} with the specified detail message
     * and nested exception.
     *
     * @param msg the detail message
     * @param cause the nested exception
     */
    public IllegalQueryException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}
