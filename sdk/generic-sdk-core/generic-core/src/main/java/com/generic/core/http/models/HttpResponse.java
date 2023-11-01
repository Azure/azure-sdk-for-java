// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;

import java.io.Closeable;

/**
 * The response of an {@link HttpRequest}.
 */
public abstract class HttpResponse implements Closeable {
    private final HttpRequest request;
    private BinaryData binaryData = null;
    private final byte[] bodyBytes;

    /**
     * Creates an instance of {@link HttpResponse}.
     *
     * @param request The {@link HttpRequest} that resulted in this {@link HttpResponse}.
     */
    protected HttpResponse(HttpRequest request) {
        this.request = request;
        this.bodyBytes = null;
    }

    /**
     * Creates an instance of {@link HttpResponse}.
     *
     * @param request The {@link HttpRequest} that resulted in this {@link HttpResponse}.
     * @param bodyBytes The response body as a byte array.
     */
    protected HttpResponse(HttpRequest request, byte[] bodyBytes) {
        this.request = request;
        this.bodyBytes = bodyBytes;
    }

    /**
     * Get the response status code.
     *
     * @return The response status code
     */
    public abstract int getStatusCode();

    /**
     * Lookup a response header with the provider {@link HttpHeaderName}.
     *
     * @param headerName the name of the header to lookup.
     * @return the value of the header, or null if the header doesn't exist in the response.
     */
    public String getHeaderValue(HttpHeaderName headerName) {
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
     * <p>
     * Subclasses should override this method.
     *
     * @return The {@link BinaryData} response body.
     */
    public BinaryData getBody() {
        // We shouldn't create multiple binary data instances for a single stream.
        if (binaryData == null && bodyBytes != null) {
            binaryData = BinaryData.fromBytes(bodyBytes);
        }

        return binaryData;
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
     * Closes the response content stream, if any.
     */
    @Override
    public void close() {
    }
}
