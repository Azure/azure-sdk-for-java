// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.perf.core.ContainerTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class ListBlobsTest extends ContainerTest<PerfStressOptions> {
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
