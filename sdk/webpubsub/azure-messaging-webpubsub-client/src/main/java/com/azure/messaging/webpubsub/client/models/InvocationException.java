// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.exception.AzureException;

/**
 * The exception thrown when an invocation fails or is cancelled.
 */
public final class InvocationException extends AzureException {

    /**
     * The invocation ID of the request.
     */
    private final String invocationId;

    /**
     * The error details from the service, if available.
     */
    private final AckResponseError errorDetail;

    /**
     * Creates a new instance of InvocationException.
     *
     * @param message the error message.
     * @param invocationId the invocation ID of the request.
     * @param errorDetail the error details from the service.
     */
    public InvocationException(String message, String invocationId, AckResponseError errorDetail) {
        super(message);
        this.invocationId = invocationId;
        this.errorDetail = errorDetail;
    }

    /**
     * Creates a new instance of InvocationException.
     *
     * @param message the error message.
     * @param cause the cause of the exception.
     * @param invocationId the invocation ID of the request.
     * @param errorDetail the error details from the service.
     */
    public InvocationException(String message, Throwable cause, String invocationId, AckResponseError errorDetail) {
        super(message, cause);
        this.invocationId = invocationId;
        this.errorDetail = errorDetail;
    }

    /**
     * Gets the invocation ID of the request.
     *
     * @return the invocation ID.
     */
    public String getInvocationId() {
        return invocationId;
    }

    /**
     * Gets the error details from the service, if available.
     *
     * @return the error details.
     */
    public AckResponseError getErrorDetail() {
        return errorDetail;
    }
}
