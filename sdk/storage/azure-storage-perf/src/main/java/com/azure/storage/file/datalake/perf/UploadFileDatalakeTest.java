// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.RepeatingInputStream;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.azure.storage.file.datalake.perf.core.FileTestBase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public class UploadFileDatalakeTest extends FileTestBase<PerfStressOptions> {
    protected final RepeatingInputStream inputStream;
    protected final Flux<ByteBuffer> byteBufferFlux;

    public UploadFileDatalakeTest(PerfStressOptions options) {
        super(options);
        inputStream = (RepeatingInputStream) TestDataCreationHelper.createRandomInputStream(options.getSize());
        byteBufferFlux = createRandomByteBufferFlux(options.getSize());
    }

    @Override
    public void run() {
        inputStream.reset();
        dataLakeFileClient.upload(inputStream, options.getSize(), true);
    }

    @Override
    public Mono<Void> runAsync() {
        return dataLakeFileAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), null, true)
            .then();
    }
}
