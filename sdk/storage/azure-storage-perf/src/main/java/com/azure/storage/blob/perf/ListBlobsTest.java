// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.perf.core.ContainerTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;
import java.util.UUID;

public class ListBlobsTest extends ContainerTest<PerfStressOptions> {
    private final AtomicLong _count = new AtomicLong();
    private final AtomicLong _iterations = new AtomicLong();

    public ListBlobsTest(PerfStressOptions options) {
        super(options);
    }

    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(
            Flux.range(0, options.getCount())
                .map(i -> "getblobstest-" + UUID.randomUUID())
                .flatMap(b -> blobContainerAsyncClient.getBlobAsyncClient(b).upload(Flux.empty(), null))
                .then());
    }

    @Override
    public Mono<Void> cleanupAsync() {
        System.out.printf("Counted %d blobs over %d iterations.%n", _count.get(), _iterations.get());
        return super.cleanupAsync();
    }

    @Override
    public void run() {
        blobContainerClient.listBlobs().forEach(b -> {
        });
    }

    @Override
    public Mono<Void> runAsync() {
        _iterations.incrementAndGet();

        return blobContainerAsyncClient.listBlobs()
            .doOnNext(blobItem -> _count.incrementAndGet())
            .then();
    }
}
