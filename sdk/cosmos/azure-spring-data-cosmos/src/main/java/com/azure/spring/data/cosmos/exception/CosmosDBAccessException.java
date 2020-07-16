// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.Nullable;

/**
 * Public class extending DataAccessException, exposes innerException.
 * Every API in {@link CosmosRepository}
 * and {@link ReactiveCosmosRepository}
 * should throw {@link CosmosDBAccessException}.
 * innerException refers to the exception thrown by CosmosDB SDK. Callers of repository APIs can
 * rely on innerException for any retriable logic, or for more details on the failure of
 * the operation.
 */
public class CosmosDBAccessException extends DataAccessException {

    protected final CosmosClientException cosmosClientException;

    /**
     * Construct a {@code CosmosDBAccessException} with the specified detail message.
     * @param msg the detail message
     */
    public CosmosDBAccessException(String msg) {
        super(msg);
        this.cosmosClientException = null;
    }

    /**
     * Construct a {@code CosmosDBAccessException} with the specified detail message.
     * and nested exception.
     * @param msg the detail message
     * @param cause the nested Throwable
     */
    public CosmosDBAccessException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
        if (cause instanceof CosmosClientException) {
            this.cosmosClientException = (CosmosClientException) cause;
        } else {
            this.cosmosClientException = null;
        }
    }

    /**
     * Construct a {@code CosmosDBAccessException} with the specified detail message
     * and nested exception.
     *
     * @param msg the detail message
     * @param cause the nested exception
     */
    public CosmosDBAccessException(@Nullable String msg, @Nullable Exception cause) {
        super(msg, cause);
        this.cosmosClientException = cause instanceof CosmosClientException
            ? (CosmosClientException) cause
            : null;
    }

    /**
     * To get exception object for cosmos client
     * @return CosmosClientException
     */
    public CosmosClientException getCosmosClientException() {
        return cosmosClientException;
    }
}
