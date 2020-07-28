// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.HttpUtils;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.util.Map;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class RequestEntityTooLargeException extends CosmosException {
    private static final long serialVersionUID = 1L;

    RequestEntityTooLargeException() {
        this(RMResources.RequestEntityTooLarge);
    }

    /**
     * Instantiates a new Request entity too large exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public RequestEntityTooLargeException(CosmosError cosmosError,
                                          long lsn,
                                          String partitionKeyRangeId,
                                          Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    RequestEntityTooLargeException(String msg) {
        super(HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE, msg);
    }

    RequestEntityTooLargeException(String msg, String resourceAddress) {
        super(msg, null, null, HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE, resourceAddress);
    }

    /**
     * Instantiates a new Request entity too large exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public RequestEntityTooLargeException(String message, HttpHeaders headers, String requestUri) {
        this(message, null, headers, requestUri);
    }

    RequestEntityTooLargeException(Exception innerException) {
        this(RMResources.RequestEntityTooLarge, innerException, null, null);
    }

    RequestEntityTooLargeException(String message,
                                   Exception innerException,
                                   HttpHeaders headers,
                                   String requestUri) {
        super(String.format(RMResources.RequestEntityTooLarge, message),
            innerException,
            HttpUtils.asMap(headers),
            HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE,
            requestUri);
    }
}
