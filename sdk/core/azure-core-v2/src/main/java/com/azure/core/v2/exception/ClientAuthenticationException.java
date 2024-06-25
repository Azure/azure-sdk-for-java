// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.exception;

import io.clientcore.core.http.models.Response;

/**
 * <p>The {@code ClientAuthenticationException} represents an exception thrown when client authentication fails with
 * a status code of 4XX, typically 401 unauthorized.</p>
 *
 * <p>This exception is thrown in the following scenarios:</p>
 *
 * <ul>
 *     <li>The client did not send the required authorization credentials to access the requested resource, i.e., the
 *     Authorization HTTP header is missing in the request.</li>
 *
 *     <li>The request contains the HTTP Authorization header, but authorization has been refused for the credentials
 *     contained in the request header.</li>
 * </ul>
 *
 * @see com.azure.core.exception
 * @see com.azure.core.exception.HttpResponseException
 */
public class ClientAuthenticationException extends HttpResponseException {

    /**
     * Initializes a new instance of the {@link ClientAuthenticationException} class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response with the authorization failure.
     */
    public ClientAuthenticationException(final String message, final Response<?> response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the {@link ClientAuthenticationException} class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response with the authorization failure.
     * @param value The deserialized HTTP response value.
     */
    public ClientAuthenticationException(final String message, final Response<?> response, final Object value) {
        super(message, response, value);
    }

    /**
     * Initializes a new instance of the {@link ClientAuthenticationException} class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response with the authorization failure.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     */
    public ClientAuthenticationException(final String message, final Response<?> response, final Throwable cause) {
        super(message, response, cause);
    }
}
