// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The model class containing the result of a logs upload operation.
 */
@Immutable
public final class UploadLogsResult {
    private final List<UploadLogsError> errors;
    private final UploadLogsStatus status;

    /**
     * Creates an instance of {@link UploadLogsOptions}.
     * @param status the status of the logs upload operation.
     * @param errors the list of errors that occurred when uploading logs, if any.
     */
    public UploadLogsResult(UploadLogsStatus status, List<UploadLogsError> errors) {
        this.status = status;
        this.errors = errors;
    }

    /**
     * Returns the status of the logs upload operation.
     * @return the status of the logs upload operation.
     */
    public UploadLogsStatus getStatus() {
        return status;
    }

    /**
     * Returns the list of errors that occurred when uploading logs, if any.
     * @return the list of errors that occurred when uploading logs, if any.
     */
    public List<UploadLogsError> getErrors() {
        return errors;
    }
}
