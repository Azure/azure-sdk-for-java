// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.http.Response;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;

import java.io.Closeable;
import java.io.IOException;

/**
 * The response of an {@link HttpRequest}.
 */
public class HttpResponse implements Response<BinaryData>, Closeable {
    private final BinaryData value;
    private final Headers headers;
    private final HttpRequest request;
    private final int statusCode;

    /**
     * Creates an instance of {@link HttpResponse}.
     *
     * @param request The {@link HttpRequest} that resulted in this {@link HttpResponse}.
     * @param value The response body as a byte array.
     */
    public HttpResponse(HttpRequest request, int statusCode, Headers headers, BinaryData value) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.value = value;
    }

    /**
     * Get the response status code.
     *
     * @return The response status code.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Get all response {@link Headers}.
     *
     * @return The response {@link Headers}.
     */
    public Headers getHeaders() {
         return this.headers;
    }

    /**
     * Gets the {@link HttpRequest request} which resulted in this response.
     *
     * @return The {@link HttpRequest request} which resulted in this response.
     */
    public final HttpRequest getRequest() {
        return this.request;
    }

    /**
     * Gets the deserialized value of this response.
     *
     * @return The deserialized value of this response.
     */
    public BinaryData getValue() {
        return this.value;
    }

    /**
     * Gets the {@link BinaryData} that represents the body of the response.
     *
     * @return The {@link BinaryData} response body.
     */
    public BinaryData getBody() {
        return getValue();
    }

    /**
     * Closes the response content stream, if any.
     */
    @Override
    public void close() throws IOException {
    }
}
