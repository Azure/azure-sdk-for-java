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
public class LockedException extends CosmosClientException {
    private static final long serialVersionUID = 1L;

    LockedException() {
        this(RMResources.Locked);
    }

    public LockedException(CosmosError cosmosError, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.LOCKED, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    LockedException(String msg) {
        super(HttpConstants.StatusCodes.LOCKED, msg);
    }

    LockedException(String msg, String resourceAddress) {
        super(msg, null, null, HttpConstants.StatusCodes.LOCKED, resourceAddress);
    }

    public LockedException(String message, HttpHeaders headers, String requestUriString) {
        this(message, null, headers, requestUriString);
    }

    LockedException(Exception innerException) {
        this(RMResources.Locked,  innerException, null, null);
    }

    LockedException(String message,
                           Exception innerException,
                           HttpHeaders headers,
                           String requestUriString) {
        super(String.format("%s: %s", RMResources.Locked, message),
                innerException,
                HttpUtils.asMap(headers),
                HttpConstants.StatusCodes.LOCKED,
                requestUriString);
    }
}