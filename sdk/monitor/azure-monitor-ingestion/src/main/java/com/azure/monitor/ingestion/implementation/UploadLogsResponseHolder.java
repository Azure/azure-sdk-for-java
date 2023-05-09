// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.implementation;

import com.azure.core.annotation.Immutable;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.models.ResponseError;

/**
 * The intermediate response holder for converting exceptions to {@link ResponseError} instances.
 */
@Immutable
public final class UploadLogsResponseHolder {

    private final HttpResponseException exception;
    private final LogsIngestionRequest request;

    public UploadLogsResponseHolder(LogsIngestionRequest request, HttpResponseException ex) {
        this.request = request;
        this.exception = ex;
    }

    public LogsIngestionRequest getRequest() {
        return request;
    }

    public HttpResponseException getException() {
        return exception;
    }
}
