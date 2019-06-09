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
 *
 */

package com.azure.data.cosmos.directconnectivity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.Error;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;

public class TransportException extends Exception {

    final private Error error;
    final private Map<String, Object> headers;
    final private HttpResponseStatus status;

    public TransportException(HttpResponseStatus status, ObjectNode details, Map<String, Object> headers) {

        super("TODO: DANOBLE: format message string based on headers, and status information");
        this.error = BridgeInternal.createError(details);
        this.headers = headers;
        this.status = status;
    }

    public Error getError() {
        return error;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }
}
