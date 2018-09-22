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

package com.microsoft.azure.cosmosdb.internal;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.HttpUtils;
import com.microsoft.azure.cosmosdb.rx.internal.RMResources;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;

import java.net.URL;

/**
 * This exception is thrown when DocumentServiceRequest contains x-ms-documentdb-partitionkeyrangeid
 * header and such range id doesn't exist.
 * <p>
 * No retries should be made in this case, as either split or merge might have happened and query/readfeed
 * must take appropriate actions.
 */
public class InternalServerErrorException extends DocumentClientException {

    public InternalServerErrorException() {
        this(RMResources.InternalServerError);
    }

    public InternalServerErrorException(String message) {
        this(message, (Exception) null, null, null);
    }


    public InternalServerErrorException(String message, Exception innerException) {
        this(message, innerException, (HttpResponseHeaders) null, null);
    }

    public InternalServerErrorException(Exception innerException) {
        this(RMResources.Gone, innerException, (HttpResponseHeaders) null, null);
    }


    public InternalServerErrorException(String message, HttpResponseHeaders headers, URL requestUri) {
        super(message, null, HttpUtils.asSafeMap(headers), HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUri != null ? requestUri.toString() : null);
    }

    public InternalServerErrorException(String message, Exception innerException, HttpResponseHeaders headers, URL requestUri) {
        super(message, innerException, HttpUtils.asSafeMap(headers), HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, requestUri != null ? requestUri.toString() : null);
    }
}
