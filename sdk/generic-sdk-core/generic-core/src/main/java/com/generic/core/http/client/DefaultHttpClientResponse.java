// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.models.HeaderName;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;
import com.generic.core.util.ClientLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

class DefaultHttpClientResponse extends HttpResponse {
    private static final ClientLogger LOGGER = new ClientLogger(DefaultHttpClientResponse.class);
    private final int statusCode;
    private final Headers headers;
    private final BinaryData body;
    private final InputStream responseStream;

    public DefaultHttpClientResponse(HttpRequest request, int statusCode, Headers headers, InputStream responseStream) {
        super(request);
        this.statusCode = statusCode;
        this.headers = headers;
        // TODO: move to a different response impl
        this.responseStream = responseStream;
        this.body = BinaryData.fromStream(this.responseStream, getContentLength(headers));
    }

    DefaultHttpClientResponse(HttpRequest request, int statusCode, Headers headers, BinaryData body) {
        super(request);
        this.statusCode = statusCode;
        this.headers = headers;
        this.responseStream = null;
        this.body = body;
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
    public BinaryData getBody() {
        return this.body;
    }
    public HttpResponse buffer() {
        if (this.body.isReplayable()) {
            return this;
        }

        return new DefaultHttpClientResponse(this.getRequest(), statusCode, headers, body.toReplayableBinaryData());
    }

    @Override
    public void close() {
        if (this.responseStream != null) {
            try {
                this.responseStream.close();
            } catch (IOException e) {
                throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
            }
        }
    }

    private static Long getContentLength(Headers headers) {
        String contentLength = headers.getValue(HeaderName.CONTENT_LENGTH);
        if (contentLength != null) {
            return Long.parseLong(contentLength);
        }
        return null;
    }
}
