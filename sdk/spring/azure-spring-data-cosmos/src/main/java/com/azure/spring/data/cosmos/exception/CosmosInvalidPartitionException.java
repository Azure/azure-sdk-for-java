// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import jakarta.annotation.Nullable;

/**
 * Exception for Cosmos Invalid Partition Exception.
 */
public final class CosmosInvalidPartitionException extends CosmosAccessException {

    /**
     * Construct a {@code CosmosDBAccessException} with the specified detail message.
     * and nested exception.
     * @param msg the detail message
     * @param cause the nested Throwable
     */
    public CosmosInvalidPartitionException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }

}
