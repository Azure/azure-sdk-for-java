// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.directconnectivity.HttpUtils;
import com.azure.data.cosmos.internal.http.HttpHeaders;

import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * This exception is thrown when DocumentServiceRequest contains x-ms-documentdb-partitionkeyrangeid
 * header and such range id doesn't exist.
 * <p>
 * No retries should be made in this case, as either split or merge might have happened and query/readfeed
 * must take appropriate actions.
 */
public class InternalServerErrorException extends CosmosClientException {

    InternalServerErrorException() {
        this(RMResources.InternalServerError);
    }

    public InternalServerErrorException(CosmosError cosmosError, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    public InternalServerErrorException(String message) {
        this(message, null, (Map<String, String>) null, null);
    }


    InternalServerErrorException(String message, Exception innerException) {
        this(message, innerException, (HttpHeaders) null, (String) null);
    }

    InternalServerErrorException(Exception innerException) {
        this(RMResources.InternalServerError, innerException, (HttpHeaders) null, (String) null);
    }
    
    public InternalServerErrorException(String message, HttpHeaders headers, URI requestUri) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUri != null ? requestUri.toString() : null);
    }

    InternalServerErrorException(String message, HttpHeaders headers, String requestUriString) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUriString);
    }

    InternalServerErrorException(String message, HttpHeaders headers, URL requestUrl) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUrl != null ? requestUrl.toString() : null);
    }

    InternalServerErrorException(String message, Exception innerException, HttpHeaders headers, URI requestUri) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUri != null ? requestUri.toString() : null);
    }

    InternalServerErrorException(String message, Exception innerException, HttpHeaders headers, String requestUriString) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUriString);
    }

    public InternalServerErrorException(String message, Exception innerException, Map<String, String> headers, String requestUriString) {
        super(message, innerException, headers, HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUriString);
    }
}
