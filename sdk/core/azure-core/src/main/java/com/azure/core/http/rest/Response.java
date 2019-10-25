// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;

/**
 * REST response with a strongly-typed content specified.
 *
 * @param <T> The deserialized type of the response content, available from {@link #getValue()}.
 * @see ResponseBase
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
    HttpHeaders getHeaders();

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
}
