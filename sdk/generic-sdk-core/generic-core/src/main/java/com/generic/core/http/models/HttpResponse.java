// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.http.Response;
import com.generic.core.implementation.http.HttpResponseAccessHelper;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The response of an {@link HttpRequest}.
 */
public class HttpResponse<T> implements Response<T> {
    private static final BinaryData EMPTY_BODY = BinaryData.fromBytes(new byte[0]);

    private boolean isValueDeserialized = false;

    static {
        HttpResponseAccessHelper.setAccessor(new HttpResponseAccessHelper.HttpResponseAccessor() {
            @Override
            public HttpResponse<?> setValue(HttpResponse<?> httpResponse, Object value) {
                return httpResponse.setValue(value);
            }

            @Override
            public HttpResponse<?> setBody(HttpResponse<?> httpResponse, BinaryData body) {
                return httpResponse.setBody(body);
            }

            @Override
            public HttpResponse<?> setBodySupplier(HttpResponse<?> httpResponse, Supplier<BinaryData> bodySupplier) {
                return httpResponse.setBodySupplier(bodySupplier);
            }

            @Override
            public HttpResponse<?> setBodyDeserializer(HttpResponse<?> httpResponse,
                                                       Function<BinaryData, Object> bodyDeserializer) {
                return httpResponse.setBodyDeserializer(bodyDeserializer);
            }
        });
    }

    private final Headers headers;
    private final HttpRequest request;
    private final int statusCode;

    private BinaryData body;
    private T value;

    private Supplier<BinaryData> bodySupplier;
    private Function<BinaryData, Object> bodyDeserializer;

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
    @SuppressWarnings("unchecked")
    public T getValue() {
        if (!isValueDeserialized && bodyDeserializer != null) {
            // Deserialize the value
            value = (T) bodyDeserializer.apply(getBody());

            this.isValueDeserialized = true;
        }

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
                body = bodySupplier == null ? EMPTY_BODY : bodySupplier.get();
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

    /**
     * Sets the body of this response.
     *
     * @param body The body.
     */
    private HttpResponse<T> setBody(BinaryData body) {
        this.body = body;

        return this;
    }

    /**
     * Sets the body supplier for this response.
     *
     * @param bodySupplier The body supplier.
     */
    private HttpResponse<T> setBodySupplier(Supplier<BinaryData> bodySupplier) {
        this.bodySupplier = bodySupplier;

        return this;
    }

    /**
     * Sets the body deserializer for this response.
     *
     * @param bodyDeserializer The body deserializer.
     */
    private HttpResponse<T> setBodyDeserializer(Function<BinaryData, Object> bodyDeserializer) {
        this.bodyDeserializer = bodyDeserializer;

        return this;
    }

    @Override
    public void close() throws IOException {
    }
}
