// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomInputStream;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.perf.core.BlobTestBase;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import reactor.core.publisher.Mono;

public class UploadFromFileTest extends BlobTestBase<PerfStressOptions> {

    private static final Path TEMP_FILE;

    static {
        try {
            TEMP_FILE = Files.createTempFile(null, null);
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
        try (InputStream inputStream = createRandomInputStream(options.getSize());
             OutputStream outputStream = new FileOutputStream(TEMP_FILE.toString())) {
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
        blobClient.uploadFromFile(TEMP_FILE.toString(), true);
    }

    @Override
    public Mono<Void> runAsync() {
        return blobAsyncClient.uploadFromFile(TEMP_FILE.toString(), true);
    }
}
