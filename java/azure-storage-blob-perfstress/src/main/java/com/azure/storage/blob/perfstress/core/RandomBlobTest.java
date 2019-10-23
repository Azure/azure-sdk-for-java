package com.azure.storage.blob.perfstress.core;

import java.util.UUID;

import com.azure.perfstress.PerfStressOptions;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.perfstress.core.ContainerTest;

public abstract class RandomBlobTest<TOptions extends PerfStressOptions> extends ContainerTest<TOptions> {
    protected final BlobClient _blobClient;
    protected final BlobAsyncClient _blobAsyncClient;

    public RandomBlobTest(TOptions options) {
        super(options);

        String blobName = "randomblobtest-" + UUID.randomUUID().toString();
        _blobClient = BlobContainerClient.getBlobClient(blobName);
        _blobAsyncClient = BlobContainerAsyncClient.getBlobAsyncClient(blobName);
    }
}