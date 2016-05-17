/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.microsoft.rest.RestException;
import retrofit2.Response;

/**
 * Exception thrown for an invalid response with BatchError information.
 */
public class BatchErrorException extends RestException {
    /**
     * Information about the associated HTTP response.
     */
    private Response response;
    /**
     * The actual response body.
     */
    private BatchError body;
    /**
     * Initializes a new instance of the BatchErrorException class.
     */
    public BatchErrorException() { }
    /**
     * Initializes a new instance of the BatchErrorException class.
     *
     * @param message The exception message.
     */
    public BatchErrorException(final String message) {
        super(message);
    }
    /**
     * Initializes a new instance of the BatchErrorException class.
     *
     * @param message the exception message
     * @param cause   exception that caused this exception to occur
     */
    public BatchErrorException(final String message, final Throwable cause) {
        super(message, cause);
    }
    /**
     * Initializes a new instance of the BatchErrorException class.
     *
     * @param cause exception that caused this exception to occur
     */
    public BatchErrorException(final Throwable cause) {
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
    public BatchError getBody() {
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
    public void setBody(BatchError body) {
        this.body = body;
    }
}
