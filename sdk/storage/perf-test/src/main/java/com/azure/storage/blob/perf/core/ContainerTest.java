package com.azure.storage.blob.perfstress.core;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.perfstress.PerfStressOptions;

import reactor.core.publisher.Mono;

import java.util.UUID;

public abstract class ContainerTest<TOptions extends PerfStressOptions> extends ServiceTest<TOptions> {
    protected static final String ContainerName = "perfstress-" + UUID.randomUUID().toString();

    protected final BlobContainerClient BlobContainerClient;
    protected final BlobContainerAsyncClient BlobContainerAsyncClient;

    public ContainerTest(TOptions options) {
        super(options);

        BlobContainerClient = BlobServiceClient.getBlobContainerClient(ContainerName);
        BlobContainerAsyncClient = BlobServiceAsyncClient.getBlobContainerAsyncClient(ContainerName);
    }

    @Override
    public Mono<Void> GlobalSetupAsync() {
        return super.GlobalSetupAsync().then(BlobContainerAsyncClient.create());
    }

    @Override
    public Mono<Void> GlobalCleanupAsync() {
        return BlobContainerAsyncClient.delete().then(super.GlobalCleanupAsync());
    }
}
