// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import org.springframework.lang.Nullable;

/**
 * Exception for Cosmos Partition Key Range Gone Exception.
 */
public final class CosmosPartitionKeyRangeGoneException extends CosmosAccessException {

    /**
     * Construct a {@code CosmosDBAccessException} with the specified detail message.
     * and nested exception.
     * @param msg the detail message
     * @param cause the nested Throwable
     */
    public CosmosPartitionKeyRangeGoneException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }

}
