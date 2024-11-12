// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.RequestEntityTooLargeException;
import com.azure.cosmos.implementation.http.HttpHeaders;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class CosmosRequestEntityTooLargeException extends RequestEntityTooLargeException {

    /**
     * Cosmos exception.
     */
    protected final CosmosException cosmosException;

    /**
     * Instantiates a new Request entity too large exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     * @param cause the nested Throwable
     */
    public CosmosRequestEntityTooLargeException(String message, HttpHeaders headers, String requestUri, Throwable cause) {
        super(message, headers, requestUri);
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
