// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.stress;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.stress.utils.OriginalContent;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class DownloadToFile extends ShareScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(DownloadToFile.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final Path directoryPath;
    private final ShareFileClient syncClient;
    private final ShareFileAsyncClient asyncClient;
    private final ShareFileAsyncClient asyncNoFaultClient;

    public DownloadToFile(StorageStressOptions options) {
        super(options);
        this.directoryPath = getTempPath("test");
        String fileName = generateFileName();
        this.syncClient = getSyncShareClient().getFileClient(fileName);
        this.asyncClient = getAsyncShareClient().getFileClient(fileName);
        this.asyncNoFaultClient = getAsyncShareClientNoFault().getFileClient(fileName);
    }

    @Override
    protected void runInternal(Context span) {
        Path downloadPath = directoryPath.resolve(UUID.randomUUID() + ".txt");
        try {
            syncClient.downloadToFileWithResponse(downloadPath.toString(), null, null, span);
            originalContent.checkMatch(BinaryData.fromFile(downloadPath), span).block();
        } finally {
            deleteFile(downloadPath);
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        return Mono.using(
            () -> directoryPath.resolve(UUID.randomUUID() + ".txt"),
            path -> asyncClient.downloadToFile(path.toString())
                .flatMap(ignored -> originalContent.checkMatch(BinaryData.fromFile(path), span)),
            DownloadToFile::deleteFile);
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(originalContent.setupFile(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.deleteIfExists().then(super.cleanupAsync());
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

    private Path getTempPath(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }
}
