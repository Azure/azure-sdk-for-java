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
 * Forbidden exception
 */
public class ForbiddenException extends CosmosException {
    ForbiddenException() {
        this(RMResources.Forbidden);
    }

    /**
     * Constructor
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public ForbiddenException(CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                              Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.FORBIDDEN, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    ForbiddenException(String message) {
        this(message, null, null, null);
    }

    ForbiddenException(String message, HttpHeaders headers, String requestUrlString) {
        this(message, null, headers, requestUrlString);
    }

    /**
     * Constructor
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public ForbiddenException(String message, HttpHeaders headers, URI requestUri) {
        this(message, headers, requestUri != null ? requestUri.toString() : null);
    }

    ForbiddenException(Exception innerException) {
        this(RMResources.Forbidden, innerException, null, null);
    }

    ForbiddenException(String message,
                       Exception innerException,
                       HttpHeaders headers,
                       String requestUrlString) {
        super(String.format("%s: %s", RMResources.Forbidden, message),
            innerException,
            HttpUtils.asMap(headers),
            HttpConstants.StatusCodes.FORBIDDEN,
            requestUrlString);
    }
}
