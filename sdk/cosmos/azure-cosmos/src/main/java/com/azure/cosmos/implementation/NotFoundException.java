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
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class NotFoundException extends CosmosException {
    private static final long serialVersionUID = 1L;


    /**
     * Instantiates a new Not found exception.
     */
    public NotFoundException() {
        this(RMResources.NotFound);
    }

    /**
     * Instantiates a new Not found exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public NotFoundException(CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                             Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.NOTFOUND, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    /**
     * Instantiates a new Not found exception.
     *
     * @param message the message
     */
    public NotFoundException(String message) {
        this(message, null, (HttpHeaders) null, null);
    }

    /**
     * Instantiates a new Not found exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public NotFoundException(String message, Map<String, String> headers, String requestUri) {
        this(message, null, headers, requestUri);
    }

    NotFoundException(String message, HttpHeaders headers, String requestUri) {
        this(message, null, headers, requestUri);
    }

    /**
     * Instantiates a new Not found exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public NotFoundException(String message, HttpHeaders headers, URI requestUri) {
        this(message, headers, requestUri != null ? requestUri.toString() : null);
    }

    NotFoundException(Exception innerException) {
        this(RMResources.NotFound, innerException, (Map<String, String>) null, null);
    }

    NotFoundException(String message,
                      Exception innerException,
                      HttpHeaders headers,
                      String requestUri) {
        this(message, innerException, HttpUtils.asMap(headers), requestUri);
    }

    NotFoundException(String message,
                      Exception innerException,
                      Map<String, String> headers,
                      String requestUri) {
        super(String.format("%s: %s", RMResources.NotFound, message),
            innerException,
            headers,
            HttpConstants.StatusCodes.NOTFOUND,
            requestUri);
    }
}
