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

package com.azure.data.cosmos.directconnectivity.rntbd;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.Error;
import com.azure.data.cosmos.directconnectivity.TransportException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;

public final class RntbdContextException extends TransportException {

    final private Error error;
    final private Map<String, Object> responseHeaders;
    final private HttpResponseStatus status;

    RntbdContextException(HttpResponseStatus status, ObjectNode details, Map<String, Object> responseHeaders) {

        super(status + ": " + details, null);

        this.error = BridgeInternal.createError(details);
        this.responseHeaders = responseHeaders;
        this.status = status;
    }

    public Error getError() {
        return error;
    }

    public Map<String, Object> getResponseHeaders() {
        return responseHeaders;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }
}