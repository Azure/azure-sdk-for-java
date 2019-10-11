package com.microsoft.storageperf;

import com.microsoft.storageperf.core.PerfStressOptions;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URISyntaxException;
import java.util.UUID;

public abstract class ContainerV8Test<TOptions extends PerfStressOptions> extends ServiceV8Test<TOptions> {
    protected static final String ContainerName = "perfstress-" + UUID.randomUUID().toString();

    protected final CloudBlobContainer CloudBlobContainer;

    public ContainerV8Test(TOptions options) {
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
