// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.io.Serial;
import java.net.URI;
import java.util.Map;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class CosmosNotFoundException extends NotFoundException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Not found exception.
     */
    public CosmosNotFoundException() {
        super();
    }

    /**
     * Instantiates a new Not found exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public CosmosNotFoundException(CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                             Map<String, String> responseHeaders) {
        super(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
    }

    /**
     * Instantiates a new Not found exception.
     *
     * @param message the message
     */
    public CosmosNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Not found exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public CosmosNotFoundException(String message, Map<String, String> headers, String requestUri) {
        super(message, headers, requestUri);
    }

    /**
     * Instantiates a new Not found exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public CosmosNotFoundException(String message, HttpHeaders headers, URI requestUri) {
        super(message, headers, requestUri);
    }

}
