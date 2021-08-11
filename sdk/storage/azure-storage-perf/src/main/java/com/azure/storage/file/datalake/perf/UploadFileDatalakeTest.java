// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.perf;

import com.azure.perf.test.core.RepeatingInputStream;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.azure.storage.StoragePerfStressOptions;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import com.azure.storage.file.datalake.perf.core.FileTestBase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public class UploadFileDatalakeTest extends FileTestBase<StoragePerfStressOptions> {
    protected final RepeatingInputStream inputStream;
    protected final Flux<ByteBuffer> byteBufferFlux;

    public UploadFileDatalakeTest(StoragePerfStressOptions options) {
        super(options);
        if (options.isSync()) {
            inputStream = (RepeatingInputStream) TestDataCreationHelper.createRandomInputStream(options.getSize());
            byteBufferFlux = null;
        } else {
            byteBufferFlux = createRandomByteBufferFlux(options.getSize());
            inputStream = null;
        }
    }

    @Override
    public void run() {
        inputStream.reset();
        FileParallelUploadOptions uploadOptions = new FileParallelUploadOptions(inputStream, options.getSize())
            .setParallelTransferOptions(
                new ParallelTransferOptions()
                    .setMaxSingleUploadSizeLong(options.getTransferSingleUploadSize())
                    .setBlockSizeLong(options.getTransferBlockSize())
                    .setMaxConcurrency(options.getTransferConcurrency())
            );
        dataLakeFileClient.uploadWithResponse(uploadOptions, null, null);
    }

    @Override
    public Mono<Void> runAsync() {
        FileParallelUploadOptions uploadOptions = new FileParallelUploadOptions(
            createRandomByteBufferFlux(options.getSize()))
            .setParallelTransferOptions(
                new ParallelTransferOptions()
                    .setMaxSingleUploadSizeLong(options.getTransferSingleUploadSize())
                    .setBlockSizeLong(options.getTransferBlockSize())
                    .setMaxConcurrency(options.getTransferConcurrency())
            );
        return dataLakeFileAsyncClient.uploadWithResponse(uploadOptions)
            .then();
    }
}
