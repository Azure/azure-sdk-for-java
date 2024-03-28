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
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class UploadFromFile extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(UploadFromFile.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobClient syncNoFaultClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public UploadFromFile(StorageStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncNoFaultClient = getSyncContainerClientNoFault().getBlobClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(blobName);
    }

    @Override
    protected void runInternal(Context span) {
        // first upload file using faulted client
        Path downloadPath = getTempPath("test");
        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize())) {
            Path uploadFilePath = generateFile(inputStream);
            downloadPath = downloadPath.resolve(CoreUtils.randomUuid() + ".txt");
            syncClient.uploadFromFileWithResponse(new BlobUploadFromFileOptions(uploadFilePath.toString())
                .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong(4 * 1024 * 1024L)),
                null, span);
            // then download file using no fault client to verify the content
            syncNoFaultClient.downloadToFileWithResponse(
                new BlobDownloadToFileOptions(downloadPath.toString()), null, span);
            originalContent.checkMatch(BinaryData.fromFile(downloadPath), span).block();
        } finally {
            deleteFile(downloadPath);
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        Path downloadPath = getTempPath("test");
        return Mono.using(
            () -> new CrcInputStream(originalContent.getBlobContentHead(), options.getSize()),
            inputStream -> {
                Path uploadFilePath = generateFile(inputStream);
                return Mono.using(
                    () -> downloadPath.resolve(UUID.randomUUID() + ".txt"),
                    path -> asyncClient.uploadFromFileWithResponse(new BlobUploadFromFileOptions(uploadFilePath.toString())
                            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong(4 * 1024 * 1024L)))
                        .flatMap(ignored -> asyncNoFaultClient.downloadToFileWithResponse(
                            new BlobDownloadToFileOptions(path.toString()))
                        )
                        .flatMap(ignored -> originalContent.checkMatch(BinaryData.fromFile(path), span)),
                    UploadFromFile::deleteFile);
            },
            CrcInputStream::close);
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
            path.toFile().delete();
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
