// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

import com.azure.common.http.HttpResponse;

/**
 * The exception thrown when failed to authenticate the client request with status code of 4XX.
 *
 * A runtime exception indicating request authorization failure caused by one of the following scenarios:
 * A client did not send the required authorization credentials to access the requested resource, i.e. Authorization HTTP header is missing in the request,
 * OR - In case the request already contains the HTTP Authorization header - then the exception indicates that authorization has been refused for the credentials contained in the request header.
 */
public class ClientAuthenticationException extends ClientRequestException {

    /**
     * Initializes a new instance of the ClientAuthenticationException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     */
    public ClientAuthenticationException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the ClientAuthenticationException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param value the deserialized response value
     */
    public ClientAuthenticationException(String message, HttpResponse response, Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the ClientAuthenticationException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param cause the Throwable which caused the creation of this ClientAuthenticationException
     */
    public ClientAuthenticationException(String message, HttpResponse response, Throwable cause) {
        super(message, response, cause);
    }

    /**
     * Initializes a new instance of the ClientAuthenticationException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param response the HTTP response
     * @param httpStatus the HTTP response status code
     */
    public ClientAuthenticationException(final String message, final HttpResponse response, final int httpStatus) {
        super(message, response, httpStatus);
    }
}
