// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob.perf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.storage.StorageException;

import com.microsoft.azure.storage.blob.perf.core.BlobTestBase;
import reactor.core.publisher.Mono;

public class UploadFromFileTest extends BlobTestBase<PerfStressOptions> {
    private static Path tempFile;

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
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                tempFile = Files.createTempFile(null, null);
                inputStream = TestDataCreationHelper.createRandomInputStream(options.getSize());
                outputStream = new FileOutputStream(tempFile.toString());
                TestDataCreationHelper.copyStream(inputStream, outputStream, DEFAULT_BUFFER_SIZE);
                return 1;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }

                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }).then();
    }

    private Mono<Void> deleteTempFile() {
        try {
            Files.delete(tempFile);
            return Mono.empty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            cloudBlockBlob.uploadFromFile(tempFile.toString());
        } catch (StorageException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }
}
