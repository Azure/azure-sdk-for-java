// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.perf.core.BlobTestBase;
import reactor.core.publisher.Mono;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;
import static com.azure.perf.test.core.TestDataCreationHelper.createRandomInputStream;

public class UploadBlockBlobTest extends BlobTestBase<PerfStressOptions> {
    public UploadBlockBlobTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        blockBlobClient.upload(createRandomInputStream(options.getSize()), options.getSize(), true);
    }

    @Override
    public Mono<Void> runAsync() {
        return blockBlobAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), options.getSize(), true)
            .then();
    }
}
