// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.perf.core.ContainerTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ListBlobsTest extends ContainerTest<PerfStressOptions> {
    public ListBlobsTest(PerfStressOptions options) {
        super(options);
    }

    public Mono<Void> globalSetupAsync() {
        int count = options.getCount();
        long size = (1.6 * 1024 * 1024 * 1024) / count;

        Flux<ByteBuffer> data = createRandomByteBufferFlux(size);

        return super.globalSetupAsync().then(
            Flux.range(0, count)
                .map(i -> "getblobstest-" + UUID.randomUUID())
                .flatMap(b -> blobContainerAsyncClient.getBlobAsyncClient(b).upload(data, null))
                .then());
    }

    @Override
    public void run() {
        blobContainerClient.listBlobs().forEach(b -> {
        });
    }

    @Override
    public Mono<Void> runAsync() {
        return blobContainerAsyncClient
            .listBlobs()
            .flatMap(b -> blobContainerAsyncClient
                .getBlobAsyncClient(b.getName())
                .downloadToFile(b.getName())
                .doOnError(ex -> System.out.println("Download error: " + ex.toString()))
                .onErrorResume(ex -> Mono.empty()))
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .then();
    }
}
