// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.perf.core.ContainerTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class ListBlobsTest extends ContainerTest<PerfStressOptions> {
    private List<BlobAsyncClient> _clients;

    public ListBlobsTest(PerfStressOptions options) {
        super(options);
    }

    public Mono<Void> globalSetupAsync() {
        int count = options.getCount();
        long size = (long)((1.6 * 1024 * 1024 * 1024) / count);

        Flux<ByteBuffer> data = createRandomByteBufferFlux(size);

        return super.globalSetupAsync().then(
            Flux.range(0, count)
                .map(i -> "getblobstest-" + UUID.randomUUID())
                .flatMap(b -> blobContainerAsyncClient.getBlobAsyncClient(b).upload(data, null))
                .then());
    }

    public Mono<Void> setupAsync() {
        _clients = blobContainerAsyncClient.listBlobs().collectList().block();
        return Mono.empty();
    }

    @Override
    public void run() {
        blobContainerClient.listBlobs().forEach(b -> {
        });
    }

    @Override
    public Mono<Void> runAsync() {
        // return blobContainerAsyncClient
        //     .listBlobs()
        //     .flatMap(b -> blobContainerAsyncClient
        //         .getBlobAsyncClient(b.getName())
        //         .downloadToFile(b.getName(), /* overwrite */ true)
        return Flux.fromIterable(_clients)
            .flatMap(c -> c
                .downloadToFile(c.getBlobName(), /* overwrite */ true)
                .doOnError(ex -> System.out.println("Download error: " + ex.toString()))
                .onErrorResume(ex -> Mono.empty()))
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .then();
    }
}
