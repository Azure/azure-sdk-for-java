// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.exception;

import com.azure.core.http.HttpResponse;

/**
 * The exception thrown when failed to authenticate the client request with status code of 4XX, typically 401
 * unauthorized.
 *
 * A runtime exception indicating request authorization failure caused by one of the following scenarios:
 * <ul>
 * <li>A client did not send the required authorization credentials to access the requested resource, i.e. Authorization
 * HTTP header is missing in the request</li>
 * <li>If the request contains the HTTP Authorization header, then the exception indicates that authorization has been
 * refused for the credentials contained in the request header.</li>
 * </ul>
 */
public class ClientAuthenticationException extends HttpResponseException {

    /**
     * Initializes a new instance of the {@link ClientAuthenticationException} class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response with the authorization failure.
     */
    public ClientAuthenticationException(final String message, final HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the {@link ClientAuthenticationException} class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response with the authorization failure.
     * @param value The deserialized HTTP response value.
     */
    public ClientAuthenticationException(final String message, final HttpResponse response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the {@link ClientAuthenticationException} class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response with the authorization failure.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     */
    public ClientAuthenticationException(final String message, final HttpResponse response, final Throwable cause) {
        super(message, response, cause);
    }
}
