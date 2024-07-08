// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.exception;

/**
 * <p>The {@code ServiceResponseException} represents an exception thrown when the client fails to understand the
 * service response or the connection times out.</p>
 *
 * <p>This exception is typically thrown in the following scenarios:</p>
 *
 * <ul>
 *     <li>The request was sent, but the client failed to understand the response. This could be due to the response
 *     not being in the expected format, or only a partial response was received.</li>
 *
 *     <li>The connection may have timed out. These errors can be retried for idempotent or safe operations.</li>
 * </ul>
 *
 * @see com.azure.core.exception
 * @see com.azure.core.exception.AzureException
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
