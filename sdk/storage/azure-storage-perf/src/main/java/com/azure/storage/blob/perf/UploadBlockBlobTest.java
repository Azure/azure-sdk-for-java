// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;
import static com.azure.perf.test.core.TestDataCreationHelper.createRandomInputStream;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.perf.core.BlobTestBase;
import java.io.InputStream;
import java.nio.ByteBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UploadBlockBlobTest extends BlobTestBase<PerfStressOptions> {
    private final InputStream randomInputStream;
    private final Flux<ByteBuffer> randomByteBufferFlux;

    public UploadBlockBlobTest(PerfStressOptions options) {
        super(options);
        this.randomInputStream = createRandomInputStream(options.getSize());
        this.randomByteBufferFlux = createRandomByteBufferFlux(options.getSize());
    }

    @Override
    public void run() {
        blockBlobClient.upload(randomInputStream, options.getSize(), true);
    }

    @Override
    public Mono<Void> runAsync() {
        return blockBlobAsyncClient.upload(randomByteBufferFlux, options.getSize(), true).then();
    }
}
