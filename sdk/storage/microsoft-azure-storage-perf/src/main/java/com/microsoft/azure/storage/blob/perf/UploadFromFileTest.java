// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob.perf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.storage.StorageException;

import com.microsoft.azure.storage.blob.perf.core.BlobTestBase;
import reactor.core.publisher.Mono;

public class UploadFromFileTest extends BlobTestBase<PerfStressOptions> {

    private static final Path TEMP_FILE;
    private static final String TEMP_FILE_PATH;

    static {
        try {
            TEMP_FILE = Files.createTempFile(null, null);
            TEMP_FILE_PATH = TEMP_FILE.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public UploadFromFileTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(createTempFile());
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return deleteTempFile().then(super.globalCleanupAsync());
    }

    private Mono<Void> createTempFile() {
        return Mono.fromCallable(() -> {
            TestDataCreationHelper.writeToFile(TEMP_FILE_PATH, options.getSize(), DEFAULT_BUFFER_SIZE);
            return 1;
        }).then();
    }

    private Mono<Void> deleteTempFile() {
        try {
            Files.delete(TEMP_FILE);
            return Mono.empty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            cloudBlockBlob.uploadFromFile(TEMP_FILE_PATH);
        } catch (StorageException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }
}
