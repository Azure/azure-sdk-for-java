// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.directconnectivity.HttpUtils;
import com.azure.data.cosmos.internal.http.HttpHeaders;

import java.net.URI;
import java.util.Map;

public class RequestRateTooLargeException extends CosmosClientException {

    public RequestRateTooLargeException() {
        this(RMResources.TooManyRequests, null);
    }

    public RequestRateTooLargeException(CosmosError cosmosError, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.NOTFOUND, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    RequestRateTooLargeException(String message, URI requestUri) {
        this(message, null, null, requestUri);
    }

    RequestRateTooLargeException(String message,
                                        Exception innerException,
                                        URI requestUri) {
        this(message, innerException, null, requestUri);
    }

    RequestRateTooLargeException(Exception innerException) {
        this(RMResources.TooManyRequests, innerException, null, null);
    }

    public RequestRateTooLargeException(String message, HttpHeaders headers, URI requestUri) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.TOO_MANY_REQUESTS, requestUri != null ? requestUri.toString() : null);
    }

    RequestRateTooLargeException(String message, HttpHeaders headers, String requestUriString) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.TOO_MANY_REQUESTS, requestUriString);
    }

    RequestRateTooLargeException(String message,
                                        Exception innerException,
                                        HttpHeaders headers,
                                        URI requestUri) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.TOO_MANY_REQUESTS, requestUri != null ? requestUri.toString() : null);
    }
}
