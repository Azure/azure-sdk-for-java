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
import com.azure.data.cosmos.internal.Strings;
import com.azure.data.cosmos.internal.http.HttpHeaders;

import java.net.URI;
import java.util.Map;

public class RequestTimeoutException extends CosmosClientException {

    public RequestTimeoutException() {
        this(RMResources.RequestTimeout, null);
    }

    public RequestTimeoutException(CosmosError cosmosError, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.REQUEST_TIMEOUT, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    public RequestTimeoutException(String message, URI requestUri) {
        this(message, null, null, requestUri);
    }

    RequestTimeoutException(String message,
                                   Exception innerException,
                                   URI requestUri,
                                   String localIpAddress) {
        this(message(localIpAddress, message), innerException, null, requestUri);
    }

    RequestTimeoutException(Exception innerException) {
        this(RMResources.Gone, innerException, (HttpHeaders) null, null);
    }

    public RequestTimeoutException(String message, HttpHeaders headers, URI requestUrl) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.REQUEST_TIMEOUT, requestUrl != null ? requestUrl.toString() : null);
    }

    RequestTimeoutException(String message, HttpHeaders headers, String requestUriString) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.REQUEST_TIMEOUT, requestUriString);
    }

    RequestTimeoutException(String message,
                                   Exception innerException,
                                   HttpHeaders headers,
                                   URI requestUrl) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.REQUEST_TIMEOUT, requestUrl != null ? requestUrl.toString() : null);
    }

    private static String message(String localIP, String baseMessage) {
        if (!Strings.isNullOrEmpty(localIP)) {
            return String.format(
                RMResources.ExceptionMessageAddIpAddress,
                baseMessage,
                localIP);
        }

        return baseMessage;
    }
}
