// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.storage.blob.perf.core.AbstractDownloadTest;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Use {@code downloadtofiletest} command to run this test.
 * This test requires providing connection string in {@code STORAGE_CONNECTION_STRING} environment variable.
 * It's recommended to use premium blob storage account.
 * This test includes temporary file deletion as part of scenario. Please keep in mind that this adds
 * constant component to the results.
 */
public class DownloadBlobToFileTest extends AbstractDownloadTest<BlobPerfStressOptions> {

    private final File tempDir;

    public DownloadBlobToFileTest(BlobPerfStressOptions options) {
        super(options);

        try {
            tempDir = Files.createTempDirectory("downloadToFileTest").toFile();
            tempDir.deleteOnExit();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void run() {
        File file = new File(tempDir, UUID.randomUUID().toString());
        try {
            blobClient.downloadToFile(file.getAbsolutePath());
        } finally {
            // We don't use File.deleteOnExit.
            // This would cause memory accumulation in the java.io.DeleteOnExitHook with random file names
            // and eventually take whole heap in long-running benchmarks.
            if (!file.delete()){
                throw new IllegalStateException("Unable to delete test file");
            }
        }
    }


    @Override
    public Mono<Void> runAsync() {
        File file = new File(tempDir, UUID.randomUUID().toString());
        return blobAsyncClient.downloadToFile(file.getAbsolutePath())
            .doFinally(ignored -> {
                // We don't use File.deleteOnExit.
                // This would cause memory accumulation in the java.io.DeleteOnExitHook with random file names
                // and eventually take whole heap in long-running benchmarks.
                if (!file.delete()){
                    throw new IllegalStateException("Unable to delete test file");
                }
            })
            .then();
    }
}
