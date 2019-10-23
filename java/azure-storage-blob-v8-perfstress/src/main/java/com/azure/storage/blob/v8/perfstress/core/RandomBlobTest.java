package com.azure.storage.blob.v8.perfstress.core;

import java.net.URISyntaxException;
import java.util.UUID;

import com.azure.perfstress.PerfStressOptions;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

public abstract class RandomBlobTest<TOptions extends PerfStressOptions> extends ContainerTest<TOptions> {
    protected final CloudBlockBlob _cloudBlockBlob;
    
    public RandomBlobTest(TOptions options) {
        super(options);

        String blobName = "randomblobtest-" + UUID.randomUUID().toString();

        try {
            _cloudBlockBlob = CloudBlobContainer.getBlockBlobReference(blobName);
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException(e);
        }
    }
}