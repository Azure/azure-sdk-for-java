// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import static com.azure.storage.common.implementation.Constants.HeaderConstants.ERROR_CODE;

/**
 * exceptions for the Tables SDK
 */
public class TableStorageException extends HttpResponseException {

    /**
     * Constructs a {@code BlobStorageException}.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the error code of the exception.
     */
    public TableStorageException(String message, HttpResponse response, Object value) {
        super(message, response, value);
    }

    /**
     * @return The error code returned by the service.
     */
    public TableErrorCode getErrorCode() {
        return TableErrorCode.fromString(super.getResponse().getHeaders().getValue(ERROR_CODE));
    }

    /**
     * @return The message returned by the service.
     */
    public String getServiceMessage() {
        return super.getMessage();
    }

    /**
     * @return The status code on the response.
     */
    public int getStatusCode() {
        return super.getResponse().getStatusCode();
    }
}
