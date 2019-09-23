package com.azure.core.http.rest;

import java.util.stream.Stream;

public abstract class BatchResult {
    public abstract Stream<Response<?>> getRawOperationResponses();

    public <T> T getValue(BatchOperation<T> operation) {
        return operation.getValue(this);
    }

    public <T> Response<T> getRawResponse(BatchOperation<T> operation) {
        return operation.getRawResponse(this);
    }
}
