// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.HttpUtils;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.net.URI;

/**
 * The type Request cancelled exception.
 */
public class RequestCancelledException extends CosmosException {

    /**
     * Instantiates a new Request cancelled exception.
     */
    public RequestCancelledException() {
        this(RMResources.RequestCancelled, null);
    }

    /**
     * Instantiates a new Request cancelled exception.
     *
     * @param message the message
     * @param requestUri the request uri
     */
    public RequestCancelledException(String message, URI requestUri) {
        this(message, null, null, requestUri);
    }

    RequestCancelledException(String message,
                              Exception innerException,
                              HttpHeaders headers,
                              URI requestUrl) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.REQUEST_CANCELLED,
            requestUrl != null ? requestUrl.toString() : null);
    }
}
