// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.RepeatingInputStream;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.perf.core.BlobTestBase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;
import static com.azure.perf.test.core.TestDataCreationHelper.createRandomInputStream;

public class UploadBlobTest extends BlobTestBase<UploadBlobPerfStressOptions> {
    protected final RepeatingInputStream inputStream;
    protected final Flux<ByteBuffer> byteBufferFlux;

    public UploadBlobTest(UploadBlobPerfStressOptions options) {
        super(options);
        inputStream = (RepeatingInputStream) createRandomInputStream(options.getSize());
        inputStream.mark(Integer.MAX_VALUE);
        byteBufferFlux = createRandomByteBufferFlux(options.getSize());
    }

    @Override
    public void run() {
        inputStream.reset();
        // This one uses Storage's stream->flux converter
        BlobParallelUploadOptions uploadOptions = new BlobParallelUploadOptions(inputStream, options.getSize())
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
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setMaxSingleUploadSizeLong(options.getTransferSingleUploadSize())
            .setBlockSizeLong(options.getTransferBlockSize())
            .setMaxConcurrency(options.getTransferConcurrency());
        return blobAsyncClient.upload(byteBufferFlux, parallelTransferOptions, true)
            .then();
    }
}
