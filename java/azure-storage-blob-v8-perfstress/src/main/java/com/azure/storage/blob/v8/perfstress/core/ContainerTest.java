package com.azure.storage.blob.v8.perfstress.core;

import com.azure.perfstress.PerfStressOptions;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URISyntaxException;
import java.util.UUID;

public abstract class ContainerTest<TOptions extends PerfStressOptions> extends ServiceTest<TOptions> {
    protected static final String ContainerName = "perfstress-" + UUID.randomUUID().toString();

    protected final CloudBlobContainer CloudBlobContainer;

    public ContainerTest(TOptions options) {
        super(options);

        try {
            CloudBlobContainer = CloudBlobClient.getContainerReference(ContainerName);
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> GlobalSetupAsync() {
        return super.GlobalCleanupAsync()
            .publishOn(Schedulers.elastic())
            .then(Mono.fromCallable(() -> {
                CloudBlobContainer.create();
                return 1;
            }))
            .then();
    }

    @Override
    public Mono<Void> GlobalCleanupAsync() {
        return Mono.empty()
            .publishOn(Schedulers.elastic())
            .then(Mono.fromCallable(() -> {
                CloudBlobContainer.delete();
                return 1;
            }))
            .then(super.GlobalCleanupAsync());
    }
}
