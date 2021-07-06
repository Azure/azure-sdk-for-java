// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.perf;

import com.azure.perf.test.core.NullOutputStream;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.perf.core.DirectoryTest;
import reactor.core.publisher.Mono;

import java.io.OutputStream;
import java.util.UUID;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public class ReadFileDatalakeTest extends DirectoryTest<PerfStressOptions> {
    private static final int BUFFER_SIZE = 16 * 1024 * 1024;
    private static final OutputStream DEV_NULL = new NullOutputStream();
    private static final String FILE_NAME = "perfstress-filev11-" + UUID.randomUUID().toString();

    protected final DataLakeFileClient dataLakeFileClient;
    protected final DataLakeFileAsyncClient dataLakeFileAsyncClient;

    private final byte[] buffer = new byte[BUFFER_SIZE];

    public ReadFileDatalakeTest(PerfStressOptions options) {
        super(options);
        dataLakeFileClient = dataLakeDirectoryClient.getFileClient(FILE_NAME);
        dataLakeFileAsyncClient = dataLakeDirectoryAsyncClient.getFileAsyncClient(FILE_NAME);
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(dataLakeFileAsyncClient.create())
            .then(dataLakeFileAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), null, true))
            .then();
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        dataLakeFileClient.read(DEV_NULL);
    }

    @Override
    public Mono<Void> runAsync() {
        return dataLakeFileAsyncClient.read()
            .map(b -> {
                int readCount = 0;
                int remaining = b.remaining();
                while (readCount < remaining) {
                    int expectedReadCount = Math.min(remaining - readCount, BUFFER_SIZE);
                    b.get(buffer, 0, expectedReadCount);
                    readCount += expectedReadCount;
                }
                return 1;
            }).then();
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    public Mono<Void> globalCleanupAsync() {
        return dataLakeFileAsyncClient.delete()
            .then(super.globalCleanupAsync())
            .then();
    }
}
