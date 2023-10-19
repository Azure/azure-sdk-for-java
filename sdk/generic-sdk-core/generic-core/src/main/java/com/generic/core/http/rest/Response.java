// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.generic.core.http.rest;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.models.Headers;

/**
 * REST response with a strongly-typed content specified.
 *
 * @param <T> The deserialized type of the response content, available from {@link #getValue()}.
 */
public interface Response<T> {

    /**
     * Gets the HTTP response status code.
     *
     * @return The status code of the HTTP response.
     */
    int getStatusCode();

    /**
     * Gets the headers from the HTTP response.
     *
     * @return The HTTP response headers.
     */
    Headers getHeaders();

    /**
     * Gets the HTTP request which resulted in this response.
     *
     * @return The HTTP request.
     */
    HttpRequest getRequest();

    /**
     * Gets the deserialized value of the HTTP response.
     *
     * @return The deserialized value of the HTTP response.
     */
    T getValue();

    @SuppressWarnings({"rawtypes", "unchecked"})
    static Response createResponse(HttpRequest request, int statusCode, Headers headers, Object value) {
        return new SimpleResponse(request, statusCode, headers, value);
    }
}
