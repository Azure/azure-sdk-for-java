// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/** Exception thrown for an invalid response with ExportResult information. */
public final class ExportResultException extends HttpResponseException {
    /**
     * Initializes a new instance of the ExportResultException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     */
    public ExportResultException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the ExportResultException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the deserialized response value.
     */
    public ExportResultException(String message, HttpResponse response, ExportResult value) {
        super(message, response, value);
    }

    @Override
    public ExportResult getValue() {
        return (ExportResult) super.getValue();
    }
}
