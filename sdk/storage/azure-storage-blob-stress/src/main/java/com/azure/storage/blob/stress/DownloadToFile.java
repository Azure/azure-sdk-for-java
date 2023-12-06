// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.blob.stress.utils.TelemetryHelper;
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
    private static final TelemetryHelper TELEMETRY_HELPER = new TelemetryHelper(DownloadToFile.class);
    private final Path directoryPath;
    private static final OriginalContent ORIGINAL_CONTENT = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public DownloadToFile(StorageStressOptions options) {
        super(options, TELEMETRY_HELPER);
        this.directoryPath = getTempPath("test");
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(options.getBlobName());
        this.syncClient = getSyncContainerClient().getBlobClient(options.getBlobName());
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(options.getBlobName());
    }

    @Override
    protected boolean runInternal(Context span) {
        Path downloadPath = directoryPath.resolve(UUID.randomUUID() + ".txt");
        BlobDownloadToFileOptions blobOptions = new BlobDownloadToFileOptions(downloadPath.toString());

        try {
            syncClient.downloadToFileWithResponse(blobOptions, Duration.ofSeconds(options.getDuration()), span);
            return Boolean.TRUE.equals(ORIGINAL_CONTENT.checkMatch(downloadPath, span).block());
        } finally {
            deleteFile(downloadPath);
        }
    }

    @Override
    protected Mono<Boolean> runInternalAsync(Context span) {
        return Mono.using(
            () -> directoryPath.resolve(UUID.randomUUID() + ".txt"),
            path ->  asyncClient.downloadToFileWithResponse(new BlobDownloadToFileOptions(path.toString()))
                    .flatMap(ignored -> ORIGINAL_CONTENT.checkMatch(path, span)),
            DownloadToFile::deleteFile);
    }

    private static void deleteFile(Path path) {
        try {
            path.toFile().delete();
        } catch (Exception e) {
            LOGGER.atInfo()
                .addKeyValue("path", path)
                .log("failed to delete file", e);
        }
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(ORIGINAL_CONTENT.setupBlob(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return asyncNoFaultClient.delete()
            .then(super.globalCleanupAsync());
    }

    private Path getTempPath(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }
}
