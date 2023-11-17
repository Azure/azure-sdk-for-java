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
import com.azure.storage.blob.stress.utils.TelemetryHelper;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.zip.CRC32;

public class DownloadToFile extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(DownloadToFile.class);
    private static final TelemetryHelper TELEMETRY_HELPER = new TelemetryHelper(DownloadToFile.class.getName());
    private final Path directoryPath;
    private final int blobPrintableSize;
    private static final OriginalContent ORIGINAL_CONTENT = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public DownloadToFile(StorageStressOptions options) {
        super(options, TELEMETRY_HELPER);
        this.directoryPath = getTempPath("test");
        this.blobPrintableSize = (int) Math.min(options.getSize(), 1024);
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
            return validateDownloadedContents(downloadPath);
        } finally {
            deleteFile(downloadPath);
        }
    }

    @Override
    protected Mono<Boolean> runInternalAsync(Context span) {
        Path downloadPath = directoryPath.resolve(UUID.randomUUID() + ".txt");
        BlobDownloadToFileOptions blobOptions = new BlobDownloadToFileOptions(downloadPath.toString());

        return asyncClient
                .downloadToFileWithResponse(blobOptions)
                .flatMap(ignored -> validateDownloadedContentsAsync(downloadPath, span))
                .doFinally(i -> deleteFile(downloadPath));
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

    private boolean validateDownloadedContents(Path downloadPath) {
        // Use crc to check for file mismatch, avoiding every parallel test streaming original data from disk
        // If there's a mismatch, the original data can be streamed to check where the fault occurred
        // Data is streamed in the first place to avoid holding potentially gigs in memory
        long length = 0;
        ByteBuffer contentHead = ByteBuffer.allocate(blobPrintableSize);
        CRC32 dataCrc = new CRC32();
        try (InputStream file = Files.newInputStream(downloadPath)) {
            byte[] buf = new byte[4 * 1024 * 1024];
            int read;
            while ((read = file.read(buf)) != -1) {
                dataCrc.update(buf, 0, read);
                if (contentHead.hasRemaining()) {
                    contentHead.put(buf, 0, Math.min(read, contentHead.remaining()));
                }
                length += read;
            }
        }
        catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }

        return ORIGINAL_CONTENT.checkMatch(dataCrc, length, contentHead);
    }

    private Mono<Boolean> validateDownloadedContentsAsync(Path downloadPath, Context span) {
        CRC32 dataCrc = new CRC32();
        ByteBuffer contentHead = ByteBuffer.allocate(blobPrintableSize);

        return BinaryData.fromFile(downloadPath).toFluxByteBuffer()
            .map(bb -> {
                long length = bb.remaining();
                dataCrc.update(bb);
                if (contentHead.hasRemaining()) {
                    bb.flip();
                    while (contentHead.hasRemaining() && bb.hasRemaining()) {
                        contentHead.put(bb.get());
                    }
                }

                return length;
            })
            .reduce(0L, Long::sum)
            .map(l -> {
                try(AutoCloseable scope = TELEMETRY_HELPER.getTracer().makeSpanCurrent(span)) {
                    return ORIGINAL_CONTENT.checkMatch(dataCrc, l, contentHead);
                } catch (Exception e) {
                    throw LOGGER.logExceptionAsError(new RuntimeException(e));
                }
            });
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(ORIGINAL_CONTENT.setupBlob(asyncNoFaultClient, options.getSize(), blobPrintableSize));
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
