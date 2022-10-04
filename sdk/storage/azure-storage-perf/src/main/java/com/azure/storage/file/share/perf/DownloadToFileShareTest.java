// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.perf.core.DirectoryTest;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.UUID;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

/**
 * Use {@code downloadtofileshare} command to run this test.
 * This test requires providing connection string in {@code STORAGE_CONNECTION_STRING} environment variable.
 * It's recommended to use premium file share storage account.
 * This test includes temporary file deletion as part of scenario. Please keep in mind that this adds
 * constant component to the results.
 */
public class DownloadToFileShareTest extends DirectoryTest<PerfStressOptions> {

    protected final ShareFileClient shareFileClient;
    protected final ShareFileAsyncClient shareFileAsyncClient;

    private final File tempDir;

    public DownloadToFileShareTest(PerfStressOptions options) {
        super(options);
        String fileName = "perfstressdfile" + UUID.randomUUID();
        shareFileClient = shareDirectoryClient.getFileClient(fileName);
        shareFileAsyncClient = shareDirectoryAsyncClient.getFileClient(fileName);

        try {
            tempDir = Files.createTempDirectory("downloadToFileTest").toFile();
            tempDir.deleteOnExit();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(shareFileAsyncClient.create(options.getSize()))
            .then(shareFileAsyncClient.upload(createRandomByteBufferFlux(options.getSize()), new ParallelTransferOptions()))
            .then();
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        File file = new File(tempDir, UUID.randomUUID().toString());
        try {
            shareFileClient.downloadToFile(file.getAbsolutePath());
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
        return shareFileAsyncClient.downloadToFile(file.getAbsolutePath())
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
