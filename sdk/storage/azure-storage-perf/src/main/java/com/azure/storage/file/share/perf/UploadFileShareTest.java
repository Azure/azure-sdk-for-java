// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf;

import com.azure.perf.test.core.RepeatingInputStream;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.azure.storage.StoragePerfStressOptions;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.file.share.perf.core.FileTestBase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public class UploadFileShareTest extends FileTestBase<StoragePerfStressOptions> {
    protected final RepeatingInputStream inputStream;
    protected final Flux<ByteBuffer> byteBufferFlux;

    public UploadFileShareTest(StoragePerfStressOptions options) {
        super(options);
        inputStream = (RepeatingInputStream) TestDataCreationHelper.createRandomInputStream(options.getSize());
        byteBufferFlux = createRandomByteBufferFlux(options.getSize());
    }

    @Override
    public void run() {
        inputStream.reset();
        ParallelTransferOptions transferOptions = new ParallelTransferOptions()
            .setMaxSingleUploadSizeLong(options.getTransferSingleUploadSize())
            .setBlockSizeLong(options.getTransferBlockSize())
            .setMaxConcurrency(options.getTransferConcurrency());
        shareFileClient.upload(inputStream, options.getSize(), transferOptions);
    }

    @Override
    public Mono<Void> runAsync() {
        ParallelTransferOptions transferOptions = new ParallelTransferOptions()
            .setMaxSingleUploadSizeLong(options.getTransferSingleUploadSize())
            .setBlockSizeLong(options.getTransferBlockSize())
            .setMaxConcurrency(options.getTransferConcurrency());
        return shareFileAsyncClient.upload(byteBufferFlux, transferOptions)
            .then();
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(Mono.defer(() -> shareFileAsyncClient.create(options.getSize()))).then();
    }
}
