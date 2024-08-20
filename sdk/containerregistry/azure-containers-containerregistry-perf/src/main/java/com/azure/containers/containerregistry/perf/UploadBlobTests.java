// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.perf;

import com.azure.containers.containerregistry.perf.core.ServiceTest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.RepeatingInputStream;
import reactor.core.publisher.Mono;

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
    public void globalSetup() {
        super.globalSetup();
        importImage(REPOSITORY_NAME, Arrays.asList("latest"));
    }

    @Override
    public void run() {
        RepeatingInputStream input = new RepeatingInputStream(options.getSize());
        blobClient.uploadBlob(BinaryData.fromStream(input), Context.NONE);
    }

    @Override
    public Mono<Void> runAsync() {
        return
            BinaryData.fromFlux(generateAsyncStream(options.getSize()))
                .flatMap(content -> blobAsyncClient.uploadBlob(content))
                .then();
    }
}
