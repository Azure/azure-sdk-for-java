// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.directconnectivity.HttpUtils;
import com.azure.data.cosmos.internal.http.HttpHeaders;

import java.net.URI;
import java.util.Map;

public class RetryWithException extends CosmosClientException {

    public RetryWithException(CosmosError cosmosError, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.RETRY_WITH, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    RetryWithException(String message, URI requestUri) {
        this(message, null, null, requestUri);
    }

    RetryWithException(String message,
                              Exception innerException,
                              URI requestUri) {
        this(message, innerException, null, requestUri);
    }

    public RetryWithException(String message, HttpHeaders headers, URI requestUri) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.RETRY_WITH, requestUri != null ? requestUri.toString() : null);
    }

    RetryWithException(String message, HttpHeaders headers, String requestUriString) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.RETRY_WITH, requestUriString);
    }

    RetryWithException(String message,
                              Exception innerException,
                              HttpHeaders headers,
                              URI requestUri) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.RETRY_WITH, requestUri != null ? requestUri.toString() : null);
    }
}
