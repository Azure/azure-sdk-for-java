// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import reactor.core.publisher.Mono;

public abstract class FileTestBase<TOptions extends PerfStressOptions> extends DirectoryTest<TOptions> {

    public static final int DEFAULT_BUFFER_SIZE = 8192;
    protected final DataLakeFileClient dataLakeFileClient;
    protected final DataLakeFileAsyncClient dataLakeFileAsyncClient;

    public FileTestBase(TOptions options) {
        super(options);

        String fileName = "randomfiletest-" + CoreUtils.randomUuid();

        dataLakeFileClient =  dataLakeDirectoryClient.getFileClient(fileName);
        dataLakeFileAsyncClient = dataLakeDirectoryAsyncClient.getFileAsyncClient(fileName);
    }

    @Override
    public Mono<Void> setupAsync() {
        return dataLakeFileAsyncClient.create().then(super.cleanupAsync());
    }

    @Override
    public void setup() {
        dataLakeFileClient.create();
        super.cleanup(); // This doesn't seem right...
    }
}
