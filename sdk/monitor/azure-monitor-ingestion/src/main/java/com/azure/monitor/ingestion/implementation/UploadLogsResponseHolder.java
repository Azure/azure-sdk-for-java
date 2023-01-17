// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.implementation;

import com.azure.core.annotation.Immutable;
import com.azure.core.models.ResponseError;

/**
 * The intermediate response holder for converting exceptions to {@link ResponseError} instances.
 */
@Immutable
public final class UploadLogsResponseHolder {
    private String status;
    private ResponseError responseError;

    /**
     * Creates and instance of {@link UploadLogsResponseHolder}.
     * @param status The status of the logs upload request.
     * @param responseError The error details of the upload request, if any.
     */
    public UploadLogsResponseHolder(String status, ResponseError responseError) {
        this.status = status;
        this.responseError = responseError;
    }

    /**
     * Returns the status of the logs upload request.
     * @return the status of the logs upload request.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the error details of the upload request, if any.
     * @return the error details of the upload request, if any.
     */
    public ResponseError getResponseError() {
        return responseError;
    }
}
