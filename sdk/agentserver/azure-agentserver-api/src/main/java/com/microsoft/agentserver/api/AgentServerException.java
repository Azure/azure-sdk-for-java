// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import java.util.Objects;

/**
 * Exception thrown when an API operation fails.
 * <p>
 * Carries an HTTP status code and a structured {@link AgentServerError} body that the
 * hosting framework adapter serialises as the standard error envelope
 * ({@code { "error": { "message", "type", "code", "param"?, "details"?, "additionalInfo"? } }})
 * per the API spec.
 */
public class AgentServerException extends Exception {

    private final int statusCode;
    private final AgentServerError error;

    /**
     * Creates an {@code AgentServerException} with the given status code and structured error body.
     *
     * @param statusCode the HTTP status code representing the error
     * @param error      the structured error body (required)
     */
    public AgentServerException(int statusCode, AgentServerError error) {
        super("API error: HTTP " + statusCode + " — " + Objects.requireNonNull(error, "error").message());
        this.statusCode = statusCode;
        this.error = error;
    }

    /**
     * Returns the HTTP status code associated with this exception.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the structured error body, never {@code null}.
     *
     * @return the structured {@link AgentServerError}
     */
    public AgentServerError getError() {
        return error;
    }
}
