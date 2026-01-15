// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import org.springframework.lang.Nullable;

/**
 * Exception for Cosmos Partition Is Migrating Exception.
 */
@SuppressWarnings("deprecation")
public final class CosmosPartitionIsMigratingException extends CosmosAccessException {

    /**
     * Construct a {@code CosmosDBAccessException} with the specified detail message.
     * and nested exception.
     * @param msg the detail message
     * @param cause the nested Throwable
     */
    public CosmosPartitionIsMigratingException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }

}
