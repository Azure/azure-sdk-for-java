package com.azure.monitor.ingestion.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.models.ResponseError;

import java.util.List;

/**
 *
 */
@Immutable
public class UploadLogsError {
    private ResponseError responseError;
    private List<Object> failedLogs;

    public UploadLogsError(ResponseError error, List<Object> failedLogs) {
        this.responseError = error;
        this.failedLogs = failedLogs;
    }

    /**
     * @return
     */
    public ResponseError getResponseError() {
        return responseError;
    }

    /**
     * @return
     */
    public List<Object> getFailedLogs() {
        return failedLogs;
    }
}
