// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.util.binarydata.BinaryData;

import java.io.Closeable;

/**
 * REST response with a strongly-typed content specified.
 *
 * @param <T> The deserialized type of the response content, available from {@link #getValue()}.
 */
public interface Response<T> extends Closeable {
    /**
     * Gets the response status code.
     *
     * @return The status code of the response.
     */
    int getStatusCode();

    /**
     * Gets the headers from the response.
     *
     * @return The response headers.
     */
    HttpHeaders getHeaders();

    /**
     * Gets the request which resulted in this response.
     *
     * @return The request.
     */
    HttpRequest getRequest();

    /**
     * Gets the deserialized value of this response.
     *
     * @return The deserialized value of this response.
     */
    T getValue();

    /**
     * Gets the {@link BinaryData} that represents the raw body of the response.
     *
     * @return The {@link BinaryData} containing the response body.
     */
    BinaryData getBody();

    /**
     * A static method that creates a default {@link Response} implementation.
     *
     * @param <T> The deserialized type of the response content, available from {@link #getValue()}.
     * @param request The request which resulted in this response.
     * @param statusCode The response status code.
     * @param headers The response headers.
     * @param value The deserialized value of the response.
     *
     * @return A default {@link Response} implementation.
     */
    static <T> Response<T> create(HttpRequest request, int statusCode, HttpHeaders headers, T value) {
        return new HttpResponse<>(request, statusCode, headers, value);
    }
}
