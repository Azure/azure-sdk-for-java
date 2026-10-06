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
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

/**
 * Download to file with CRC64 Algorithm enabled.
 * Verifies the correctness of the download response content via CRC.
 */
public class DownloadToFileWithCRC64 extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(DownloadToFileWithCRC64.class);
    private final Path directoryPath;
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;
    private final ParallelTransferOptions parallelTransferOptions;

    public DownloadToFileWithCRC64(StorageStressOptions options) {
        super(options);
        this.directoryPath = getTempPath("test");
        String blobName = generateBlobName();
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(blobName);
        this.parallelTransferOptions = new ParallelTransferOptions()
            .setMaxConcurrency(options.getMaxConcurrency());
    }

    @Override
    protected void runInternal(Context span) {
        Path downloadPath = directoryPath.resolve(UUID.randomUUID() + ".txt");
        BlobDownloadToFileOptions blobOptions = new BlobDownloadToFileOptions(downloadPath.toString())
            .setParallelTransferOptions(parallelTransferOptions)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

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
            path -> asyncClient.downloadToFileWithResponse(
                new BlobDownloadToFileOptions(path.toString())
                    .setParallelTransferOptions(parallelTransferOptions)
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64))
                .flatMap(ignored -> originalContent.checkMatch(BinaryData.fromFile(path), span)),
            DownloadToFileWithCRC64::deleteFile);
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(originalContent.setupBlob(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.deleteIfExists()
            .then(super.cleanupAsync());
    }

    private Path getTempPath(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private static void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Throwable e) {
            LOGGER.atError()
                .addKeyValue("path", path)
                .log("failed to delete file", e);
        }
    }
}
