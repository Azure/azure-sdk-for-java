// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import jakarta.annotation.Nullable;

/**
 * Exception for Cosmos Method Not Allowed Exception.
 */
public final class CosmosMethodNotAllowedException extends CosmosAccessException {

    /**
     * Construct a {@code CosmosDBAccessException} with the specified detail message.
     * and nested exception.
     * @param msg the detail message
     * @param cause the nested Throwable
     */
    public CosmosMethodNotAllowedException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }

}
