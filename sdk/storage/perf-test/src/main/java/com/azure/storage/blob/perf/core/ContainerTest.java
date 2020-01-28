// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.core.test.perf.PerfStressOptions;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;

import reactor.core.publisher.Mono;

import java.util.UUID;

public abstract class ContainerTest<TOptions extends PerfStressOptions> extends ServiceTest<TOptions> {
    protected static final String containerName = "perfstress-" + UUID.randomUUID().toString();

    protected final BlobContainerClient BlobContainerClient;
    protected final BlobContainerAsyncClient BlobContainerAsyncClient;

    public ContainerTest(TOptions options) {
        super(options);

        BlobContainerClient = BlobServiceClient.getBlobContainerClient(containerName);
        BlobContainerAsyncClient = BlobServiceAsyncClient.getBlobContainerAsyncClient(containerName);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(BlobContainerAsyncClient.create());
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return BlobContainerAsyncClient.delete().then(super.globalCleanupAsync());
    }
}
