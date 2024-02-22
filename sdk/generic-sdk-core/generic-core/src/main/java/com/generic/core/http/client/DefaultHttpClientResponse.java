// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;

class DefaultHttpClientResponse extends HttpResponse {
    private final int statusCode;
    private final Headers headers;

    DefaultHttpClientResponse(HttpRequest request, int statusCode, Headers headers, BinaryData value) {
        super(request, value);

        this.statusCode = statusCode;
        this.headers = headers;
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public Headers getHeaders() {
        return this.headers;
    }

    public HttpResponse buffer() {
        return this;
    }
}
