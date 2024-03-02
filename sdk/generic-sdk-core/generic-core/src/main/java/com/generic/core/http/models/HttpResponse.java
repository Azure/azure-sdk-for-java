// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.http.Response;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;

/**
 * The response of an {@link HttpRequest}.
 */
public class HttpResponse<T> implements Response<T>, Closeable {
    protected static final BinaryData EMPTY_BODY = BinaryData.fromBytes(new byte[0]);

    private final Headers headers;
    private final HttpRequest request;
    private final int statusCode;

    private BinaryData body;
    private Function<BinaryData, Object> deserializationCallback;
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
     * Creates an instance of {@link HttpResponse}.
     *
     * @param request The {@link HttpRequest} that resulted in this {@link HttpResponse}.
     * @param statusCode The response status code.
     * @param headers The response {@link Headers}.
     * @param value The response body.
     * @param isRawResponse Indicates whether the value provided needs to be deserialized.
     */
    @SuppressWarnings("unchecked")
    public HttpResponse(HttpRequest request, int statusCode, Headers headers, BinaryData value, boolean isRawResponse) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;

        if (isRawResponse) {
            this.body = value;
        } else {
            this.value = (T) value;
        }
    }

    /**
     * Creates an instance of {@link HttpResponse}.
     *
     * @param request The {@link HttpRequest} that resulted in this {@link HttpResponse}.
     * @param statusCode The response status code.
     * @param headers The response {@link Headers}.
     * @param body A {@link BinaryData} holding the response's raw body.
     * @param deserializationCallback A {@link Function} to handle the raw body's deserialization. This callback will
     * be called the first time {@link #getValue()} is called.
     */
    public HttpResponse(HttpRequest request, int statusCode, Headers headers, BinaryData body,
                        Function<BinaryData, Object> deserializationCallback) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
        this.deserializationCallback = deserializationCallback;
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
     * Gets the deserialized value of this response.
     *
     * @return The deserialized value of this response.
     */
    @SuppressWarnings("unchecked")
    public T getDecodedBody() {
        T decodedBody = null;

        if (value == null && body != null) {
            decodedBody = (T) deserializationCallback.apply(body);
        } else if (value instanceof BinaryData) {
            decodedBody = (T) deserializationCallback.apply((BinaryData) value);
        }

        return decodedBody;
    }

    /**
     * Set a value for this response.
     *
     * @param decodedBody The response value.
     */
    @SuppressWarnings("unchecked")
    public void setDecodedBody(Object decodedBody) {
        this.value = (T) decodedBody;
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

    public void setDeserializationCallback(Function<BinaryData, Object> deserializationCallback) {
        this.deserializationCallback = deserializationCallback;
    }

    /**
     * Closes the response content stream, if any.
     */
    @Override
    public void close() throws IOException {
    }
}
