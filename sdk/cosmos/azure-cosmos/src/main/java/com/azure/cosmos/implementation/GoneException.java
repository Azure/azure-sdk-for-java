// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.HttpUtils;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.net.SocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Gone exception.
 */
public class GoneException extends CosmosException {

    private boolean basedOn410ResponseFromService = false;

    /**
     * Instantiates a new Gone exception.
     *
     * @param msg the msg
     */
    public GoneException(String msg) {
        this(msg, null);
    }

    /**
     * Instantiates a new Gone exception.
     */
    public GoneException() {
        this(RMResources.Gone, null);
    }

    /**
     * Instantiates a new Gone exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public GoneException(CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                         Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.GONE, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    /**
     * Instantiates a new Gone exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     *
     */
    public GoneException(String resourceAddress, CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                         Map<String, String> responseHeaders, Throwable cause) {
        super(resourceAddress, HttpConstants.StatusCodes.GONE, cosmosError, responseHeaders, cause);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    /**
     * Instantiates a new Gone exception.
     *
     * @param message the message
     * @param requestUri the request uri
     */
    public GoneException(String message, String requestUri) {
        this(message, null, new HashMap<>(), requestUri);
    }

    /**
     * Instantiates a new {@link GoneException Gone exception}.
     *
     * @param message    the message
     * @param requestUri the request uri
     * @param cause      the cause of this (client-side) {@link GoneException}
     */
    public GoneException(String message, URI requestUri, Exception cause) {
        this(message, cause, null, requestUri);
    }

    GoneException(Exception innerException) {
        this(RMResources.Gone, innerException, new HashMap<>(), null);
    }

    // Used via reflection from unit tests
    GoneException(String message, HttpHeaders headers, String requestUriString) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.GONE, requestUriString);
    }

    /**
     * Instantiates a new Gone exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUrl the request url
     */
    public GoneException(String message, HttpHeaders headers, URI requestUrl) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.GONE, requestUrl != null
                                                                                           ? requestUrl.toString()
                                                                                           : null);
    }

    /**
     * Instantiates a new Gone exception.
     *
     * @param message the message
     * @param headers the headers
     * @param remoteAddress the remote address
     */
    public GoneException(String message, HttpHeaders headers, SocketAddress remoteAddress) {
        super(
            message,
            null,
            HttpUtils.asMap(headers),
            HttpConstants.StatusCodes.GONE,
            remoteAddress != null ? remoteAddress.toString() : null);
    }

    /**
     * Instantiates a new Gone exception.
     *
     * @param message the message
     * @param innerException the inner exception
     * @param headers the headers
     * @param requestUrl the request url
     */
    public GoneException(String message,
                         Exception innerException,
                         HttpHeaders headers,
                         URI requestUrl) {
        super(message,
            innerException,
            HttpUtils.asMap(headers),
            HttpConstants.StatusCodes.GONE,
            requestUrl != null
                ? requestUrl.toString()
                : null);
    }

    /**
     * Instantiates a new Gone exception.
     *
     * @param message the message
     * @param innerException the inner exception
     * @param headers the headers
     * @param requestUriString the request uri string
     */
    public GoneException(String message,
                         Exception innerException,
                         Map<String, String> headers,
                         String requestUriString) {
        super(message, innerException, headers, HttpConstants.StatusCodes.GONE, requestUriString);
    }

    public boolean isBasedOn410ResponseFromService() {
        return this.basedOn410ResponseFromService;
    }

    public void setIsBasedOn410ResponseFromService() {
        this.basedOn410ResponseFromService = true;
    }
}
