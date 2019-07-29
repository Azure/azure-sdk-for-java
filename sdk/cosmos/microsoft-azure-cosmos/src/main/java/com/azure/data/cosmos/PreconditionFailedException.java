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
public class PreconditionFailedException extends CosmosClientException {

    private static final long serialVersionUID = 1L;

    PreconditionFailedException() {
        this(RMResources.PreconditionFailed);
    }

    public PreconditionFailedException(CosmosError cosmosError, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.PRECONDITION_FAILED, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    PreconditionFailedException(String msg) {
        super(HttpConstants.StatusCodes.PRECONDITION_FAILED, msg);
    }

    PreconditionFailedException(String msg, String resourceAddress) {
        super(msg, null, null, HttpConstants.StatusCodes.PRECONDITION_FAILED, resourceAddress);
    }

    public PreconditionFailedException(String message, HttpHeaders headers, String requestUriString) {
        this(message, null, headers, requestUriString);
    }

    PreconditionFailedException(Exception innerException) {
        this(RMResources.PreconditionFailed, innerException, null, null);
    }

    PreconditionFailedException(String message,
                                       Exception innerException,
                                       HttpHeaders headers,
                                       String requestUriString) {
        super(String.format("%s: %s", RMResources.PreconditionFailed, message),
                innerException,
                HttpUtils.asMap(headers),
                HttpConstants.StatusCodes.PRECONDITION_FAILED,
                requestUriString);
    }
}