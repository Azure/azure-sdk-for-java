/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/**
 * Exception thrown for an invalid response with ExportResult information.
 */
public final class ExportResultException extends HttpResponseException {
    /**
     * Initializes a new instance of the ExportResultException class.
     *
     * @param message  the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     */
    public ExportResultException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the ExportResultException class.
     *
     * @param message  the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value    the deserialized response value.
     */
    public ExportResultException(String message, HttpResponse response, ExportResult value) {
        super(message, response, value);
    }

    @Override
    public ExportResult getValue() {
        return (ExportResult) super.getValue();
    }
}
