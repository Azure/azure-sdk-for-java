// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.RepeatingInputStream;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.perf.core.DirectoryTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.UUID;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public class AppendFileDatalakeTest extends DirectoryTest<PerfStressOptions> {
    private static final String FILE_NAME = "perfstress-file-" + UUID.randomUUID().toString();

    protected final DataLakeFileClient dataLakeFileClient;
    protected final DataLakeFileAsyncClient dataLakeFileAsyncClient;
    protected final RepeatingInputStream inputStream;
    protected final Flux<ByteBuffer> byteBufferFlux;

    public AppendFileDatalakeTest(PerfStressOptions options) {
        super(options);
        dataLakeFileClient = dataLakeDirectoryClient.getFileClient(FILE_NAME);
        dataLakeFileAsyncClient = dataLakeDirectoryAsyncClient.getFileAsyncClient(FILE_NAME);
        inputStream = (RepeatingInputStream) TestDataCreationHelper.createRandomInputStream(options.getSize());
        byteBufferFlux = createRandomByteBufferFlux(options.getSize());
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(dataLakeFileAsyncClient.create())
            .then();
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        inputStream.reset();
        dataLakeFileClient.append(TestDataCreationHelper.createRandomInputStream(options.getSize()),
            0, options.getSize());
    }

    @Override
    public Mono<Void> runAsync() {
        return dataLakeFileAsyncClient.append(byteBufferFlux, 0, options.getSize());
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    public Mono<Void> globalCleanupAsync() {
        return dataLakeFileAsyncClient.delete()
            .then(super.globalCleanupAsync())
            .then();
    }
}
