// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.perf.core.BlobTestBase;
import reactor.core.publisher.Mono;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public class UploadBlobTest extends BlobTestBase<PerfStressOptions> {
    public UploadBlobTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> runAsync() {
        return blobAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), null, true)
            .then();
    }
}
