// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.io.Serial;
import java.util.Map;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class CosmosInvalidPartitionException extends InvalidPartitionException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Invalid partition exception.
     */
    public CosmosInvalidPartitionException() {
        super();
    }

    /**
     * Instantiates a new Invalid partition exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public CosmosInvalidPartitionException(CosmosError cosmosError,
                                     long lsn,
                                     String partitionKeyRangeId,
                                     Map<String, String> responseHeaders) {
        super(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
    }

    /**
     * Instantiates a new Invalid partition exception.
     *
     * @param msg the msg
     */
    public CosmosInvalidPartitionException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new Invalid partition exception.
     *
     * @param msg the msg
     * @param resourceAddress the resource address
     */
    public CosmosInvalidPartitionException(String msg, String resourceAddress) {
        super(msg, resourceAddress);
    }

    /**
     * Instantiates a new Invalid partition exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public CosmosInvalidPartitionException(String message, HttpHeaders headers, String requestUri) {
        super(message, headers, requestUri);
    }

}
