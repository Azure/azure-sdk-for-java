// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.query.NonStreamingOrderByBadRequestException;

public class CosmosNonStreamingOrderByBadRequestException extends NonStreamingOrderByBadRequestException {

    /**
     * Cosmos exception.
     */
    protected final CosmosException cosmosException;

    /**
     * Creates a new instance of the NonStreamingOrderByBadRequestException class.
     *
     * @param statusCode the http status code of the response.
     * @param errorMessage the error message.
     * @param cause the nested Throwable
     */
    public CosmosNonStreamingOrderByBadRequestException(int statusCode, String errorMessage, Throwable cause) {
        super(statusCode, errorMessage);
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
