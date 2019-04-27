// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.exception;

/**
 * A runtime exception indicating service response failure caused by one of the following scenarios:
 * Service is not able to handle a well-format request by some reasons.
 * OR The request was sent, but the client failed to understand the response.
 *
 * These errors may not be safe to retry.
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
     * @param message the exception message or the response content if a message is not available
     * @param cause the Throwable which caused the creation of this ServiceRequestException
     */
    public ServiceResponseException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
