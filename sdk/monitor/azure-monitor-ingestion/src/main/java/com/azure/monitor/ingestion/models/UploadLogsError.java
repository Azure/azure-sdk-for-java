// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.models.ResponseError;

import java.util.List;

/**
 * The model representing the error and the associated logs that failed when uploading a subset of logs to Azure
 * Monitor.
 */
@Immutable
public final class UploadLogsError {
    private final ResponseError responseError;
    private final List<Object> failedLogs;

    /**
     * Creates an instance of error.
     * @param error the response error containing the error details returned by the service.
     * @param failedLogs the logs that failed to upload.
     */
    public UploadLogsError(ResponseError error, List<Object> failedLogs) {
        this.responseError = error;
        this.failedLogs = failedLogs;
    }

    /**
     * Returns the response error containing the error details returned by the service.
     * @return the response error containing the error details returned by the service.
     */
    public ResponseError getResponseError() {
        return responseError;
    }

    /**
     * Returns the logs that failed to upload.
     * @return the logs that failed to upload.
     */
    public List<Object> getFailedLogs() {
        return failedLogs;
    }
}
