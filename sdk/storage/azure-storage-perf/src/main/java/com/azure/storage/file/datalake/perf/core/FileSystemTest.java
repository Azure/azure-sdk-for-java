// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

public abstract class FileSystemTest<TOptions extends PerfStressOptions> extends ServiceTest<TOptions> {
    private static final String FILE_SYSTEM_NAME = "perfstress-dl-" + UUID.randomUUID().toString();

    protected final DataLakeFileSystemClient dataLakeFileSystemClient;
    protected final DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient;

    public FileSystemTest(TOptions options) {
        super(options);
        // Setup the container clients
        dataLakeFileSystemClient = dataLakeServiceClient.getFileSystemClient(FILE_SYSTEM_NAME);
        dataLakeFileSystemAsyncClient = dataLakeServiceAsyncClient.getFileSystemAsyncClient(FILE_SYSTEM_NAME);
    }

    // NOTE: the pattern setup the parent first, then yourself.
    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(dataLakeFileSystemAsyncClient.create()).then();
    }

    // NOTE: the pattern, cleanup yourself, then the parent.
    @Override
    public Mono<Void> globalCleanupAsync() {
        return dataLakeFileSystemAsyncClient.delete().then(super.globalCleanupAsync());
    }
}
