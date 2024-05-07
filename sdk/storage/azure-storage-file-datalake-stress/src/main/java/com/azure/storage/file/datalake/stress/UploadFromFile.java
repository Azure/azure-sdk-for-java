// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.stress;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.stress.utils.OriginalContent;
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

public class UploadFromFile extends DataLakeScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(UploadFromFile.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final DataLakeFileClient syncClient;
    private final DataLakeFileAsyncClient asyncClient;
    private final DataLakeFileClient syncNoFaultClient;
    private final DataLakeFileAsyncClient asyncNoFaultClient;

    public UploadFromFile(StorageStressOptions options) {
        super(options);
        String fileName = generateFileName();
        this.syncClient = getSyncFileSystemClient().getFileClient(fileName);
        this.asyncClient = getAsyncFileSystemClient().getFileAsyncClient(fileName);
        this.syncNoFaultClient = getSyncFileSystemClientNoFault().getFileClient(fileName);
        this.asyncNoFaultClient = getAsyncFileSystemClientNoFault().getFileAsyncClient(fileName);
    }


    @Override
    protected void runInternal(Context span) {
        Path downloadPath = getTempPath("test");
        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getContentHead(), options.getSize())) {
            Path uploadFilePath = generateFile(inputStream);
            downloadPath = downloadPath.resolve(CoreUtils.randomUuid() + ".txt");
            syncClient.uploadFromFileWithResponse(uploadFilePath.toString(),
                new ParallelTransferOptions()
                    .setMaxSingleUploadSizeLong(4 * 1024 * 1024L).setMaxConcurrency(1), null, null, null, null, span);
            // then download file using no fault client to verify the content
            syncNoFaultClient.readToFileWithResponse(downloadPath.toString(), null, null, null, null, false, null, null, span);
            originalContent.checkMatch(BinaryData.fromFile(downloadPath), span).block();
        } finally {
            deleteFile(downloadPath);
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        Path downloadPath = getTempPath("test");
        return Mono.using(
            () -> new CrcInputStream(originalContent.getContentHead(), options.getSize()),
            inputStream -> {
                Path uploadFilePath = generateFile(inputStream);
                return Mono.using(
                    () -> downloadPath.resolve(UUID.randomUUID() + ".txt"),
                    path -> asyncClient.uploadFromFileWithResponse(uploadFilePath.toString(),
                            new ParallelTransferOptions().setMaxSingleUploadSizeLong(4 * 1024 * 1024L).setMaxConcurrency(1),
                            null, null, null)
                        .flatMap(ignored -> asyncNoFaultClient.readToFile(path.toString(), true)
                        )
                        .flatMap(ignored -> originalContent.checkMatch(BinaryData.fromFile(path), span)),
                    UploadFromFile::deleteFile);
            },
            CrcInputStream::close);
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(originalContent.setupFile(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.delete()
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
