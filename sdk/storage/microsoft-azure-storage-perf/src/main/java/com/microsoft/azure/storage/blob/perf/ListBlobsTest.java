// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob.perf;

import java.util.UUID;

import com.azure.perf.test.core.NullInputStream;
import com.azure.perf.test.core.PerfStressOptions;
import com.microsoft.azure.storage.blob.perf.core.ContainerTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class ListBlobsTest extends ContainerTest<PerfStressOptions> {
    public ListBlobsTest(PerfStressOptions options) {
        super(options);
    }

    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(
            Flux.range(0, options.getCount())
                .map(i -> "getblobstest-" + UUID.randomUUID())
                .flatMap(b -> upload(b).then(Mono.just(1)))
                .then());
    }

    private Mono<Void> upload(String blobName) {
        return Mono.empty()
            .publishOn(Schedulers.elastic())
            .then(Mono.fromCallable(() -> {
                cloudBlobContainer.getBlockBlobReference(blobName).upload(new NullInputStream(), 0);
                return 1;
            }))
            .then();
    }

    @Override
    public void run() {
        cloudBlobContainer.listBlobs().forEach(b -> { });
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }
}
