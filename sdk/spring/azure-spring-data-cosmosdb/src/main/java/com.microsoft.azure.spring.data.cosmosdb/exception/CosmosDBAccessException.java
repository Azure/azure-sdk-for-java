// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.exception;

import com.azure.data.cosmos.CosmosClientException;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.Nullable;

/**
 * Public class extending DataAccessException, exposes innerException.
 * Every API in {@link com.microsoft.azure.spring.data.cosmosdb.repository.CosmosRepository}
 * and {@link com.microsoft.azure.spring.data.cosmosdb.repository.ReactiveCosmosRepository}
 * should throw {@link CosmosDBAccessException}.
 * innerException refers to the exception thrown by CosmosDB SDK. Callers of repository APIs can
 * rely on innerException for any retriable logic, or for more details on the failure of
 * the operation.
 */
public class CosmosDBAccessException extends DataAccessException {

    protected final CosmosClientException cosmosClientException;

    public CosmosDBAccessException(String msg) {
        super(msg);
        this.cosmosClientException = null;
    }

    public CosmosDBAccessException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
        if (cause instanceof CosmosClientException) {
            this.cosmosClientException = (CosmosClientException) cause;
        } else {
            this.cosmosClientException = null;
        }
    }

    public CosmosDBAccessException(@Nullable String msg, @Nullable Exception cause) {
        super(msg, cause);
        this.cosmosClientException = cause instanceof CosmosClientException
            ? (CosmosClientException) cause
            : null;
    }

    public CosmosClientException getCosmosClientException() {
        return cosmosClientException;
    }
}
