// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/**
 * Exception thrown when a query to retrieve logs fails.
 */
public final class LogsQueryException extends HttpResponseException {
    private final transient LogsQueryError error;

    /**
     * Creates a new instance of this exception with the {@link HttpResponse} and {@link LogsQueryErrorDetail error}
     * information.
     * @param response The {@link HttpResponse}.
     * @param error The {@link LogsQueryError error} details.
     */
    public LogsQueryException(HttpResponse response, LogsQueryError error) {
        super("Failed to execute logs query", response, error);
        this.error = error;
    }

    @Override
    public LogsQueryError getValue() {
        return this.error;
    }
}
