// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.MethodNotAllowedException;
import com.azure.cosmos.implementation.http.HttpHeaders;

/**
 * The type Method not allowed exception.
 */
public class CosmosMethodNotAllowedException extends MethodNotAllowedException {

    /**
     * Cosmos exception.
     */
    protected final CosmosException cosmosException;

    /**
     * Instantiates a new Method not allowed exception.
     *
     * @param message the message
     * @param innerException the inner exception
     * @param headers the headers
     * @param requestUriString the request uri string
     * @param cause the nested Throwable
     */
    public CosmosMethodNotAllowedException(String message,
                                     Exception innerException,
                                     HttpHeaders headers,
                                     String requestUriString,
                                     Throwable cause) {
        super(message, innerException, headers, requestUriString);
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
