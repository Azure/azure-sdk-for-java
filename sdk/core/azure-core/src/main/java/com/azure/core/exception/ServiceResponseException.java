// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.exception;

/**
 * A runtime exception indicating service response failure caused by one of the following scenarios:
 *
 * <ol>
 * <li>The request was sent, but the client failed to understand the response. (Not in the right format, partial
 * response, etc.).</li>
 * <li>The connection may have timed out. These errors can be retried for idempotent or safe operations.</li>
 * </ol>
 */
public class ServiceResponseException extends AzureException {

    /**
     * Initializes a new instance of the ServiceResponseException class.
     *
     * @param message the exception message or the response content if a message is not available
     */
    public ServiceResponseException(final String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the ServiceResponseException class.
     *
     * @param message the exception message.
     * @param cause the Throwable which caused the creation of this ServiceResponseException.
     */
    public ServiceResponseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
