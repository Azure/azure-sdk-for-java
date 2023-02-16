// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.perf;

import com.azure.containers.containerregistry.perf.core.ServiceTest;
import com.azure.containers.containerregistry.perf.core.TestInputStream;
import com.azure.core.util.Context;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.nio.channels.Channels;
import java.util.Arrays;

import static com.azure.containers.containerregistry.perf.core.TestInputStream.generateAsyncStream;
import static com.azure.containers.containerregistry.perf.core.Utils.REPOSITORY_NAME;

public class UploadBlobTests extends ServiceTest<PerfStressOptions> {
    public UploadBlobTests(PerfStressOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(importImageAsync(REPOSITORY_NAME, Arrays.asList("latest")));
    }

    @Override
    public void run() {
        TestInputStream stream = new TestInputStream(options.getSize());
        blobClient.uploadBlob(Channels.newChannel(stream), Context.NONE);
    }

    @Override
    public Mono<Void> runAsync() {
        return blobAsyncClient.uploadBlob(generateAsyncStream(options.getSize())).then();
    }
}
