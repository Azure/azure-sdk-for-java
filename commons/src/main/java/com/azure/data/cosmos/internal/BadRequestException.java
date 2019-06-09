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
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.Error;
import com.azure.data.cosmos.directconnectivity.HttpUtils;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;

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

    public BadRequestException(Error error, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.BADREQUEST, error, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    public BadRequestException(String message) {
        this(message, (Exception) null, (HttpResponseHeaders) null, null);
    }

    public BadRequestException(String message, HttpResponseHeaders headers, String requestUri) {
        this(message, null, headers, requestUri);
    }

    public BadRequestException(String message, HttpResponseHeaders headers, URI requestUri) {
        this(message, headers, requestUri != null ? requestUri.toString() : null);
    }

    public BadRequestException(Exception innerException) {
        this(RMResources.BadRequest, innerException, null, null);
    }

    public BadRequestException(String message,
                             Exception innerException,
                             HttpResponseHeaders headers,
                             String requestUri) {
        super(String.format("%s: %s", RMResources.BadRequest, message),
                innerException,
                HttpUtils.asMap(headers),
                HttpConstants.StatusCodes.BADREQUEST,
                requestUri != null ? requestUri.toString() : null);
    }
}
