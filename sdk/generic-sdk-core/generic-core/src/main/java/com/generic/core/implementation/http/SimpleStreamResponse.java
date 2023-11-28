// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http;

import com.generic.core.http.StreamResponse;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.Headers;
import com.generic.core.util.logging.ClientLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * An {@link InputStream}-backed {@link StreamResponse}.
 */
public final class SimpleStreamResponse implements StreamResponse {
    private static final ClientLogger LOGGER = new ClientLogger(SimpleStreamResponse.class);

    private volatile boolean consumed;
    private final HttpResponse response;
    private final HttpRequest request;
    private final int statusCode;
    private final Headers headers;
    private final InputStream value;

    /**
     * Creates a {@link SimpleStreamResponse}.
     *
     * @param request The request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param headers The headers of the HTTP response.
     * @param value The content of the HTTP response.
     */
    public SimpleStreamResponse(HttpRequest request, int statusCode, Headers headers, InputStream value) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.value = value;
        this.response = null;
    }

    /**
     * Creates a {@link SimpleStreamResponse}.
     *
     * @param response The HTTP response to create this {@link SimpleStreamResponse} from.
     */
    public SimpleStreamResponse(HttpResponse response) {
        this.request = response.getRequest();
        this.statusCode = response.getStatusCode();
        this.headers = response.getHeaders();
        this.value = response.getBody().toStream();
        this.response = response;
    }

    /**
     * Gets the request which resulted in this {@link SimpleResponse}.
     *
     * @return The request which resulted in this {@link SimpleResponse}.
     */
    @Override
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * Gets the status code of the HTTP response.
     *
     * @return The status code of the HTTP response.
     */
    @Override
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Headers getHeaders() {
        return headers;
    }

    /**
     * The content of the HTTP response as an {@link InputStream}.
     *
     * @return The content of the HTTP response as an {@link InputStream}.
     */
    @Override
    public InputStream getValue() {
        if (response == null) {
            return value;
        } else {
            return response.getBody().toStream();
        }
    }

    /**
     * Disposes the connection associated with this {@link SimpleStreamResponse}.
     */
    @Override
    public void close() {
        if (this.consumed) {
            return;
        }

        this.consumed = true;

        if (response == null) {
            final InputStream value = getValue();

            try {
                value.close();
            } catch (IOException e) {
                throw LOGGER.logThrowableAsError(new UncheckedIOException("Failed to close response stream.", e));
            }
        } else {
            response.close();
        }
    }
}
