package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpPipeline;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class BatchAsyncClient {
    private final AzureBlobStorageImpl client;
    private final HttpPipeline queuingPipeline;
    private final List<Mono<?>> queuedRequests;

    BatchAsyncClient(AzureBlobStorageImpl client, HttpPipeline queuingPipeline) {
        this.client = client;
        this.queuingPipeline = queuingPipeline;
        this.queuedRequests = new ArrayList<>();
    }

    // TODO (alzimmer): Replace response with correct type.
    public void delete() {

    }

    // TODO (alzimmer): Replace response with correct type.
    public void setTier() {

    }

    // TODO (alzimmer): Replace response with correct type.
    public void send() {
        queuedRequests.clear();
    }
}
