// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.implementation;

/**
 * Exception thrown when a Foundry storage API operation fails.
 * <p>
 * Contains the HTTP status code and response body for diagnostic purposes.
 */
public final class FoundryStorageException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    /**
     * Creates a new {@code FoundryStorageException}.
     *
     * @param message      the exception message
     * @param statusCode   the HTTP status code returned by the storage API
     * @param responseBody the response body, or empty string if unavailable
     */
    public FoundryStorageException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody != null ? responseBody : "";
    }

    /**
     * Creates a new {@code FoundryStorageException} with a cause.
     *
     * @param message the exception message
     * @param cause   the underlying cause
     */
    public FoundryStorageException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.responseBody = "";
    }

    /**
     * Returns the HTTP status code from the failed storage API call.
     *
     * @return the status code, or 0 if not applicable
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the response body from the failed storage API call.
     *
     * @return the response body, or an empty string
     */
    public String getResponseBody() {
        return responseBody;
    }
}

