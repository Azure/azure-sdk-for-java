// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.InternalServerErrorException;

/**
 * This exception is thrown when DocumentServiceRequest contains x-ms-documentdb-partitionkeyrangeid
 * header and such range id doesn't exist.
 * <p>
 * No retries should be made in this case, as either split or merge might have happened and query/readfeed
 * must take appropriate actions.
 */
public class CosmosInternalServerErrorException extends InternalServerErrorException {

    /**
     * Cosmos exception.
     */
    protected final CosmosException cosmosException;


    /**
     * Instantiates a new InternalServerError exception.
     *
     * @param message the message
     * @param innerException the inner exception
     * @param subStatusCode the subStatusCode
     * @param cause the nested Throwable
     */
    public CosmosInternalServerErrorException(String message, Exception innerException, int subStatusCode, Throwable cause) {
        super(message, innerException, subStatusCode);
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
