// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

/**
 *  The server did not send any data in the allotted amount of time.
 *  These errors may not be safe to retry.
 */
public class ReadTimeoutException extends ServiceResponseException {

    /**
     * Initializes a new instance of the ReadTimeoutException class.
     *
     * @param message the exception message or the response content if a message is not available
     */
    public ReadTimeoutException(final String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the ReadTimeoutException class.
     *
     * @param message the exception message or the response content if a message is not available
     * @param cause the Throwable which caused the creation of this ReadTimeoutException
     */
    public ReadTimeoutException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
