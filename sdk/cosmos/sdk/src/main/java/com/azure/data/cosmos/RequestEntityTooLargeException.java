/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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