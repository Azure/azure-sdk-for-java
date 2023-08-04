// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

public class BatchErrorException extends HttpResponseException {
    /**
     * Initializes a new instance of the BatchErrorException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     */
    public BatchErrorException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Initializes a new instance of the BatchErrorException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the deserialized response value.
     */
    public BatchErrorException(String message, HttpResponse response, BatchError value) {
        super(message, response, value);
    }

    @Override
    public BatchError getValue() {
        return (BatchError) super.getValue();
    }
}
