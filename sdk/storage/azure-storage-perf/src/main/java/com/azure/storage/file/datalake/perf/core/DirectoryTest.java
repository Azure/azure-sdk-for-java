// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient;
import com.azure.storage.file.datalake.DataLakeDirectoryClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

public abstract class DirectoryTest<TOptions extends PerfStressOptions> extends FileSystemTest<TOptions> {
    private static final String DIRECTORY_NAME = "perfstress-dl-" + UUID.randomUUID().toString();

    protected final DataLakeDirectoryClient dataLakeDirectoryClient;
    protected final DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient;

    public DirectoryTest(TOptions options) {
        super(options);
        // Setup the container clients
        dataLakeDirectoryClient = dataLakeFileSystemClient.getDirectoryClient(DIRECTORY_NAME);
        dataLakeDirectoryAsyncClient = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(DIRECTORY_NAME);
    }

    // NOTE: the pattern setup the parent first, then yourself.
    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(dataLakeDirectoryAsyncClient.create()).then();
    }
}
