// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;

/**
 * REST response with a strongly-typed content specified.
 *
 * @param <T> The deserialized type of the response content, available from {@link #value()}.
 * @see ResponseBase
 */
public interface Response<T> {

    /**
     * Get the HTTP response status code.
     *
     * @return the status code of the HTTP response.
     */
    int statusCode();

    /**
     * Get the headers from the HTTP response.
     *
     * @return an HttpHeaders instance containing the HTTP response headers.
     */
    HttpHeaders headers();

    /**
     * Get the HTTP request which resulted in this response.
     *
     * @return the HTTP request.
     */
    HttpRequest request();

    /**
     * @return the deserialized value of the HTTP response.
     */
    T value();
}
