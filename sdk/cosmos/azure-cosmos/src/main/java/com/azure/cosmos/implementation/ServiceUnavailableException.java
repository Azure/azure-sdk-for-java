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
 * The type Service unavailable exception.
 */
public class ServiceUnavailableException extends CosmosException {
    ServiceUnavailableException() {
        this(RMResources.ServiceUnavailable);
    }

    /**
     * Instantiates a new Service unavailable exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public ServiceUnavailableException(CosmosError cosmosError,
                                       long lsn,
                                       String partitionKeyRangeId,
                                       Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    ServiceUnavailableException(String message) {
        this(message, null, null, null);
    }

    ServiceUnavailableException(String message, HttpHeaders headers, String requestUriString) {
        this(message, null, headers, requestUriString);
    }

    /**
     * Instantiates a new Service unavailable exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public ServiceUnavailableException(String message, HttpHeaders headers, URI requestUri) {
        this(message, headers, requestUri != null ? requestUri.toString() : null);
    }

    ServiceUnavailableException(Exception innerException) {
        this(RMResources.ServiceUnavailable, innerException, null, null);
    }

    /**
     * Instantiates a new Service unavailable exception.
     *
     * @param message the message
     * @param innerException the inner exception
     * @param headers the headers
     * @param requestUriString the request uri string
     */
    public ServiceUnavailableException(String message,
                                       Exception innerException,
                                       HttpHeaders headers,
                                       String requestUriString) {
        super(
            String.format("%s: %s",
                RMResources.ServiceUnavailable,
                String.format(RMResources.ExceptionMessage, Strings.isNullOrWhiteSpace(message) ? RMResources.ServiceUnavailable : message)),
            innerException,
            HttpUtils.asMap(headers),
            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
            requestUriString);
    }
}
