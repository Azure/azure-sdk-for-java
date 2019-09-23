package com.azure.storage.blob.specialized;

import com.azure.core.http.rest.BatchOperation;
import com.azure.core.http.rest.BatchResult;
import com.azure.core.http.rest.Response;
import reactor.core.publisher.Mono;

public class BlobBatchOperation<T> implements BatchOperation<T> {
    private final Mono<? extends Response> response;

    BlobBatchOperation(Mono<? extends Response> response) {
        this.response = response;
    }

    @Override
    public T getValue(BatchResult response) {
        return response.getValue(this);
    }

    @Override
    public Response<T> getRawResponse(BatchResult response) {
        return response.getRawResponse(this);
    }
}
