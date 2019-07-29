// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.directconnectivity.HttpUtils;
import com.azure.data.cosmos.internal.http.HttpHeaders;

import java.net.URI;
import java.util.Map;

public class ServiceUnavailableException extends CosmosClientException {
    ServiceUnavailableException() {
        this(RMResources.ServiceUnavailable);
    }

    public ServiceUnavailableException(CosmosError cosmosError, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.NOTFOUND, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    ServiceUnavailableException(String message) {
        this(message, null, null, null);
    }

    ServiceUnavailableException(String message, HttpHeaders headers, String requestUriString) {
        this(message, null, headers, requestUriString);
    }

    public ServiceUnavailableException(String message, HttpHeaders headers, URI requestUri) {
        this(message, headers, requestUri != null ? requestUri.toString() : null);
    }

    ServiceUnavailableException(Exception innerException) {
        this(RMResources.ServiceUnavailable, innerException, null, null);
    }

    public ServiceUnavailableException(String message,
                                       Exception innerException,
                                       HttpHeaders headers,
                                       String requestUriString) {
        super(String.format("%s: %s", RMResources.ServiceUnavailable, message),
                innerException,
                HttpUtils.asMap(headers),
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                requestUriString);
    }
}
