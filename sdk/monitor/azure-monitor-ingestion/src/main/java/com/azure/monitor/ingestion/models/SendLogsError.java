package com.azure.monitor.ingestion.models;

import com.azure.core.models.ResponseError;

import java.util.List;

/**
 *
 */
public class SendLogsError {
    private ResponseError responseError;
    private List<Integer> failedLogsIndex;

    /**
     * @return
     */
    public ResponseError getResponseError() {
        return responseError;
    }

    /**
     * @param responseError
     */
    public void setResponseError(ResponseError responseError) {
        this.responseError = responseError;
    }

    /**
     * @return
     */
    public List<Integer> getFailedLogsIndex() {
        return failedLogsIndex;
    }

    /**
     * @param failedLogsIndex
     */
    public void setFailedLogsIndex(List<Integer> failedLogsIndex) {
        this.failedLogsIndex = failedLogsIndex;
    }
}
