// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;

import java.net.URI;
import java.util.Map;

/**
 * This exception is thrown when DocumentServiceRequest contains x-ms-documentdb-partitionkeyrangeid
 * header and such range id doesn't exist.
 * <p>
 * No retries should be made in this case, as either split or merge might have happened and query/readfeed
 * must take appropriate actions.
 */
public class InternalServerErrorException extends CosmosException {

    InternalServerErrorException() {
        this(RMResources.InternalServerError);
    }

    /**
     * Instantiates a new Internal server error exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public InternalServerErrorException(CosmosError cosmosError,
                                        long lsn,
                                        String partitionKeyRangeId,
                                        HttpHeaders responseHeaders) {
        super(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    /**
     * Instantiates a new Internal server error exception.
     *
     * @param message the message
     */
    public InternalServerErrorException(String message) {
        this(message, (Exception) null, (HttpHeaders) null, (String) null);
    }

    /**
     * Instantiates a new Internal server error exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public InternalServerErrorException(String message, HttpHeaders headers, URI requestUri) {
        super(message, null, headers, HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR,
            requestUri != null ? requestUri.toString() : null);
    }

    /**
     * Instantiates a new Internal server error exception.
     *
     * @param message the message
     * @param innerException the inner exception
     * @param headers the headers
     * @param requestUriString the request uri string
     */
    public InternalServerErrorException(String message, Exception innerException, HttpHeaders headers,
                                        String requestUriString) {
        super(message, innerException, headers, HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUriString);
    }
}
