// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms.models;

import com.azure.core.annotation.Immutable;

/**
 * Represents the result of checking opt-out status for a specific recipient.
 */
@Immutable
public final class OptOutCheckResult {
    private final String to;
    private final int httpStatusCode;
    private final Boolean isOptedOut;
    private final String errorMessage;

    /**
     * Creates an instance of OptOutCheckResult.
     *
     * @param to The recipient phone number (in E.164 format).
     * @param httpStatusCode The HTTP status code of the operation.
     * @param isOptedOut Whether the number is opted out from receiving messages.
     * @param errorMessage Optional error message in case of 4xx/5xx errors.
     */
    public OptOutCheckResult(String to, int httpStatusCode, Boolean isOptedOut, String errorMessage) {
        this.to = to;
        this.httpStatusCode = httpStatusCode;
        this.isOptedOut = isOptedOut;
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the recipient phone number.
     *
     * @return The recipient phone number (in E.164 format).
     */
    public String getTo() {
        return to;
    }

    /**
     * Gets the HTTP status code of the operation.
     *
     * @return The HTTP status code.
     */
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * Gets whether the number is opted out from receiving messages.
     *
     * @return True if the number is opted out, false otherwise, or null if the status is unknown.
     */
    public Boolean isOptedOut() {
        return isOptedOut;
    }

    /**
     * Gets the error message if the operation failed.
     *
     * @return The error message, or null if the operation was successful.
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
