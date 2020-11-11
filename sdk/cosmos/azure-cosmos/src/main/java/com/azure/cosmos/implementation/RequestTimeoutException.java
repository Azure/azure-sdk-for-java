// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.HttpUtils;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.net.SocketAddress;
import java.net.URI;
import java.util.Map;

/**
 * The type Request timeout exception.
 */
public class RequestTimeoutException extends CosmosException {

    /**
     * Instantiates a new Request timeout exception.
     */
    public RequestTimeoutException() {
        this(RMResources.RequestTimeout, null);
    }

    /**
     * Instantiates a new Request timeout exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public RequestTimeoutException(CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                                   Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.REQUEST_TIMEOUT, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    /**
     * Instantiates a new Request timeout exception.
     *
     * @param message the message
     * @param requestUri the request uri
     */
    public RequestTimeoutException(String message, URI requestUri) {
        this(message, null, null, requestUri);
    }

    /**
     * Instantiates a new Request timeout exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUrl the request url
     */
    public RequestTimeoutException(String message, HttpHeaders headers, URI requestUrl) {
        super(message,
            null,
            HttpUtils.asMap(headers),
            HttpConstants.StatusCodes.REQUEST_TIMEOUT,
            requestUrl != null
                ? requestUrl.toString()
                : null);
    }

    /**
     * Instantiates a new Request timeout exception.
     *
     * @param message the message
     * @param headers the headers
     * @param remoteAddress the remote address
     */
    public RequestTimeoutException(String message, HttpHeaders headers, SocketAddress remoteAddress) {
        super(message,
            null,
            HttpUtils.asMap(headers),
            HttpConstants.StatusCodes.REQUEST_TIMEOUT,
            remoteAddress != null
                ? remoteAddress.toString()
                : null);
    }

    // Used via reflection from unit tests
    RequestTimeoutException(String message, HttpHeaders headers, String requestUriString) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.REQUEST_TIMEOUT, requestUriString);
    }

    RequestTimeoutException(String message,
                            Exception innerException,
                            HttpHeaders headers,
                            URI requestUrl) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.REQUEST_TIMEOUT,
            requestUrl != null ? requestUrl.toString() : null);
    }
}
