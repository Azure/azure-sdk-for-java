// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/**
 * A {@code StorageException} is thrown whenever Azure Storage successfully returns an error code that is not 200-level.
 * Users can inspect the status code and error code to determine the cause of the error response. The exception message
 * may also contain more detailed information depending on the type of error. The user may also inspect the raw HTTP
 * response or call toString to get the full payload of the error response if present.
 * Note that even some expected "errors" will be thrown as a {@code StorageException}. For example, some users may
 * perform a getProperties request on an entity to determine whether it exists or not. If it does not exists, an
 * exception will be thrown even though this may be considered an expected indication of absence in this case.
 *
 * <p><strong>Sample Code</strong></p>
 * <p>For more samples, please see the <a href="https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java">sample file</a></p>
 */
public final class StorageException extends HttpResponseException {
    private static final String ERROR_CODE = "x-ms-error-code";
    /**
     * Constructs a {@code StorageException} from the given {@link StorageErrorException}.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the error code of the exception.
     */
    public StorageException(String message, HttpResponse response, Object value) {
        super(message, response, value);
    }

    /**
     * @return The error code returned by the service.
     */
    public StorageErrorCode getErrorCode() {
        return StorageErrorCode.fromString(super.getResponse().getHeaders().getValue(ERROR_CODE));
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
