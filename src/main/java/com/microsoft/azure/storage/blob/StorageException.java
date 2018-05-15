package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.ResponseError;
import com.microsoft.azure.storage.blob.models.ResponseErrorDetail;
import com.microsoft.azure.storage.blob.models.ResponseErrorException;
import com.microsoft.rest.v2.RestException;

public final class StorageException extends RestException{

    public StorageException(ResponseErrorException e) {
        super(e.getMessage(), e.response(), e.body());
    }

    public String errorCode() {
        return super.response().headers().value("x-ms-error-code");
    }

    public ResponseError body() {
        return ((ResponseError) super.body());
    }
}
