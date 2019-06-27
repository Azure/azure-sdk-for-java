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

import com.azure.data.cosmos.directconnectivity.HttpUtils;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.RMResources;
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