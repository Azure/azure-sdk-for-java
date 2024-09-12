// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake.implementation.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * A {@code DatalakeStorageExceptionInternal} is thrown whenever Azure Storage successfully returns an error code that
 * is not 200-level. Users can inspect the status code and error code to determine the cause of the error response. The
 * exception message may also contain more detailed information depending on the type of error. The user may also
 * inspect the raw HTTP response or call toString to get the full payload of the error response if present. Note that
 * even some expected "errors" will be thrown as a {@code DatalakeStorageExceptionInternal}. For example, some users may
 * perform a getProperties request on an entity to determine whether it exists or not. If it does not exists, an
 * exception will be thrown even though this may be considered an expected indication of absence in this case.
 */
public final class DataLakeStorageExceptionInternal extends HttpResponseException {
    /**
     * Constructs a {@code DatalakeStorageExceptionInternal}.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the deserialized error response.
     */
    public DataLakeStorageExceptionInternal(String message, HttpResponse response, DataLakeStorageError value) {
        super(StorageImplUtils.convertStorageExceptionMessage(message, response), response, value);
    }

    @Override
    public DataLakeStorageError getValue() {
        return (DataLakeStorageError) super.getValue();
    }
}
