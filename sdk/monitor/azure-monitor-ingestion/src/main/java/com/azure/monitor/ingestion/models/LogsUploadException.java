// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.models;

import com.azure.core.exception.AzureException;
import com.azure.core.exception.HttpResponseException;

import java.util.List;

/**
 * An aggregate exception containing all inner exceptions that were caused from uploading logs.
 */
public class LogsUploadException extends AzureException {
    /**
     * Total count of all logs that were not uploaded to Azure Monitor due to errors.
     */
    private final long failedLogsCount;
    /**
     * A list of all HTTP errors that occured when uploading logs to Azure Monitor service.
     */
    private final List<HttpResponseException> logsUploadErrors;

    /**
     * Creates an instance of {@link LogsUploadException}.
     * @param logsUploadErrors list of all HTTP response exceptions.
     * @param failedLogsCount the total number of logs that failed to upload.
     */
    public LogsUploadException(List<HttpResponseException> logsUploadErrors, long failedLogsCount) {
        this.logsUploadErrors = logsUploadErrors;
        this.failedLogsCount = failedLogsCount;
    }

    /**
     * Returns the list of all HTTP response exceptions.
     * @return The list of all errors.
     */
    public List<HttpResponseException> getLogsUploadErrors() {
        return this.logsUploadErrors;
    }

    /**
     * Returns the total number for logs that failed to upload.
     * @return the total number of logs that failed to upload.
     */
    public long getFailedLogsCount() {
        return failedLogsCount;
    }
}
