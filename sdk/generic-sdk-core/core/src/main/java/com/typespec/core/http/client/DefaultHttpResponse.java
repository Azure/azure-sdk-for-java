// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.client;

import com.typespec.core.http.models.HttpHeaderName;
import com.typespec.core.http.models.HttpRequest;
import com.typespec.core.http.models.HttpResponse;
import com.typespec.core.models.BinaryData;
import com.typespec.core.http.models.HttpHeaders;

class DefaultHttpResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders httpHeaders;
    private final BinaryData body;

    public DefaultHttpResponse(HttpRequest request, int statusCode, HttpHeaders httpHeaders, BinaryData body) {
        super(request);
        this.statusCode = statusCode;
        this.httpHeaders = httpHeaders;
        this.body = body;
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String getHeaderValue(HttpHeaderName name) {
        return httpHeaders.getValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.httpHeaders;
    }

    @Override
    public BinaryData getBody() {
        return this.body;
    }
    public HttpResponse buffer() {
        return this;
    }
}
