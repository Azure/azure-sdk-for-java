// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.directconnectivity.HttpUtils;
import com.azure.data.cosmos.internal.http.HttpHeaders;

import java.net.URI;
import java.util.Map;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class NotFoundException extends CosmosClientException {
    private static final long serialVersionUID = 1L;

    public NotFoundException() {
        this(RMResources.NotFound);
    }

    public NotFoundException(CosmosError cosmosError, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.NOTFOUND, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    public NotFoundException(String message) {
        this(message, null, (HttpHeaders) null, null);
    }

    public NotFoundException(String message, Map<String, String> headers, String requestUri) {
        this(message, null, headers, requestUri);
    }

    NotFoundException(String message, HttpHeaders headers, String requestUri) {
        this(message, null, headers, requestUri);
    }

    public NotFoundException(String message, HttpHeaders headers, URI requestUri) {
        this(message, headers, requestUri != null ? requestUri.toString() : null);
    }

    NotFoundException(Exception innerException) {
        this(RMResources.NotFound, innerException, (Map) null, null);
    }

    NotFoundException(String message,
                                 Exception innerException,
                                 HttpHeaders headers,
                                 String requestUri) {
        this(message, innerException, HttpUtils.asMap(headers), requestUri);
    }

    NotFoundException(String message,
                             Exception innerException,
                             Map<String, String> headers,
                             String requestUri) {
        super(String.format("%s: %s", RMResources.NotFound, message),
              innerException,
              headers,
              HttpConstants.StatusCodes.NOTFOUND,
              requestUri);
    }
}