// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.MethodNotAllowedException;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.util.Map;

/**
 * The type Method not allowed exception.
 */
public class CosmosMethodNotAllowedException extends MethodNotAllowedException {

    /**
     * Instantiates a new Method not allowed exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public CosmosMethodNotAllowedException(CosmosError cosmosError,
                                     long lsn,
                                     String partitionKeyRangeId,
                                     Map<String, String> responseHeaders) {
        super(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
    }

    /**
     * Instantiates a new Method not allowed exception.
     *
     * @param message the message
     * @param innerException the inner exception
     * @param headers the headers
     * @param requestUriString the request uri string
     */
    public CosmosMethodNotAllowedException(String message,
                                     Exception innerException,
                                     HttpHeaders headers,
                                     String requestUriString) {
        super(message, innerException, headers, requestUriString);
    }

}
