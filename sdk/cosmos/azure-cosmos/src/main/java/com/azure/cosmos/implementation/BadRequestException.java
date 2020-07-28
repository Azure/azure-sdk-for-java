// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.HttpUtils;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class BadRequestException extends CosmosException {
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Bad request exception.
     *
     * @param message the message
     * @param innerException the inner exception
     */
    public BadRequestException(String message, Exception innerException) {
        super(message, innerException, new HashMap<>(), HttpConstants.StatusCodes.BADREQUEST, null);
    }

    /**
     * Instantiates a new Bad request exception.
     */
    public BadRequestException() {
        this(RMResources.BadRequest);
    }

    /**
     * Instantiates a new Bad request exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public BadRequestException(CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                               Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.BADREQUEST, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    /**
     * Instantiates a new Bad request exception.
     *
     * @param message the message
     */
    public BadRequestException(String message) {
        this(message, null, null, null);
    }

    BadRequestException(String message, HttpHeaders headers, String requestUrlString) {
        this(message, null, headers, requestUrlString);
    }

    /**
     * Instantiates a new Bad request exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public BadRequestException(String message, HttpHeaders headers, URI requestUri) {
        this(message, headers, requestUri != null ? requestUri.toString() : null);
    }

    BadRequestException(Exception innerException) {
        this(RMResources.BadRequest, innerException, null, null);
    }

    BadRequestException(String message,
                        Exception innerException,
                        HttpHeaders headers,
                        String requestUrlString) {
        super(String.format("%s: %s", RMResources.BadRequest, message),
            innerException,
            HttpUtils.asMap(headers),
            HttpConstants.StatusCodes.BADREQUEST,
            requestUrlString);
    }
}
