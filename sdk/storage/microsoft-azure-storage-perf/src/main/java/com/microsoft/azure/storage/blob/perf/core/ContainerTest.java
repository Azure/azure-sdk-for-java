// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URISyntaxException;
import java.util.UUID;

public abstract class ContainerTest<TOptions extends PerfStressOptions> extends ServiceTest<TOptions> {
    protected static final String CONTAINER_NAME = "perfstress-" + UUID.randomUUID().toString();

    protected final CloudBlobContainer cloudBlobContainer;

    public ContainerTest(TOptions options) {
        super(options);

        try {
            cloudBlobContainer = cloudBlobClient.getContainerReference(CONTAINER_NAME);
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(Mono.fromCallable(() -> {
                cloudBlobContainer.create();
                return 1;
            }))
            .then();
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return Mono.empty()
            .publishOn(Schedulers.elastic())
            .then(Mono.fromCallable(() -> {
                cloudBlobContainer.delete();
                return 1;
            }))
            .then(super.globalCleanupAsync());
    }
}
