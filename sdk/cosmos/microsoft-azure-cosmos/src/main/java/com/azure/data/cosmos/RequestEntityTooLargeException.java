// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.directconnectivity.HttpUtils;
import com.azure.data.cosmos.internal.http.HttpHeaders;

import java.util.Map;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class RequestEntityTooLargeException extends CosmosClientException {
    private static final long serialVersionUID = 1L;

    RequestEntityTooLargeException() {
        this(RMResources.RequestEntityTooLarge);
    }

    public RequestEntityTooLargeException(CosmosError cosmosError, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    RequestEntityTooLargeException(String msg) {
        super(HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE, msg);
    }

    RequestEntityTooLargeException(String msg, String resourceAddress) {
        super(msg, null, null, HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE, resourceAddress);
    }

    public RequestEntityTooLargeException(String message, HttpHeaders headers, String requestUriString) {
        this(message, null, headers, requestUriString);
    }

    RequestEntityTooLargeException(Exception innerException) {
        this(RMResources.RequestEntityTooLarge, innerException, null, null);
    }

    RequestEntityTooLargeException(String message,
                                          Exception innerException,
                                          HttpHeaders headers,
                                          String requestUriString) {
        super(String.format(RMResources.RequestEntityTooLarge, message),
                innerException,
                HttpUtils.asMap(headers),
                HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE,
                requestUriString);
    }
}