// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.file.share.perf.core.FileTestBase;
import reactor.core.publisher.Mono;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomInputStream;

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
        return super.globalSetupAsync().then(createTempFile());
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return deleteTempFile().then(super.globalCleanupAsync());
    }

    private Mono<Void> createTempFile() {
        try (InputStream inputStream = createRandomInputStream(options.getSize());
             OutputStream outputStream = new FileOutputStream(TEMP_FILE_PATH)) {
            copyStream(inputStream, outputStream);
            return Mono.empty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        shareFileClient.uploadFromFile(TEMP_FILE_PATH);
    }

    @Override
    public Mono<Void> runAsync() {
        return shareFileAsyncClient.uploadFromFile(TEMP_FILE_PATH);
    }
}
