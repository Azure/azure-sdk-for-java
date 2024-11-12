// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.RequestTimeoutException;

import java.net.URI;

/**
 * The type Request timeout exception.
 */
public class CosmosRequestTimeoutException extends RequestTimeoutException {

    /**
     * Cosmos exception.
     */
    protected final CosmosException cosmosException;

    /**
     * Instantiates a new Retry with exception.
     *
     * @param message the message
     * @param requestUri the request uri
     * @param subStatusCode the sub status code
     * @param cause the nested Throwable
     */
    public CosmosRequestTimeoutException(String message, URI requestUri, int subStatusCode, Throwable cause) {
        super(message, requestUri, subStatusCode);
        this.cosmosException = cause instanceof CosmosException ? (CosmosException) cause : null;
    }

    /**
     * To get exception object for cosmos client
     * @return CosmosException
     */
    public CosmosException getCosmosException() {
        return cosmosException;
    }

}
