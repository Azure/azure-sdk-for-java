// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.HttpUtils;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.util.Map;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class PreconditionFailedException extends CosmosException {

    private static final long serialVersionUID = 1L;

    PreconditionFailedException() {
        this(RMResources.PreconditionFailed);
    }

    /**
     * Constructor
     * @param cosmosError the error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public PreconditionFailedException(CosmosError cosmosError,
                                       long lsn,
                                       String partitionKeyRangeId,
                                       Map<String, String> responseHeaders) {
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

    /**
     * Constructor
     * @param message the message
     * @param headers the headers
     * @param requestUriString the request uri string
     */
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
