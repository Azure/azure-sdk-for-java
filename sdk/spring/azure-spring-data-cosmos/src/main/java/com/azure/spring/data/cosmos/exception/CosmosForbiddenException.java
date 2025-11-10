// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import jakarta.annotation.Nullable;

/**
 * Exception for Cosmos Forbidden Exception.
 */
public final class CosmosForbiddenException extends CosmosAccessException {

    /**
     * Construct a {@code CosmosDBAccessException} with the specified detail message.
     * and nested exception.
     * @param msg the detail message
     * @param cause the nested Throwable
     */
    public CosmosForbiddenException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }

}
