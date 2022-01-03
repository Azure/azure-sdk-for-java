// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.RepeatingInputStream;
import com.azure.storage.StoragePerfStressOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.perf.core.BlobTestBase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;
import static com.azure.perf.test.core.TestDataCreationHelper.createRandomInputStream;

public class UploadBlobNoLengthTest extends BlobTestBase<StoragePerfStressOptions> {
    protected final RepeatingInputStream inputStream;
    protected final Flux<ByteBuffer> byteBufferFlux;

    public UploadBlobNoLengthTest(StoragePerfStressOptions options) {
        super(options);
        if (options.isSync()) {
            inputStream = (RepeatingInputStream) createRandomInputStream(options.getSize());
            inputStream.mark(Long.MAX_VALUE);
            byteBufferFlux = null;
        } else {
            inputStream = null;
            byteBufferFlux = createRandomByteBufferFlux(options.getSize());
        }
    }

    @Override
    public void run() {
        inputStream.reset();
        // This one uses Core's stream->flux converter
        BlobParallelUploadOptions uploadOptions = new BlobParallelUploadOptions(inputStream)
            .setParallelTransferOptions(
                new ParallelTransferOptions()
                .setMaxSingleUploadSizeLong(options.getTransferSingleUploadSize())
                .setBlockSizeLong(options.getTransferBlockSize())
                .setMaxConcurrency(options.getTransferConcurrency())
            );
        blobClient.uploadWithResponse(uploadOptions, null, null);
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }
}
