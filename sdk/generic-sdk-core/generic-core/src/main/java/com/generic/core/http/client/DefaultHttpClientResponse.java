// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.models.HeaderName;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;

class DefaultHttpClientResponse extends HttpResponse {
    private final int statusCode;
    private final Headers headers;

    public DefaultHttpClientResponse(HttpRequest request, int statusCode, Headers headers, BinaryData body) {
        super(request, body);
        this.statusCode = statusCode;
        this.headers = headers;
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String getHeaderValue(HeaderName name) {
        return headers.getValue(name);
    }

    @Override
    public Headers getHeaders() {
        return this.headers;
    }

    @Override
    public HttpResponse buffer() {
        return this;
    }

    @Override
    public boolean isBuffered() {
        return true;
    }
}
