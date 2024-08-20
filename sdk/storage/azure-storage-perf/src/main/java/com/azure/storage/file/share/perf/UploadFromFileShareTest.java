// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.azure.storage.file.share.perf.core.FileTestBase;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UploadFromFileShareTest extends FileTestBase<PerfStressOptions> {
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

    public UploadFromFileShareTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(Mono.fromRunnable(this::createTempFile));
    }

    @Override
    public void globalSetup() {
        super.globalSetup();
        createTempFile();
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return Mono.fromRunnable(this::deleteTempFile).then(super.globalCleanupAsync());
    }

    @Override
    public void globalCleanup() {
        deleteTempFile();
        super.globalCleanup();
    }

    private void createTempFile() {
        TestDataCreationHelper.writeToFile(TEMP_FILE_PATH, options.getSize(), DEFAULT_BUFFER_SIZE);
    }

    private void deleteTempFile() {
        try {
            Files.delete(TEMP_FILE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        shareFileClient.uploadFromFile(TEMP_FILE_PATH);
    }

    @Override
    public Mono<Void> runAsync() {
        return shareFileAsyncClient.uploadFromFile(TEMP_FILE_PATH);
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(Mono.defer(() -> shareFileAsyncClient
            .create(options.getSize() + DEFAULT_BUFFER_SIZE))).then();
    }
}
