// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GoneException;

import java.util.Map;

/**
 * The type Gone exception.
 */
public class CosmosGoneException extends GoneException {

    /**
     * Cosmos exception.
     */
    protected final CosmosException cosmosException;

    /**
     * Instantiates a new Gone exception.
     *
     * @param message the message
     * @param innerException the inner exception
     * @param headers the headers
     * @param requestUriString the request uri string
     * @param cause the nested Throwable
     */
    public CosmosGoneException(String message,
                         Exception innerException,
                         Map<String, String> headers,
                         String requestUriString,
                         int subStatusCode,
                         Throwable cause) {
        super(message, innerException, headers, requestUriString, subStatusCode);
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
