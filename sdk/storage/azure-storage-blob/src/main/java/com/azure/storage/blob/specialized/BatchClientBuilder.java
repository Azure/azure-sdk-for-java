package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpPipeline;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;

import java.net.URL;

public class BatchClientBuilder {
    private HttpPipeline pipeline;
    private URL url;

    public BatchClient buildClient() {
        return new BatchClient(buildAsyncClient());
    }

    public BatchAsyncClient buildAsyncClient() {
        return new BatchAsyncClient(new AzureBlobStorageBuilder()
            .pipeline(pipeline)
            .url(url.toString())
            .build(), pipeline);
    }

    public BatchClientBuilder blobServiceClient(BlobServiceClient blobServiceClient) {
        this.pipeline = blobServiceClient.getHttpPipeline();
        this.url = blobServiceClient.getAccountUrl();
        return this;
    }

    public BatchClientBuilder blobServiceAsyncClient(BlobServiceAsyncClient blobServiceAsyncClient) {
        this.pipeline = blobServiceAsyncClient.getHttpPipeline();
        this.url = blobServiceAsyncClient.getAccountUrl();
        return this;
    }
}
