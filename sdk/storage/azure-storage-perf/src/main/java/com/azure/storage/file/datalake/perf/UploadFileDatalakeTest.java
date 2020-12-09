// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.file.datalake.perf.core.FileTestBase;
import reactor.core.publisher.Mono;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;
import static com.azure.perf.test.core.TestDataCreationHelper.createRandomInputStream;

public class UploadFileDatalakeTest extends FileTestBase<PerfStressOptions> {
    public UploadFileDatalakeTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        dataLakeFileClient.upload(createRandomInputStream(options.getSize()), options.getSize(), true);
    }

    @Override
    public Mono<Void> runAsync() {
        return dataLakeFileAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), null, true)
            .then();
    }
}
