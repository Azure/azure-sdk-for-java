// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.implementation.http.HttpResponseAccessHelper;
import com.generic.core.models.Headers;
import com.generic.core.util.binarydata.BinaryData;

import java.io.IOException;

/**
 * The response of an {@link HttpRequest}.
 */
public class HttpResponse<T> implements Response<T> {
    private static final BinaryData EMPTY_BODY = BinaryData.fromBytes(new byte[0]);

    static {
        HttpResponseAccessHelper.setAccessor(HttpResponse::setValue);
    }

    private final Headers headers;
    private final HttpRequest request;
    private final int statusCode;

    private BinaryData body;
    private T value;

    /**
     * Creates an instance of {@link HttpResponse}.
     *
     * @param request The {@link HttpRequest} that resulted in this {@link HttpResponse}.
     * @param statusCode The response status code.
     * @param headers The response {@link Headers}.
     * @param value The response body.
     */
    public HttpResponse(HttpRequest request, int statusCode, Headers headers, T value) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.value = value;
    }

    /**
     * Get all response {@link Headers}.
     *
     * @return The response {@link Headers}.
     */
    public final Headers getHeaders() {
         return headers;
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
     * Get the response status code.
     *
     * @return The response status code.
     */
    public final int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the value of this response.
     *
     * @return The value of this response.
     */
    public T getValue() {
        return value;
    }

    /**
     * Gets the {@link BinaryData} that represents the body of the response.
     *
     * @return The {@link BinaryData} containing the response body.
     */
    public BinaryData getBody() {
        if (body == null) {
            if (value == null) {
                body = EMPTY_BODY;
            } else if (value instanceof BinaryData) {
                body = (BinaryData) value;
            } else {
                body = BinaryData.fromObject(value);
            }
        }

        return body;
    }

    /**
     * Sets the value of this response.
     *
     * @param value The value.
     */
    @SuppressWarnings("unchecked")
    private HttpResponse<T> setValue(Object value) {
        this.value = (T) value;

        return this;
    }

    @Override
    public void close() throws IOException {
    }
}
