// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.exception;

import com.azure.core.exception.HttpResponseException;
import com.azure.storage.file.models.StorageErrorCode;
import com.azure.storage.file.models.StorageErrorException;

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
    private final String message;

    StorageException(StorageErrorException e, String responseBody) {
        super(e.getMessage(), e.response(), e);
        this.message = responseBody;
    }

    /**
     * @return The error code returned by the service.
     */
    public StorageErrorCode errorCode() {
        return StorageErrorCode.fromString(super.response().headers().value(ERROR_CODE));
    }

    /**
     * @return The message returned by the service.
     */
    public String message() {
        return this.message;
    }

    /**
     * @return The status code on the response.
     */
    public int statusCode() {
        return super.response().statusCode();
    }
}
