// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.perf.core.DirectoryTest;
import com.azure.storage.file.datalake.perf.core.FileTestBase;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.UUID;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public class AppendFileDatalakeTest extends DirectoryTest<PerfStressOptions> {
    private static final int BUFFER_SIZE = 16 * 1024 * 1024;
    private static final String FILE_NAME = "perfstress-file-" + UUID.randomUUID().toString();

    private final byte[] buffer = new byte[BUFFER_SIZE];

    protected final DataLakeFileClient dataLakeFileClient;
    protected final DataLakeFileAsyncClient dataLakeFileAsyncClient;

    public AppendFileDatalakeTest(PerfStressOptions options) {
        super(options);
        dataLakeFileClient = dataLakeDirectoryClient.getFileClient(FILE_NAME);
        dataLakeFileAsyncClient = dataLakeDirectoryAsyncClient.getFileAsyncClient(FILE_NAME);
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(dataLakeFileAsyncClient.create())
//            .then(dataLakeFileAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), null))
            .then();
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        dataLakeFileAsyncClient.append(createRandomByteBufferFlux(options.getSize()), 0, options.getSize());
    }

    @Override
    public Mono<Void> runAsync() {
        return dataLakeFileAsyncClient.append(createRandomByteBufferFlux(options.getSize()), 0, options.getSize())
            .then();
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    public Mono<Void> globalCleanupAsync() {
        return dataLakeFileAsyncClient.delete()
            .then(super.globalCleanupAsync())
            .then();
    }
}
