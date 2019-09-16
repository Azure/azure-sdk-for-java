package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpPipeline;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlockBlobAsyncClient;

public class BatchClientBuilder {
    private HttpPipeline pipeline;

    public BatchClient buildClient() {
        return new BatchClient(buildAsyncClient());
    }

    public BatchAsyncClient buildAsyncClient() {
        reu
    }

    public BatchClientBuilder blobServiceClient(BlobServiceClient blobServiceClient) {
        return this;
    }

    public BatchClientBuilder blobServiceAsyncClient(BlockBlobAsyncClient blockBlobAsyncClient) {
        return this;
    }
}
