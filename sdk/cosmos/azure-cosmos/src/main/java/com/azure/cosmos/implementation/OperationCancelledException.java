// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.HttpUtils;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.net.URI;

/**
 * The type Operation cancelled exception. This is thrown when the operation is cancelled
 * by the {@link CosmosEndToEndOperationLatencyPolicyConfig}
 */
public final class OperationCancelledException extends CosmosException {

    /**
     * Instantiates a new Operation cancelled exception.
     */
    public OperationCancelledException() {
        this(RMResources.OperationCancelled, null);
    }

    /**
     * Instantiates a new Operation cancelled exception.
     *
     * @param message the message
     * @param requestUri the request uri
     */
    public OperationCancelledException(String message, URI requestUri) {
        this(message, null, null, requestUri);
    }

    OperationCancelledException(String message,
                                Exception innerException,
                                HttpHeaders headers,
                                URI requestUrl) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.REQUEST_TIMEOUT,
            requestUrl != null ? requestUrl.toString() : null);
        BridgeInternal.setSubStatusCode(this, HttpConstants.SubStatusCodes.CLIENT_OPERATION_TIMEOUT);
    }
}
