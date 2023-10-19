// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.jdk;

import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.Headers;

/**
 * Base response class for JDK with implementations for response metadata.
 */
abstract class JdkHttpResponseBase extends HttpResponse {
    private final int statusCode;
    private final Headers headers;

    JdkHttpResponseBase(final HttpRequest request, int statusCode, Headers headers) {
        super(request);
        this.statusCode = statusCode;
        this.headers = headers;
    }

    @Override
    public final int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public final String getHeaderValue(HttpHeaderName headerName) {
        return headers.getValue(headerName);
    }

    @Override
    public final Headers getHeaders() {
        return this.headers;
    }
}
