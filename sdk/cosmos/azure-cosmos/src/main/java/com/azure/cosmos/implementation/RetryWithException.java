// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;

import java.net.URI;

/**
 * The type Retry with exception.
 */
public class RetryWithException extends CosmosException {

    /**
     * Instantiates a new Retry with exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public RetryWithException(CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                              HttpHeaders responseHeaders) {
        super(HttpConstants.StatusCodes.RETRY_WITH, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    /**
     * Instantiates a new Retry with exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public RetryWithException(String message, HttpHeaders headers, URI requestUri) {
        super(message,
            null,
            headers,
            HttpConstants.StatusCodes.RETRY_WITH,
            requestUri != null ? requestUri.toString() : null);
    }
}
