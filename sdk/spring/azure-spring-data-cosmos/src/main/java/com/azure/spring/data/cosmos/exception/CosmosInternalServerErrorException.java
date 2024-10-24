// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.net.URI;
import java.util.Map;

/**
 * This exception is thrown when DocumentServiceRequest contains x-ms-documentdb-partitionkeyrangeid
 * header and such range id doesn't exist.
 * <p>
 * No retries should be made in this case, as either split or merge might have happened and query/readfeed
 * must take appropriate actions.
 */
public class CosmosInternalServerErrorException extends InternalServerErrorException {

    /**
     * Instantiates a new Internal server error exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public CosmosInternalServerErrorException(CosmosError cosmosError,
                                        long lsn,
                                        String partitionKeyRangeId,
                                        Map<String, String> responseHeaders) {
        super(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
    }

    /**
     * Instantiates a new Internal server error exception.
     *
     * @param message the message
     */
    public CosmosInternalServerErrorException(String message, int subStatusCode) {
        super(message, subStatusCode);
    }

    public CosmosInternalServerErrorException(String message, Exception innerException, int subStatusCode) {
        super(message, innerException, subStatusCode);
    }

    /**
     * Instantiates a new Internal server error exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     * @param subStatusCode the subStatusCode
     */
    public CosmosInternalServerErrorException(String message, HttpHeaders headers, URI requestUri, int subStatusCode) {
        super(message, headers, requestUri, subStatusCode);
    }

    /**
     * Instantiates a new Internal server error exception.
     *
     * @param message the message
     * @param innerException the inner exception
     * @param headers the headers
     * @param requestUriString the request uri string
     */
    public CosmosInternalServerErrorException(String message, Exception innerException, Map<String, String> headers,
                                        String requestUriString) {
        super(message, innerException, headers, requestUriString);
    }

    public CosmosInternalServerErrorException(
        String message,
        Exception innerException,
        Map<String, String> headers,
        String requestUriString,
        int subStatusCode) {
        super(message, innerException, headers, requestUriString, subStatusCode);
    }

}
