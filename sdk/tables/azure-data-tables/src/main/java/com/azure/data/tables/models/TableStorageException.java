// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import static com.azure.storage.common.implementation.Constants.HeaderConstants.ERROR_CODE;

/**
 * A {@code TableStorageException} is thrown whenever the Tables service successfully returns an error code that is not
 * 200-level. Users can inspect the status code and error code to determine the cause of the error response. The
 * exception message may also contain more detailed information depending on the type of error. The user may also
 * inspect the raw HTTP response or call {@link #toString()} to get the full payload of the error response if present.
 */
public class TableStorageException extends HttpResponseException {

    /**
     * Constructs a {@code TableStorageException}.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The {@link HttpResponse}.
     * @param value The error code of the exception.
     */
    public TableStorageException(String message, HttpResponse response, Object value) {
        super(message, response, value);
    }

    /**
     * Gets the error code returned by the service.
     *
     * @return The error code returned by the service.
     */
    public TableErrorCode getErrorCode() {
        return TableErrorCode.fromString(super.getResponse().getHeaders().getValue(ERROR_CODE));
    }

    /**
     * Gets the message returned by the service.
     *
     * @return The message returned by the service.
     */
    public String getServiceMessage() {
        return super.getMessage();
    }

    /**
     * Gets the status code of the response.
     *
     * @return The status code of the response.
     */
    public int getStatusCode() {
        return super.getResponse().getStatusCode();
    }
}
