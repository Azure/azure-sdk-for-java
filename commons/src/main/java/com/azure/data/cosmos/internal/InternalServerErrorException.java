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
import java.net.URL;
import java.util.Map;

/**
 * This exception is thrown when DocumentServiceRequest contains x-ms-documentdb-partitionkeyrangeid
 * header and such range id doesn't exist.
 * <p>
 * No retries should be made in this case, as either split or merge might have happened and query/readfeed
 * must take appropriate actions.
 */
public class InternalServerErrorException extends CosmosClientException {

    public InternalServerErrorException() {
        this(RMResources.InternalServerError);
    }

    public InternalServerErrorException(Error error, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, error, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    public InternalServerErrorException(String message) {
        this(message, null, (Map<String, String>) null, null);
    }


    public InternalServerErrorException(String message, Exception innerException) {
        this(message, innerException, (HttpHeaders) null, (String) null);
    }

    public InternalServerErrorException(Exception innerException) {
        this(RMResources.InternalServerError, innerException, (HttpHeaders) null, (String) null);
    }
    
    public InternalServerErrorException(String message, HttpHeaders headers, URI requestUri) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUri != null ? requestUri.toString() : null);
    }

    public InternalServerErrorException(String message, HttpHeaders headers, String requestUriString) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUriString);
    }

    public InternalServerErrorException(String message, HttpHeaders headers, URL requestUrl) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUrl != null ? requestUrl.toString() : null);
    }

    public InternalServerErrorException(String message, Exception innerException, HttpHeaders headers, URI requestUri) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUri != null ? requestUri.toString() : null);
    }

    public InternalServerErrorException(String message, Exception innerException, HttpHeaders headers, String requestUriString) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUriString);
    }

    public InternalServerErrorException(String message, Exception innerException, Map<String, String> headers, String requestUriString) {
        super(message, innerException, headers, HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUriString);
    }
}
