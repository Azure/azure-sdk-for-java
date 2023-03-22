// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.perf;

import com.azure.containers.containerregistry.perf.core.ServiceTest;
import com.azure.core.util.Context;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.RepeatingInputStream;
import reactor.core.publisher.Mono;

import java.nio.channels.Channels;
import java.util.Arrays;

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
        RepeatingInputStream input = new RepeatingInputStream(options.getSize());
        repository.uploadBlob(Channels.newChannel(input), Context.NONE);
    }

    @Override
    public Mono<Void> runAsync() {
        return blobAsyncClient.uploadBlob(generateAsyncStream(options.getSize())).then();
    }
}
