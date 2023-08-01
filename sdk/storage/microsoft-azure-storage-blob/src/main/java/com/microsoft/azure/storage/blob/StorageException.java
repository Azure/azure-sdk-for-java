// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.StorageErrorCode;
import com.microsoft.azure.storage.blob.models.StorageErrorException;
import com.microsoft.rest.v2.RestException;

/**
 * A {@code StorageException} is thrown whenever Azure Storage successfully returns an error code that is not 200-level.
 * Users can inspect the status code and error code to determine the cause of the error response. The exception message
 * may also contain more detailed information depending on the type of error. The user may also inspect the raw HTTP
 * response or call toString to get the full payload of the error response if present.
 * Note that even some expected "errors" will be thrown as a {@code StorageException}. For example, some users may
 * perform a getProperties request on an entity to determine whether it exists or not. If it does not exists, an
 * exception will be thrown even though this may be considered an expected indication of absence in this case.
 *
 * @apiNote ## Sample Code \n
 * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=exception "Sample code for StorageExceptions")] \n
 * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
 */
public final class StorageException extends RestException {

    private final String message;

    StorageException(StorageErrorException e) {
        super(e.getMessage(), e.response(), e);
        if (e.body() != null) {
            this.message = e.body().message();
        } else {
            this.message = null;
        }
    }

    /**
     * @return The error code returned by the service.
     */
    public StorageErrorCode errorCode() {
        return StorageErrorCode.fromString(super.response().headers().value(Constants.HeaderConstants.ERROR_CODE));
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
