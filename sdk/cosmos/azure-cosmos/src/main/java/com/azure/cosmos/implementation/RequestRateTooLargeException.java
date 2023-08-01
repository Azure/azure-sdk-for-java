// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.HttpUtils;
import com.azure.cosmos.implementation.http.HttpHeaders;

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
                                        Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.TOO_MANY_REQUESTS, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    RequestRateTooLargeException(String message, URI requestUri) {
        this(message, null, null, requestUri);
    }

    RequestRateTooLargeException(String message,
                                 Exception innerException,
                                 URI requestUri) {
        this(message, innerException, null, requestUri);
    }

    RequestRateTooLargeException(Exception innerException) {
        this(RMResources.TooManyRequests, innerException, null, null);
    }

    /**
     * Instantiates a new Request rate too large exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public RequestRateTooLargeException(String message, HttpHeaders headers, URI requestUri) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.TOO_MANY_REQUESTS,
            requestUri != null ? requestUri.toString() : null);
    }

    RequestRateTooLargeException(String message, HttpHeaders headers, String requestUriString) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.TOO_MANY_REQUESTS, requestUriString);
    }

    RequestRateTooLargeException(String message,
                                 Exception innerException,
                                 HttpHeaders headers,
                                 URI requestUri) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.TOO_MANY_REQUESTS,
            requestUri != null ? requestUri.toString() : null);
    }
}
