// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.perf.core.BlobTestBase;
import java.nio.ByteBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UploadBlobTest extends BlobTestBase<PerfStressOptions> {

    private final Flux<ByteBuffer> randomByteBufferFlux;

    public UploadBlobTest(PerfStressOptions options) {
        super(options);
        this.randomByteBufferFlux = createRandomByteBufferFlux(options.getSize());
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> runAsync() {
        return blobAsyncClient.upload(randomByteBufferFlux, null, true).then();
    }
}
