/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.implementation.api;

import com.microsoft.rest.RestException;
import retrofit2.Response;

/**
 * Exception thrown for an invalid response with AdlsError information.
 */
public class AdlsErrorException extends RestException {
    /**
     * Information about the associated HTTP response.
     */
    private Response response;
    /**
     * The actual response body.
     */
    private AdlsError body;
    /**
     * Initializes a new instance of the AdlsErrorException class.
     */
    public AdlsErrorException() { }
    /**
     * Initializes a new instance of the AdlsErrorException class.
     *
     * @param message The exception message.
     */
    public AdlsErrorException(final String message) {
        super(message);
    }
    /**
     * Initializes a new instance of the AdlsErrorException class.
     *
     * @param message the exception message
     * @param cause   exception that caused this exception to occur
     */
    public AdlsErrorException(final String message, final Throwable cause) {
        super(message, cause);
    }
    /**
     * Initializes a new instance of the AdlsErrorException class.
     *
     * @param cause exception that caused this exception to occur
     */
    public AdlsErrorException(final Throwable cause) {
        super(cause);
    }
    /**
     * Gets information about the associated HTTP response.
     *
     * @return the HTTP response
     */
    public Response getResponse() {
        return response;
    }
    /**
     * Gets the HTTP response body.
     *
     * @return the response body
     */
    public AdlsError getBody() {
        return body;
    }
    /**
     * Sets the HTTP response.
     *
     * @param response the HTTP response
     */
    public void setResponse(Response response) {
        this.response = response;
    }
    /**
     * Sets the HTTP response body.
     *
     * @param body the response body
     */
    public void setBody(AdlsError body) {
        this.body = body;
    }
}
