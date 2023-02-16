// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.perf;

import com.azure.containers.containerregistry.perf.core.ServiceTest;
import com.azure.core.util.Context;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.containers.containerregistry.perf.core.TestInputStream.generateAsyncStream;
import static com.azure.containers.containerregistry.perf.core.Utils.REPOSITORY_NAME;

public class DownloadBlobTests extends ServiceTest<PerfStressOptions> {
    private volatile AtomicReference<String> digest = new AtomicReference<>();
    private final TestOutputStream output = new TestOutputStream();
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
            .doOnNext(result -> digest.set(result.getDigest()))
            .then();
    }
    @Override
    public void run() {
        blobClient.downloadStream(digest.get(), Channels.newChannel(output), Context.NONE);
    }

    @Override
    public Mono<Void> runAsync() {
        return blobAsyncClient.downloadStream(digest.get())
            .flatMap(result -> result.writeValueTo(Channels.newChannel(output)))
            .then();
    }

    private class TestOutputStream extends OutputStream {
        @Override
        public void write(int b) {
        }

        @Override
        public void write(byte b[], int off, int len) {
        }
    }
}
