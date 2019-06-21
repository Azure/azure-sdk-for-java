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
import com.azure.data.cosmos.internal.http.HttpHeaders;

import java.net.URI;
import java.util.Map;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class NotFoundException extends CosmosClientException {
    private static final long serialVersionUID = 1L;

    public NotFoundException() {
        this(RMResources.NotFound);
    }

    public NotFoundException(Error error, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.NOTFOUND, error, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    public NotFoundException(String message) {
        this(message, null, (HttpHeaders) null, null);
    }

    public NotFoundException(String message, Map<String, String> headers, String requestUri) {
        this(message, null, headers, requestUri);
    }

    public NotFoundException(String message, HttpHeaders headers, String requestUri) {
        this(message, null, headers, requestUri);
    }

    public NotFoundException(String message, HttpHeaders headers, URI requestUri) {
        this(message, headers, requestUri != null ? requestUri.toString() : null);
    }

    public NotFoundException(Exception innerException) {
        this(RMResources.NotFound, innerException, (Map) null, null);
    }

    public NotFoundException(String message,
                                 Exception innerException,
                                 HttpHeaders headers,
                                 String requestUri) {
        this(message, innerException, HttpUtils.asMap(headers), requestUri);
    }

    public NotFoundException(String message,
                             Exception innerException,
                             Map<String, String> headers,
                             String requestUri) {
        super(String.format("%s: %s", RMResources.NotFound, message),
              innerException,
              headers,
              HttpConstants.StatusCodes.NOTFOUND,
              requestUri);
    }
}