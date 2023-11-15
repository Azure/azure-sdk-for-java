package com.azure.storage.blob.stress.scenarios;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.stress.builders.DownloadToFileScenarioBuilder;
import com.azure.storage.blob.stress.scenarios.infra.BlobStressScenario;
import com.azure.storage.stress.RandomInputStream;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.zip.CRC32;

public class DownloadToFileStressScenario extends BlobStressScenario<DownloadToFileScenarioBuilder> {

    private static final ClientLogger LOGGER = new ClientLogger(DownloadToFileScenarioBuilder.class);
    private long originalDataChecksum;
    private final ByteBuffer originalContentHead;
    private final Path originalDataPath;
    private final Path directoryPath;
    private final int blobPrintableSize;

    public DownloadToFileStressScenario(DownloadToFileScenarioBuilder builder) {
        super(builder, /*initializeBlob*/true);
        this.directoryPath = builder.getDirectoryPath();
        this.originalDataPath = directoryPath.resolve("original-data-" + UUID.randomUUID());
        this.blobPrintableSize = (int) Math.min(builder.getBlobSize(), 1024);
        this.originalContentHead = ByteBuffer.allocate(blobPrintableSize);
    }

    @Override
    public void run(Duration timeout) {
        long endTimeNano = System.nanoTime() + timeout.toNanos();
        long timeoutNano;

        while ((timeoutNano = endTimeNano - System.nanoTime()) > 0) {
            Path downloadPath = directoryPath.resolve(UUID.randomUUID() + ".txt");
            Context span = TRACER.start("downloadToFile", Context.NONE);
            AutoCloseable s = TRACER.makeSpanCurrent(span);
            try {
                BlobDownloadToFileOptions options = new BlobDownloadToFileOptions(downloadPath.toString());

                getSyncBlobClient().downloadToFileWithResponse(options, Duration.ofNanos(timeoutNano), Context.NONE);
                if (validateDownloadedContents(downloadPath, span)) {
                    trackSuccess(span);
                } else {
                    trackMismatch(span);
                }
            } catch (Exception e) {
                if (e.getMessage().contains("Timeout on blocking read") || e instanceof InterruptedException || e instanceof TimeoutException) {
                    trackCancellation(span);
                } else {
                    trackFailure(span, e);
                }
            } finally {
                downloadPath.toFile().delete();
                closeScope(s);
            }
        }
    }

    @Override
    public Mono<Void> runAsync() {
        Path downloadPath = directoryPath.resolve(UUID.randomUUID() + ".txt");
        BlobDownloadToFileOptions options = new BlobDownloadToFileOptions(downloadPath.toString());

        Context span = TRACER.start("downloadToFileAsync", Context.NONE);
        return getAsyncBlobClient()
                .downloadToFileWithResponse(options)
                .flatMap(ignored -> validateDownloadedContentsAsync(downloadPath, span))
                .doOnCancel(() -> trackCancellation(span))
                .doOnError(e -> trackFailure(span, e))
                .doOnNext(match -> {
                    if (match) {
                        trackSuccess(span);
                    } else {
                        trackMismatch(span);
                    }
                })
                .doFinally(i -> downloadPath.toFile().delete())
                .contextWrite(reactor.util.context.Context.of("TRACING_CONTEXT", span))
                .then();
    }

    private static void closeScope(AutoCloseable scope) {
        try {
            scope.close();
        } catch (Exception e) {
            // ignore
        }
    }

    private boolean validateDownloadedContents(Path downloadPath, Context span) {
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

        return checkMatch(dataCrc, length, contentHead, span);
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
            .map(l -> checkMatch(dataCrc, l, contentHead, span));
    }

    private boolean checkMatch(CRC32 dataCrc, Long length, ByteBuffer contentHead, Context span) {
        long crc = dataCrc.getValue();
        if (crc != originalDataChecksum) {
            logMismatch(crc, length, contentHead, span);
            return false;
        }
        return true;
    }

    private void logMismatch(long actualCrc, long actualLength, ByteBuffer contentHead, Context span) {
        // future: if mismatch, compare against original file
        AutoCloseable scope = TRACER.makeSpanCurrent(span);
        LOGGER.atError()
                .addKeyValue("expectedCrc", originalDataChecksum)
                .addKeyValue("actualCrc", actualCrc)
                .addKeyValue("expectedLength", getBlobSize())
                .addKeyValue("actualLength", actualLength)
                .addKeyValue("originalContentHead", () -> Base64.getEncoder().encodeToString(originalContentHead.array()))
                .addKeyValue("actualContentHead", () -> Base64.getEncoder().encodeToString(contentHead.array()))
                .log("mismatched crc");
        closeScope(scope);
    }

    @Override
    protected void setupBlob() {
        try (InputStream data = new RandomInputStream(getBlobSize());
             OutputStream file = Files.newOutputStream(originalDataPath);
             OutputStream blob = getSyncBlobClientNoFault().getBlockBlobClient().getBlobOutputStream(true)) {

            CRC32 dataCrc = new CRC32();

            byte[] buf = new byte[4 * 1024 * 1024];
            int read;
            while ((read = data.read(buf)) != -1) {
                file.write(buf, 0, read);
                blob.write(buf, 0, read);
                dataCrc.update(buf, 0, read);
                if (originalContentHead.hasRemaining()) {
                    originalContentHead.put(buf, 0, Math.min(read, originalContentHead.remaining()));
                }
            }
            file.flush();
            blob.flush();
            file.close();
            blob.close();

            originalDataChecksum = dataCrc.getValue();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
