// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.NotFoundException;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class CosmosNotFoundException extends NotFoundException {

    /**
     * Cosmos exception.
     */
    protected final CosmosException cosmosException;

    /**
     * Instantiates a new Not found exception.
     *
     * @param message the message
     * @param cause the nested Throwable
     */
    public CosmosNotFoundException(String message, Throwable cause) {
        super(message);
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
