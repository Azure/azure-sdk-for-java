// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcInputStream;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Upload from file with {@link BlobUploadFromFileOptions#setRequestChecksumAlgorithm}.
 */
public class ContentValidationUploadFromFile extends BlobScenarioBase<ContentValidationStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(ContentValidationUploadFromFile.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobClient syncNoFaultClient;
    private final BlobAsyncClient asyncNoFaultClient;
    private final ParallelTransferOptions parallelTransferOptions;

    public ContentValidationUploadFromFile(ContentValidationStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncNoFaultClient = getSyncContainerClientNoFault().getBlobClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(blobName);
        this.parallelTransferOptions = new ParallelTransferOptions()
            .setMaxConcurrency(options.getMaxConcurrency())
            .setMaxSingleUploadSizeLong(4 * 1024 * 1024L);
    }

    @Override
    protected void runInternal(Context span) {
        Path downloadPath = getTempPath("test");
        Path uploadFilePath = null;
        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize())) {
            uploadFilePath = generateFile(inputStream);
            downloadPath = downloadPath.resolve(CoreUtils.randomUuid() + ".txt");
            syncClient.uploadFromFileWithResponse(new BlobUploadFromFileOptions(uploadFilePath.toString())
                    .setParallelTransferOptions(parallelTransferOptions)
                    .setRequestChecksumAlgorithm(options.getRequestChecksumAlgorithm()),
                null, span);
            syncNoFaultClient.downloadToFileWithResponse(
                new BlobDownloadToFileOptions(downloadPath.toString()), null, span);
            originalContent.checkMatch(BinaryData.fromFile(downloadPath), span).block();
        } finally {
            deleteFile(downloadPath);
            deleteFile(uploadFilePath);
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        Path downloadPath = getTempPath("test");
        // This is written differently than the other runInternalAsync methods because uploadFromFile requires a file
        // path, so we need to generate the temp file.
        return Mono.using(
            () -> new CrcInputStream(originalContent.getBlobContentHead(), options.getSize()),
            inputStream -> uploadAndVerifyAsync(inputStream, downloadPath, span),
            CrcInputStream::close);
    }

    private Mono<Void> uploadAndVerifyAsync(CrcInputStream inputStream, Path downloadDir, Context span) {
        Path uploadFilePath = generateFile(inputStream);
        Path downloadFilePath = downloadDir.resolve(UUID.randomUUID() + ".txt");

        return asyncClient.uploadFromFileWithResponse(new BlobUploadFromFileOptions(uploadFilePath.toString())
                .setParallelTransferOptions(parallelTransferOptions)
                .setRequestChecksumAlgorithm(options.getRequestChecksumAlgorithm()))
            .flatMap(ignored -> asyncNoFaultClient.downloadToFileWithResponse(
                new BlobDownloadToFileOptions(downloadFilePath.toString())))
            .flatMap(ignored -> originalContent.checkMatch(BinaryData.fromFile(downloadFilePath), span))
            .doFinally(signal -> {
                deleteFile(uploadFilePath);
                deleteFile(downloadFilePath);
            });
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(originalContent.setupBlob(asyncNoFaultClient, options.getSize()));
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

    private static Path generateFile(InputStream inputStream) {
        try {
            File file = Files.createTempFile(CoreUtils.randomUuid().toString(), ".txt").toFile();
            file.deleteOnExit();
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return file.toPath();
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }
}
