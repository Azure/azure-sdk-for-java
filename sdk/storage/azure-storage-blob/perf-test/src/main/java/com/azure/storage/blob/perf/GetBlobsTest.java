// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import java.util.UUID;

import com.azure.core.test.perf.CountOptions;
import com.azure.storage.blob.perf.core.ContainerTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class GetBlobsTest extends ContainerTest<CountOptions> {
    public GetBlobsTest(CountOptions options) {
        super(options);
    }

    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(
            Flux.range(0, options.getCount())
                .map(i -> "getblobstest-" + UUID.randomUUID())
                .flatMap(b -> BlobContainerAsyncClient.getBlobAsyncClient(b).upload(Flux.empty(), null))
                .then());
    }

    @Override
    public void run() {
        BlobContainerClient.listBlobs().forEach(b -> {});
    }

    @Override
    public Mono<Void> runAsync() {
        return BlobContainerAsyncClient.listBlobs().then();
    }
}
