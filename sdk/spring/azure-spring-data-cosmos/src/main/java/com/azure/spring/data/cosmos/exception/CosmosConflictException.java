// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.io.Serial;
import java.util.Map;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class CosmosConflictException extends ConflictException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Conflict exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public CosmosConflictException(CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                             Map<String, String> responseHeaders) {
        super(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
    }

    /**
     * Instantiates a new Conflict exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUriString the request uri string
     */
    public CosmosConflictException(String message, HttpHeaders headers, String requestUriString) {
        super(message, headers, requestUriString);
    }

}
