// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.RepeatingInputStream;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.perf.core.BlobTestBase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;
import static com.azure.perf.test.core.TestDataCreationHelper.createRandomInputStream;

public class UploadBlobNoLengthTest extends BlobTestBase<PerfStressOptions> {
    protected final RepeatingInputStream inputStream;
    protected final Flux<ByteBuffer> byteBufferFlux;

    public UploadBlobNoLengthTest(PerfStressOptions options) {
        super(options);
        inputStream = (RepeatingInputStream) createRandomInputStream(options.getSize());
        inputStream.mark(Integer.MAX_VALUE);
        byteBufferFlux = createRandomByteBufferFlux(options.getSize());
    }

    @Override
    public void run() {
        inputStream.reset();
        // This one uses Core's stream->flux converter
        blobClient.uploadWithResponse(new BlobParallelUploadOptions(inputStream), null, null);
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }
}
