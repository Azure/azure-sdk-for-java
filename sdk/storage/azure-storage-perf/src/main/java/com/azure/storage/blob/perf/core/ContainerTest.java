// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;

import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ContainerTest<TOptions extends PerfStressOptions> extends ServiceTest<TOptions> {
    private static final AtomicInteger ITERATION = new AtomicInteger();
    private static final String CONTAINER_NAME_BASE = "perfstress-" + UUID.randomUUID();

    protected final BlobContainerClient blobContainerClient;
    protected final BlobContainerAsyncClient blobContainerAsyncClient;

    public ContainerTest(TOptions options) {
        super(options);
        // Setup the container clients
        int iteration = ITERATION.getAndIncrement();
        blobContainerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME_BASE + iteration);
        blobContainerAsyncClient = blobServiceAsyncClient.getBlobContainerAsyncClient(CONTAINER_NAME_BASE + iteration);
    }

    // NOTE: the pattern setup the parent first, then yourself.
    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(blobContainerAsyncClient.create());
    }

    // NOTE: the pattern, cleanup yourself, then the parent.
    @Override
    public Mono<Void> globalCleanupAsync() {
        return blobContainerAsyncClient.delete().then(super.globalCleanupAsync());
    }
}
