// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.perf.core.ContainerTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class ListBlobsTest extends ContainerTest<PerfStressOptions> {
    public ListBlobsTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        // Perform blob uploading in parallel.
        //
        // This not only results in faster setup it also helps guard against an edge case seen in Reactor Netty
        // where only one IO thread could end up owning all connections in the connection pool. This results in
        // drastically less CPU usage and throughput, there is ongoing discussions with Reactor Netty on what causes
        // this edge case, whether we had a design flaw in the performance tests, or if there is a configuration change
        // needed in Reactor Netty.
        int parallel = options.getParallel();
        return super.globalSetupAsync().then(
            Flux.range(0, options.getCount())
                .parallel(parallel)
                .runOn(Schedulers.parallel())
                .flatMap(iteration -> blobContainerAsyncClient.getBlobAsyncClient("getblobstest-" + CoreUtils.randomUuid())
                    .getBlockBlobAsyncClient()
                    .upload(Flux.empty(), 0L), false, parallel, 1)
                .sequential()
                .then());
    }

    @Override
    public void globalSetup() {
        super.globalSetup();
        for (int i = 0; i < options.getCount(); i++) {
            blobContainerClient.getBlobClient("getblobstest-" + CoreUtils.randomUuid())
                .getBlockBlobClient()
                .upload(BinaryData.fromBytes(new byte[0]));
        }
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
