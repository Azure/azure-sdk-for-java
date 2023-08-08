// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public abstract class FileTestBase<TOptions extends PerfStressOptions> extends DirectoryTest<TOptions> {

    public static final int DEFAULT_BUFFER_SIZE = 8192;
    protected final DataLakeFileClient dataLakeFileClient;
    protected final DataLakeFileAsyncClient dataLakeFileAsyncClient;

    public FileTestBase(TOptions options) {
        super(options);

        String fileName = "randomfiletest-" + UUID.randomUUID().toString();

        dataLakeFileClient =  dataLakeDirectoryClient.getFileClient(fileName);
        dataLakeFileAsyncClient = dataLakeDirectoryAsyncClient.getFileAsyncClient(fileName);
    }

    @Override
    public Mono<Void> setupAsync() {
        return dataLakeFileAsyncClient.create().then(super.cleanupAsync());
    }

    public long copyStream(InputStream input, OutputStream out) throws IOException {
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = input.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }
}
