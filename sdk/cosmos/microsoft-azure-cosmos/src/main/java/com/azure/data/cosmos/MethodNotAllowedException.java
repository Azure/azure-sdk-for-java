// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.directconnectivity.HttpUtils;
import com.azure.data.cosmos.internal.http.HttpHeaders;

import java.net.URI;
import java.util.Map;

public class MethodNotAllowedException extends CosmosClientException {
    MethodNotAllowedException() {
        this(RMResources.MethodNotAllowed);
    }

    public MethodNotAllowedException(CosmosError cosmosError, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.METHOD_NOT_ALLOWED, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    MethodNotAllowedException(String message) {
        this(message, null, null, null);
    }

    MethodNotAllowedException(String message, HttpHeaders headers, String requestUriString) {
        this(message, null, headers, requestUriString);
    }

    MethodNotAllowedException(String message, HttpHeaders headers, URI requestUri) {
        this(message, headers, requestUri != null ? requestUri.toString() : null);
    }

    MethodNotAllowedException(Exception innerException) {
        this(RMResources.MethodNotAllowed, innerException, null, null);
    }

    public MethodNotAllowedException(String message,
                                 Exception innerException,
                                     HttpHeaders headers,
                                 String requestUriString) {
        super(String.format("%s: %s", RMResources.MethodNotAllowed, message),
                innerException,
                HttpUtils.asMap(headers),
                HttpConstants.StatusCodes.METHOD_NOT_ALLOWED,
                requestUriString);
    }
}
