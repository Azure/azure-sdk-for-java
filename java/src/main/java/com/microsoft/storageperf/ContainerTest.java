package com.microsoft.storageperf;

import com.azure.storage.blob.ContainerAsyncClient;
import com.azure.storage.blob.ContainerClient;
import com.microsoft.storageperf.core.PerfStressOptions;

import reactor.core.publisher.Mono;

import java.util.UUID;

public abstract class ContainerTest<TOptions extends PerfStressOptions> extends ServiceTest<TOptions> {
    protected static final String ContainerName = "perfstress-" + UUID.randomUUID().toString();

    protected final ContainerClient ContainerClient;
    protected final ContainerAsyncClient ContainerAsyncClient;

    public ContainerTest(TOptions options) {
        super(options);

        ContainerClient = BlobServiceClient.getContainerClient(ContainerName);
        ContainerAsyncClient = BlobServiceAsyncClient.getContainerAsyncClient(ContainerName);
    }

    @Override
    public Mono<Void> GlobalSetupAsync() {
        return super.GlobalSetupAsync().then(ContainerAsyncClient.create());
    }

    @Override
    public Mono<Void> GlobalCleanupAsync() {
        return ContainerAsyncClient.delete().then(super.GlobalCleanupAsync());
    }
}
