// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ForbiddenException;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.net.URI;

/**
 * Forbidden exception
 */
public class CosmosForbiddenException extends ForbiddenException {

    /**
     * Cosmos exception.
     */
    protected final CosmosException cosmosException;

    /**
     * Constructor
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     * @param cause the nested Throwable
     */
    public CosmosForbiddenException(String message, HttpHeaders headers, URI requestUri, Throwable cause) {
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
