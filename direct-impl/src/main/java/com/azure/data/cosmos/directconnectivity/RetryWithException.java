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

package com.azure.data.cosmos.directconnectivity;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.Error;
import com.azure.data.cosmos.internal.HttpConstants;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;

import java.net.URI;
import java.util.Map;

public class RetryWithException extends CosmosClientException {

    public RetryWithException(Error error, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.RETRY_WITH, error, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    public RetryWithException(String message, URI requestUri) {
        this(message, (Exception) null, (HttpResponseHeaders) null, requestUri);
    }

    public RetryWithException(String message,
                              Exception innerException,
                              URI requestUri) {
        this(message, innerException, (HttpResponseHeaders) null, requestUri);
    }

    public RetryWithException(String message, HttpResponseHeaders headers, URI requestUri) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.RETRY_WITH, requestUri != null ? requestUri.toString() : null);
    }

    public RetryWithException(String message, HttpResponseHeaders headers, String requestUri) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.RETRY_WITH, requestUri);
    }

    public RetryWithException(String message,
                              Exception innerException,
                              HttpResponseHeaders headers,
                              URI requestUri) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.RETRY_WITH, requestUri != null ? requestUri.toString() : null);
    }
}
