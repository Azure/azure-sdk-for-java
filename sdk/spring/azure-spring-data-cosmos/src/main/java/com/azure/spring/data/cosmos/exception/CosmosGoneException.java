// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.net.SocketAddress;
import java.net.URI;
import java.util.Map;

/**
 * The type Gone exception.
 */
public class CosmosGoneException extends GoneException {

    /**
     * Instantiates a new Gone exception.
     *
     * @param msg the msg
     */
    public CosmosGoneException(String msg) {
        super(msg);
    }

    public CosmosGoneException(String msg, int subStatusCode) {
        super(msg, subStatusCode);
    }

    /**
     * Instantiates a new Gone exception.
     */
    public CosmosGoneException() {
        super();
    }

    /**
     * Instantiates a new Gone exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public CosmosGoneException(CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                         Map<String, String> responseHeaders, int subStatusCode) {
        super(cosmosError, lsn, partitionKeyRangeId, responseHeaders, subStatusCode);
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
    public CosmosGoneException(String resourceAddress, CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                         Map<String, String> responseHeaders, Throwable cause, int subStatusCode) {
        super(resourceAddress, cosmosError, lsn, partitionKeyRangeId, responseHeaders, cause, subStatusCode);
    }

    /**
     * Instantiates a new Gone exception.
     *
     * @param message the message
     * @param requestUri the request uri
     */
    public CosmosGoneException(String message, String requestUri, int subStatusCode) {
        super(message, requestUri, subStatusCode);
    }

    /**
     * Instantiates a new {@link GoneException Gone exception}.
     *
     * @param message    the message
     * @param requestUri the request uri
     * @param cause      the cause of this (client-side) {@link GoneException}
     */
    public CosmosGoneException(String message, URI requestUri, Exception cause, int subStatusCode) {
        super(message, requestUri, cause, subStatusCode);
    }

    /**
     * Instantiates a new Gone exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUrl the request url
     */
    public CosmosGoneException(String message, HttpHeaders headers, URI requestUrl, int subStatusCode) {
        super(message, headers, requestUrl, subStatusCode);
    }

    /**
     * Instantiates a new Gone exception.
     *
     * @param message the message
     * @param headers the headers
     * @param remoteAddress the remote address
     */
    public CosmosGoneException(String message, HttpHeaders headers, SocketAddress remoteAddress, int subStatusCode) {
        super(message, headers, remoteAddress, subStatusCode);
    }

    /**
     * Instantiates a new Gone exception.
     *
     * @param message the message
     * @param innerException the inner exception
     * @param headers the headers
     * @param requestUrl the request url
     */
    public CosmosGoneException(String message,
                         Exception innerException,
                         HttpHeaders headers,
                         URI requestUrl,
                         int subStatusCode) {
        super(message, innerException, headers, requestUrl, subStatusCode);
    }

    /**
     * Instantiates a new Gone exception.
     *
     * @param message the message
     * @param innerException the inner exception
     * @param headers the headers
     * @param requestUriString the request uri string
     */
    public CosmosGoneException(String message,
                         Exception innerException,
                         Map<String, String> headers,
                         String requestUriString,
                         int subStatusCode) {
        super(message, innerException, headers, requestUriString, subStatusCode);
    }

}
