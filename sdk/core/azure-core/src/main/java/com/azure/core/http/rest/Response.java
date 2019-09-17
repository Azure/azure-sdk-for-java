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
     * Get the HTTP response status code.
     *
     * @return the status code of the HTTP response.
     */
    int getStatusCode();

    /**
     * Get the headers from the HTTP response.
     *
     * @return an HttpHeaders instance containing the HTTP response headers.
     */
    HttpHeaders getHeaders();

    /**
     * Get the HTTP request which resulted in this response.
     *
     * @return the HTTP request.
     */
    HttpRequest getRequest();

    /**
     * @return the deserialized value of the HTTP response.
     */
    T getValue();
}
