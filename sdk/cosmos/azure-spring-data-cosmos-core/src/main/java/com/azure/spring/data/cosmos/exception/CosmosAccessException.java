// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.CosmosException;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.Nullable;

/**
 * Public class extending DataAccessException, exposes innerException.
 * Every API in {@link CosmosRepository}
 * and {@link ReactiveCosmosRepository}
 * should throw {@link CosmosAccessException}.
 * innerException refers to the exception thrown by CosmosDB SDK. Callers of repository APIs can
 * rely on innerException for any retriable logic, or for more details on the failure of
 * the operation.
 */
public class CosmosAccessException extends DataAccessException {

    protected final CosmosException cosmosException;

    /**
     * Construct a {@code CosmosDBAccessException} with the specified detail message.
     * @param msg the detail message
     */
    public CosmosAccessException(String msg) {
        super(msg);
        this.cosmosException = null;
    }

    /**
     * Construct a {@code CosmosDBAccessException} with the specified detail message.
     * and nested exception.
     * @param msg the detail message
     * @param cause the nested Throwable
     */
    public CosmosAccessException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
        if (cause instanceof CosmosException) {
            this.cosmosException = (CosmosException) cause;
        } else {
            this.cosmosException = null;
        }
    }

    /**
     * Construct a {@code CosmosDBAccessException} with the specified detail message
     * and nested exception.
     *
     * @param msg the detail message
     * @param cause the nested exception
     */
    public CosmosAccessException(@Nullable String msg, @Nullable Exception cause) {
        super(msg, cause);
        this.cosmosException = cause instanceof CosmosException
            ? (CosmosException) cause
            : null;
    }

    /**
     * To get exception object for cosmos client
     * @return CosmosException
     */
    public CosmosException getCosmosException() {
        return cosmosException;
    }
}
