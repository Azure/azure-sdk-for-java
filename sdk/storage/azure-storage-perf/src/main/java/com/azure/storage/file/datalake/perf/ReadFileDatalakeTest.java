// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.perf;

import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.NullOutputStream;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.StoragePerfUtils;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.perf.core.DirectoryTest;
import reactor.core.publisher.Mono;

import java.io.OutputStream;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;
import static com.azure.perf.test.core.TestDataCreationHelper.createRandomInputStream;

public class ReadFileDatalakeTest extends DirectoryTest<PerfStressOptions> {
    private static final String FILE_NAME = "perfstress-filev11-" + CoreUtils.randomUuid();

    protected final DataLakeFileClient dataLakeFileClient;
    protected final DataLakeFileAsyncClient dataLakeFileAsyncClient;

    private final OutputStream devNull = new NullOutputStream();

    private final int bufferSize;
    private final byte[] buffer;

    public ReadFileDatalakeTest(PerfStressOptions options) {
        super(options);
        dataLakeFileClient = dataLakeDirectoryClient.getFileClient(FILE_NAME);
        dataLakeFileAsyncClient = dataLakeDirectoryAsyncClient.getFileAsyncClient(FILE_NAME);

        this.bufferSize = StoragePerfUtils.getDynamicDownloadBufferSize(options.getSize());
        this.buffer = new byte[bufferSize];
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(dataLakeFileAsyncClient.create())
            .then(dataLakeFileAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), null, true))
            .then();
    }

    @Override
    public void globalSetup() {
        super.globalSetup();
        dataLakeFileClient.create();
        dataLakeFileClient.upload(createRandomInputStream(options.getSize()), options.getSize(), true);
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        dataLakeFileClient.read(devNull);
    }

    @Override
    public Mono<Void> runAsync() {
        return dataLakeFileAsyncClient.read()
            .map(b -> {
                int readCount = 0;
                int remaining = b.remaining();
                while (readCount < remaining) {
                    int expectedReadCount = Math.min(remaining - readCount, bufferSize);
                    b.get(buffer, 0, expectedReadCount);
                    readCount += expectedReadCount;
                }
                return 1;
            }).then();
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    @Override
    public Mono<Void> globalCleanupAsync() {
        return dataLakeFileAsyncClient.delete()
            .then(super.globalCleanupAsync())
            .then();
    }

    @Override
    public void globalCleanup() {
        dataLakeFileClient.delete();
        super.globalCleanup();
    }
}
