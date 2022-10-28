// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.perf.core.ContainerTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

public class ListBlobsTest extends ContainerTest<PerfStressOptions> {
    public ListBlobsTest(PerfStressOptions options) {
        super(options);
    }

    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(
            Flux.range(0, options.getCount())
                .parallel(options.getParallel())
                .runOn(Schedulers.boundedElastic())
                .flatMap(iteration -> blobContainerAsyncClient.getBlobAsyncClient("getblobstest-" + UUID.randomUUID())
                    .upload(Flux.empty(), null), false, Math.min(options.getParallel(), 1000 / options.getParallel()), 1)
                .then());
    }

    @Override
    public void run() {
        blobContainerClient.listBlobs().forEach(b -> {
        });
    }

    @Override
    public Mono<Void> runAsync() {
        return blobContainerAsyncClient.listBlobs()
            .then();
    }
}
