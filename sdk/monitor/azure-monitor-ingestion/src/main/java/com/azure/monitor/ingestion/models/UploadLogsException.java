// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.models;

import com.azure.core.exception.AzureException;
import com.azure.core.exception.HttpResponseException;

import java.util.List;

/**
 * An aggregate exception containing all inner exceptions that were caused from uploading logs.
 */
public class UploadLogsException extends AzureException {
    /**
     * Total count of all logs that were not uploaded to Azure Monitor due to errors.
     */
    private final long failedLogsCount;
    /**
     * A list of all HTTP errors that occured when uploading logs to Azure Monitor service.
     */
    private final List<HttpResponseException> uploadLogsErrors;

    /**
     * Creates an instance of {@link UploadLogsException}.
     * @param uploadLogsErrors list of all HTTP response exceptions.
     * @param failedLogsCount the total number of logs that failed to upload.
     */
    public UploadLogsException(List<HttpResponseException> uploadLogsErrors, long failedLogsCount) {
        this.uploadLogsErrors = uploadLogsErrors;
        this.failedLogsCount = failedLogsCount;
    }

    /**
     * Returns the list of all HTTP response exceptions.
     * @return The list of all errors.
     */
    public List<HttpResponseException> getUploadLogsErrors() {
        return this.uploadLogsErrors;
    }

    /**
     * Returns the total number for logs that failed to upload.
     * @return the total number of logs that failed to upload.
     */
    public long getFailedLogsCount() {
        return failedLogsCount;
    }
}
