// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.file.share.perf.core.FileTestBase;
import reactor.core.publisher.Mono;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public class UploadFileShareTest extends FileTestBase<PerfStressOptions> {
    public UploadFileShareTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> runAsync() {
        return shareFileAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), options.getSize())
            .then();
    }
}
