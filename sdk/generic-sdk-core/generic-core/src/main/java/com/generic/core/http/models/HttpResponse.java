// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.models.BinaryData;
import com.generic.core.models.HeaderName;
import com.generic.core.models.Headers;

import java.io.Closeable;
import java.io.IOException;

/**
 * The response of an {@link HttpRequest}.
 */
public abstract class HttpResponse implements Closeable {
    private final HttpRequest request;
    private final BinaryData body;

    /**
     * Creates an instance of {@link HttpResponse}.
     *
     * @param request The {@link HttpRequest} that resulted in this {@link HttpResponse}.
     */
    protected HttpResponse(HttpRequest request, BinaryData body) {
        this.request = request;
        this.body = body;
    }

    /**
     * Get the response status code.
     *
     * @return The response status code.
     */
    public abstract int getStatusCode();

    /**
     * Lookup a response header with the provider {@link HeaderName}.
     *
     * @param headerName The name of the header to lookup.
     *
     * @return The value of the header, or {@code null} if the header doesn't exist in the response.
     */
    public String getHeaderValue(HeaderName headerName) {
        return getHeaders().getValue(headerName);
    }

    /**
     * Get all response headers.
     *
     * @return the response headers
     */
    public abstract Headers getHeaders();

    /**
     * Gets the {@link BinaryData} that represents the body of the response.
     *
     * <p>Subclasses should override this method.</p>
     *
     * @return The {@link BinaryData} response body.
     */
    public BinaryData getBody() {
        return body;
    }

    /**
     * Gets the {@link HttpRequest request} which resulted in this response.
     *
     * @return The {@link HttpRequest request} which resulted in this response.
     */
    public final HttpRequest getRequest() {
        return request;
    }

    /**
     * Buffers the {@link HttpResponse}.
     * <p>
     * If {@link #isBuffered()} is true, this instance of {@link HttpResponse} is returned. Otherwise, a new instance
     * of {@link HttpResponse} is returned where {@link #getBody()} is buffered into memory.
     *
     * @return The buffered {@link HttpResponse}.
     */
    public abstract HttpResponse buffer();

    /**
     * Whether this {@link HttpResponse} is buffered.
     *
     * @return Whether this {@link HttpResponse} is buffered.
     */
    public abstract boolean isBuffered();

    /**
     * Closes the response content stream, if any.
     *
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        getBody().close();
    }
}
