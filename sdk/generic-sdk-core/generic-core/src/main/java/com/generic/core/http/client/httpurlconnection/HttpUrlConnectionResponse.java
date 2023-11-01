// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.httpurlconnection;

import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;

class HttpUrlConnectionResponse extends HttpResponse {
    private final int statusCode;
    private final Headers headers;
    private final BinaryData body;

    public HttpUrlConnectionResponse(HttpRequest request, int statusCode, Headers headers, BinaryData body) {
        super(request);
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String getHeaderValue(HttpHeaderName name) {
        return headers.getValue(name);
    }

    @Override
    public Headers getHeaders() {
        return this.headers;
    }

    @Override
    public BinaryData getBody() {
        return this.body;
    }
    public HttpResponse buffer() {
        return this;
    }
}
