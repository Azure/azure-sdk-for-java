// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation.models;

import com.azure.core.http.HttpRequest;

public final class BatchSubRequest {
    private final BatchOperation operation;
    private final HttpRequest httpRequest;

    public BatchSubRequest(BatchOperation operation, HttpRequest httpRequest) {
        this.operation = operation;
        this.httpRequest = httpRequest;
    }

    public BatchOperation getOperation() {
        return operation;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }
}
