package com.microsoft.azure.storage.file.share.perf.core;// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.UUID;

public abstract class FileTestBase<TOptions extends PerfStressOptions> extends DirectoryTest<TOptions> {

    public static final int DEFAULT_BUFFER_SIZE = 8192;
    protected final CloudFile cloudFile;

    public FileTestBase(TOptions options) {
        super(options);

        String fileName = "randomfiletest-" + UUID.randomUUID().toString();

        try {
            cloudFile =  cloudFileDirectory.getFileReference(fileName);
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.defer(() -> {
            try {
                cloudFile.delete();
            } catch (StorageException | URISyntaxException e) {
                return Mono.error(e);
            }
            return Mono.empty();
        }).then(super.cleanupAsync());
    }
}
