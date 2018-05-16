package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.ResponseError;
import com.microsoft.azure.storage.blob.models.ResponseErrorException;
import com.microsoft.azure.storage.blob.models.StorageErrorCode;
import com.microsoft.rest.v2.RestException;

/**
 * A {@code StorageException} is thrown whenever Azure Storage successfully returns an error code that is not 200-level.
 * Users can inspect the status code and error code to determine the cause of the error response. The exception message
 * may also contain more detailed information depending on the type of error. The user may also inspect the raw HTTP
 * response.
 * Note that even some expected "errors" will be thrown as a {@code StorageException}. For example, some users may
 * perform a getProperties request on an entity to determine whether it exists or not. If it does not exists, an
 * exception will be thrown even though this may be considered an expected indication of absence in this case.
 */
public final class StorageException extends RestException{

    StorageException(ResponseErrorException e) {
        super(e.getMessage(), e.response(), e.body());
    }

    /**
     * @return
     *      The error code returned by the service.
     */
    public StorageErrorCode errorCode() {
        return StorageErrorCode.fromString(super.response().headers().value(Constants.HeaderConstants.ERROR_CODE));
    }

    /**
     * @return
     *      The status code on the response.
     */
    public int statusCode() {
        return super.response().statusCode();
    }
}
