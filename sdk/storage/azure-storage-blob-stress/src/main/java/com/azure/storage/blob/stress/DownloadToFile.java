// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

public class DownloadToFile extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(DownloadToFile.class);
    private final Path directoryPath;
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public DownloadToFile(StorageStressOptions options) {
        super(options);
        this.directoryPath = getTempPath("test");
        String blobName = generateBlobName();
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(blobName);
    }

    @Override
    protected void runInternal(Context span) {
        Path downloadPath = directoryPath.resolve(UUID.randomUUID() + ".txt");
        BlobDownloadToFileOptions blobOptions = new BlobDownloadToFileOptions(downloadPath.toString());

        try {
            syncClient.downloadToFileWithResponse(blobOptions, Duration.ofSeconds(options.getDuration()), span);
            originalContent.checkMatch(BinaryData.fromFile(downloadPath), span).block();
        } finally {
            deleteFile(downloadPath);
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        return Mono.using(
            () -> directoryPath.resolve(UUID.randomUUID() + ".txt"),
            path -> asyncClient.downloadToFileWithResponse(new BlobDownloadToFileOptions(path.toString()))
                .flatMap(ignored -> originalContent.checkMatch(BinaryData.fromFile(path), span)),
            DownloadToFile::deleteFile);
    }

    private static void deleteFile(Path path) {
        try {
            path.toFile().delete();
        } catch (Throwable e) {
            LOGGER.atError()
                .addKeyValue("path", path)
                .log("failed to delete file", e);
        }
    }

    @Override
    public Mono<Void> setupAsync() {
        // setup is called for each instance of scenario. Number of instances equals options.getParallel()
        // so we're setting up options.getParallel() blobs to scale beyond service limits for 1 blob.
        return super.setupAsync()
            .then(originalContent.setupBlob(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        LOGGER.info("before calling deleteIfExists");
        return asyncNoFaultClient.deleteIfExists()
            .doFirst(() -> {
                // Log before cleanup
                System.out.println("Cleaning up...");
                LOGGER.info("Before calling cleanup on blob {}", asyncNoFaultClient.getBlobUrl());
            })
            .then(super.cleanupAsync())
            .doFinally(aVoid -> {
                // Log on successful completion
                System.out.println("Cleanup completed successfully.");
                LOGGER.info("Deleted blob {}", asyncNoFaultClient.getBlobUrl());
            });
    }

    private Path getTempPath(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }
}
