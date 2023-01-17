// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.models;

import com.azure.core.exception.AzureException;

import java.util.List;

/**
 * An aggregate exception containing all inner exceptions that were caused from uploading logs.
 */
public class UploadLogsException extends AzureException {
    private List<UploadLogsError> uploadLogsErrors;

    /**
     * Creates an instance of {@link UploadLogsException}.
     * @param uploadLogsErrors list of all errors.
     */
    public UploadLogsException(List<UploadLogsError> uploadLogsErrors) {
        this.uploadLogsErrors = uploadLogsErrors;
    }

    /**
     * Returns the list of all errors.
     * @return The list of all errors.
     */
    public List<UploadLogsError> getUploadLogsErrors() {
        return this.uploadLogsErrors;
    }

}
