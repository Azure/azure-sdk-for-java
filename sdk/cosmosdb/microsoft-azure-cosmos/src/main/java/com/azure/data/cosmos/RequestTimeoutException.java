// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.Strings;
import com.azure.data.cosmos.internal.directconnectivity.HttpUtils;
import com.azure.data.cosmos.internal.http.HttpHeaders;

import java.net.URI;
import java.util.Map;

public class RequestTimeoutException extends CosmosClientException {

    public RequestTimeoutException() {
        this(RMResources.RequestTimeout, null);
    }

    public RequestTimeoutException(CosmosError cosmosError, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.REQUEST_TIMEOUT, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

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

    public RequestTimeoutException(String message, HttpHeaders headers, URI requestUrl) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.REQUEST_TIMEOUT, requestUrl != null ? requestUrl.toString() : null);
    }

    RequestTimeoutException(String message, HttpHeaders headers, String requestUriString) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.REQUEST_TIMEOUT, requestUriString);
    }

    RequestTimeoutException(String message,
                                   Exception innerException,
                                   HttpHeaders headers,
                                   URI requestUrl) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.REQUEST_TIMEOUT, requestUrl != null ? requestUrl.toString() : null);
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
