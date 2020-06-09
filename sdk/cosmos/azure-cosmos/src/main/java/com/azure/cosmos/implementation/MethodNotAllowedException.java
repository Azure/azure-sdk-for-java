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
 * The type Method not allowed exception.
 */
public class MethodNotAllowedException extends CosmosException {
    MethodNotAllowedException() {
        this(RMResources.MethodNotAllowed);
    }

    /**
     * Instantiates a new Method not allowed exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public MethodNotAllowedException(CosmosError cosmosError,
                                     long lsn,
                                     String partitionKeyRangeId,
                                     Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.METHOD_NOT_ALLOWED, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    MethodNotAllowedException(String message) {
        this(message, null, null, null);
    }

    MethodNotAllowedException(String message, HttpHeaders headers, String requestUriString) {
        this(message, null, headers, requestUriString);
    }

    MethodNotAllowedException(String message, HttpHeaders headers, URI requestUri) {
        this(message, headers, requestUri != null ? requestUri.toString() : null);
    }

    MethodNotAllowedException(Exception innerException) {
        this(RMResources.MethodNotAllowed, innerException, null, null);
    }

    /**
     * Instantiates a new Method not allowed exception.
     *
     * @param message the message
     * @param innerException the inner exception
     * @param headers the headers
     * @param requestUriString the request uri string
     */
    public MethodNotAllowedException(String message,
                                     Exception innerException,
                                     HttpHeaders headers,
                                     String requestUriString) {
        super(String.format("%s: %s", RMResources.MethodNotAllowed, message),
            innerException,
            HttpUtils.asMap(headers),
            HttpConstants.StatusCodes.METHOD_NOT_ALLOWED,
            requestUriString);
    }
}
