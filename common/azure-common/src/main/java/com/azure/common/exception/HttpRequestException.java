// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

import com.azure.common.http.HttpResponse;

/**
 * The exception thrown when received unsuccessful response with http status code (e.g. 4XX, 5XX) from the service request.
 *
 * @see ClientRequestException
 * @see ServerException
 * @see TooManyRedirectsException
 */
public class HttpRequestException extends ServiceRequestException {

    /**
     * The http status code which indicates the http exception type
     */
    private int httpStatus;

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public HttpRequestException(String message, HttpResponse response) {
        super(message, response);
        this.httpStatus = response.statusCode();
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public HttpRequestException(String message, HttpResponse response, Object value) {
        super(message, response, value);
        this.httpStatus = response.statusCode();
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this HttpRequestException
     */
    public HttpRequestException(String message, HttpResponse response, Throwable cause) {
        super(message, response, cause);
        this.httpStatus = response.statusCode();
    }

    /**
     * Initializes a new instance of the HttpRequestException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param httpStatus the HTTP response status code
     */
    public HttpRequestException(String message, HttpResponse response, int httpStatus) {
        super(message, response);
        this.httpStatus = httpStatus;
    }

    /**
     * @return the HTTP response status code
     */
    public int getHttpStatus() {
        return httpStatus;
    }
}
