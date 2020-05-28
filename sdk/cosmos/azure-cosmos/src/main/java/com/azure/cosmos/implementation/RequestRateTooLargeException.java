// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;

import java.net.URI;
import java.util.Map;

/**
 * The type Request rate too large exception.
 */
public class RequestRateTooLargeException extends CosmosException {

    /**
     * Instantiates a new Request rate too large exception.
     */
    public RequestRateTooLargeException() {
        this(RMResources.TooManyRequests, null);
    }

    /**
     * Instantiates a new Request rate too large exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public RequestRateTooLargeException(CosmosError cosmosError,
                                        long lsn,
                                        String partitionKeyRangeId,
                                        HttpHeaders responseHeaders) {
        super(HttpConstants.StatusCodes.TOO_MANY_REQUESTS, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    RequestRateTooLargeException(String message, URI requestUri) {
        this(message, null, null, requestUri);
    }

    /**
     * Instantiates a new Request rate too large exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public RequestRateTooLargeException(String message, HttpHeaders headers, URI requestUri) {
        super(message, null, headers, HttpConstants.StatusCodes.TOO_MANY_REQUESTS,
            requestUri != null ? requestUri.toString() : null);
    }

    RequestRateTooLargeException(String message,
                                 Exception innerException,
                                 HttpHeaders headers,
                                 URI requestUri) {
        super(message, innerException, headers, HttpConstants.StatusCodes.TOO_MANY_REQUESTS,
            requestUri != null ? requestUri.toString() : null);
    }
}
