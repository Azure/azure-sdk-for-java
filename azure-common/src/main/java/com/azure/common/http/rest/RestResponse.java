/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.common.http.rest;

import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpRequest;

/**
 * REST response with a strongly-typed content specified.
 *
 * @param <T> The deserialized type of the response content.
 * @see RestResponseBase
 */
public interface RestResponse<T> {

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
     * @return the deserialized body of the HTTP response.
     */
    T body();
}
