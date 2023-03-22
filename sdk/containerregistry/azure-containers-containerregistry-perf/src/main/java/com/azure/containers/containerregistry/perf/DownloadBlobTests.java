// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.perf;

import com.azure.containers.containerregistry.perf.core.ServiceTest;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.perf.test.core.NullOutputStream;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.nio.channels.Channels;
import java.util.Arrays;

public class DownloadBlobTests extends ServiceTest<PerfStressOptions> {
    private final String[] digest = new String[1];
    private final NullOutputStream output = new NullOutputStream();

    public DownloadBlobTests(PerfStressOptions options) {
        super(options);

    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return importImageAsync(REPOSITORY_NAME, Arrays.asList("latest")).then();
    }

    @Override
    public Mono<Void> setupAsync() {
        return blobAsyncClient
            .uploadBlob(generateAsyncStream(options.getSize()))
            .doOnNext(result -> digest[0] = result.getDigest())
            .then();
    }

    @Override
    public void run() {
        repository.downloadStreamWithResponse(digest[0], Channels.newChannel(output), Context.NONE);
    }

    @Override
    public Mono<Void> runAsync() {
        return blobAsyncClient.downloadStream(digest[0])
            .flatMap(result -> FluxUtil.writeToOutputStream(result.toFluxByteBuffer(), output))
            .then();
    }
}
