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
 * The type Unauthorized exception.
 */
public class UnauthorizedException extends CosmosException {

    UnauthorizedException() {
        this(RMResources.Unauthorized);
    }

    /**
     * Instantiates a new Unauthorized exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public UnauthorizedException(CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                                 Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.UNAUTHORIZED, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    UnauthorizedException(String message) {
        this(message, null, null, null);
    }

    UnauthorizedException(String message, HttpHeaders headers, String requestUriString) {
        this(message, null, headers, requestUriString);
    }

    /**
     * Instantiates a new Unauthorized exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public UnauthorizedException(String message, HttpHeaders headers, URI requestUri) {
        this(message, headers, requestUri != null ? requestUri.toString() : null);
    }

    UnauthorizedException(Exception innerException) {
        this(RMResources.Unauthorized, innerException, null, null);
    }

    UnauthorizedException(String message,
                          Exception innerException,
                          HttpHeaders headers,
                          String requestUri) {
        super(String.format("%s: %s", RMResources.Unauthorized, message),
            innerException,
            HttpUtils.asMap(headers),
            HttpConstants.StatusCodes.UNAUTHORIZED,
            requestUri);
    }
}
