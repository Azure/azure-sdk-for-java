// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.directconnectivity.HttpUtils;
import com.azure.data.cosmos.internal.http.HttpHeaders;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class BadRequestException extends CosmosClientException {
    private static final long serialVersionUID = 1L;

    public BadRequestException(String message, Exception innerException) {
        super(message, innerException, new HashMap<>(), HttpConstants.StatusCodes.BADREQUEST, null);
    }

    public BadRequestException() {
        this(RMResources.BadRequest);
    }

    public BadRequestException(CosmosError cosmosError, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.BADREQUEST, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    public BadRequestException(String message) {
        this(message, null, null, null);
    }

    BadRequestException(String message, HttpHeaders headers, String requestUrlString) {
        this(message, null, headers, requestUrlString);
    }

    public BadRequestException(String message, HttpHeaders headers, URI requestUri) {
        this(message, headers, requestUri != null ? requestUri.toString() : null);
    }

    BadRequestException(Exception innerException) {
        this(RMResources.BadRequest, innerException, null, null);
    }

    BadRequestException(String message,
                             Exception innerException,
                             HttpHeaders headers,
                             String requestUrlString) {
        super(String.format("%s: %s", RMResources.BadRequest, message),
                innerException,
                HttpUtils.asMap(headers),
                HttpConstants.StatusCodes.BADREQUEST,
                requestUrlString);
    }
}
