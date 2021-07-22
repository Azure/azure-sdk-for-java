// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation.models;

import com.azure.core.http.HttpRequest;

public final class TransactionalBatchSubRequest {
    private final TransactionalBatchAction operation;
    private final HttpRequest httpRequest;

    public TransactionalBatchSubRequest(TransactionalBatchAction operation, HttpRequest httpRequest) {
        this.operation = operation;
        this.httpRequest = httpRequest;
    }

    public TransactionalBatchAction getOperation() {
        return operation;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }
}
