package com.azure.storage.blob.specialized;

import com.azure.core.http.rest.Response;
import reactor.core.publisher.Mono;

public class BlobBatchOperationResponse<T> implements BatchOperation<T> {
    private final Mono<? extends Response> response;
    private int contentId;

    BlobBatchOperationResponse(Mono<? extends Response> response) {
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

    Mono<? extends Response> getResponse() {
        return response;
    }

    void setContentId(int contentId) {
        this.contentId = contentId;
    }
}
