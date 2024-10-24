// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.ForbiddenException;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.net.URI;
import java.util.Map;

/**
 * Forbidden exception
 */
public class CosmosForbiddenException extends ForbiddenException {

    /**
     * Constructor
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public CosmosForbiddenException(CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                              Map<String, String> responseHeaders) {
        super(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
    }

    /**
     * Constructor
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public CosmosForbiddenException(String message, HttpHeaders headers, URI requestUri) {
        super(message, headers, requestUri);
    }

}
