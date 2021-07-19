// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.RepeatingInputStream;
import com.azure.storage.blob.perf.core.BlobTestBase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;
import static com.azure.perf.test.core.TestDataCreationHelper.createRandomInputStream;

public class UploadBlobTest extends BlobTestBase<PerfStressOptions> {
    protected final RepeatingInputStream inputStream;
    protected final Flux<ByteBuffer> byteBufferFlux;

    public UploadBlobTest(PerfStressOptions options) {
        super(options);
        inputStream = (RepeatingInputStream) createRandomInputStream(options.getSize());
        inputStream.mark(Integer.MAX_VALUE);
        byteBufferFlux = createRandomByteBufferFlux(options.getSize());
    }

    @Override
    public void run() {
        inputStream.reset();
        // This one uses Storage's stream->flux converter
        blobClient.upload(inputStream, options.getSize(), true);
    }

    @Override
    public Mono<Void> runAsync() {
        return blobAsyncClient.upload(byteBufferFlux, null, true)
            .then();
    }
}
