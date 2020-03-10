// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.directconnectivity.HttpUtils;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.models.CosmosError;

import java.net.URI;
import java.util.Map;

/**
 * The type Request timeout exception.
 */
public class RequestTimeoutException extends CosmosClientException {

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

    RequestTimeoutException(String message,
                            Exception innerException,
                            URI requestUri,
                            String localIpAddress) {
        this(message(localIpAddress, message), innerException, null, requestUri);
    }

    RequestTimeoutException(Exception innerException) {
        this(RMResources.Gone, innerException, (HttpHeaders) null, null);
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

    private static String message(String localIP, String baseMessage) {
        if (!Strings.isNullOrEmpty(localIP)) {
            return String.format(
                RMResources.ExceptionMessageAddIpAddress,
                baseMessage,
                localIP);
        }

        return baseMessage;
    }
}
