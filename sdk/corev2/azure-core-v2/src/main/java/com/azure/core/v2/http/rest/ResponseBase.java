// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.v2.http.rest;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.binarydata.ByteArrayBinaryData;
import io.clientcore.core.util.binarydata.StringBinaryData;

import java.io.IOException;

/**
 * The response of a REST request.
 *
 * @param <H> The deserialized type of the response headers.
 * @param <T> The deserialized type of the response value, available from {@link Response#getValue()}.
 */
public class ResponseBase<H, T> implements Response<T> {
    private final HttpRequest request;
    private final int statusCode;
    private final H deserializedHeaders;
    private final HttpHeaders headers;
    private final T value;
    private BinaryData body;

    /**
     * Creates a {@link ResponseBase}.
     *
     * @param request The HTTP request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param headers The headers of the HTTP response.
     * @param deserializedHeaders The deserialized headers of the HTTP response.
     * @param value The deserialized value of the HTTP response.
     */
    public ResponseBase(HttpRequest request, int statusCode, HttpHeaders headers, T value, H deserializedHeaders) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.deserializedHeaders = deserializedHeaders;
        this.value = value;
    }

    /**
     * Gets The request which resulted in this {@link ResponseBase}.
     *
     * @return The request which resulted in this {@link ResponseBase}.
     */
    @Override
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Get the headers from the HTTP response, transformed into the header type, {@code H}.
     *
     * @return An instance of header type {@code H}, deserialized from the HTTP response headers.
     */
    public H getDeserializedHeaders() {
        return deserializedHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public T getValue() {
        return (T) bodyToValue(getBody());
    }

    @Override
    public BinaryData getBody() {
        if (body == null) {
            if (value == null) {
                body = BinaryData.empty();
            } else if (value instanceof BinaryData) {
                body = (BinaryData) value;
            } else {
                body = BinaryData.fromObject(value);
            }
        }
        return body;
    }

    @Override
    public void close() throws IOException {

    }

    /**
     * Converts the {@link BinaryData} to the value type.
     *
     * @param data The {@link BinaryData} to convert.
     * @return The value type.
     */
    public Object bodyToValue(BinaryData data) {
        if (data instanceof ByteArrayBinaryData) {
            ByteArrayBinaryData byteArray = (ByteArrayBinaryData) data;
            return byteArray.toStream();
        }
        if (data instanceof StringBinaryData) {
            StringBinaryData string = (StringBinaryData) data;
            return string.toString();
        }

        return data;
    }
}
