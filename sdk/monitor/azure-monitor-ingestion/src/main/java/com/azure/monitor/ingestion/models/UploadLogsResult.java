package com.azure.monitor.ingestion.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 *
 */
@Immutable
public final class UploadLogsResult {

    private final List<UploadLogsError> errors;
    private final UploadLogsStatus status;

    public UploadLogsResult(UploadLogsStatus status, List<UploadLogsError> errors) {
        this.status = status;
        this.errors = errors;
    }

    /**
     * @return
     */
    public UploadLogsStatus getStatus() {
        return status;
    }

    /**
     * @return
     */
    public List<UploadLogsError> getErrors() {
        return errors;
    }
}
