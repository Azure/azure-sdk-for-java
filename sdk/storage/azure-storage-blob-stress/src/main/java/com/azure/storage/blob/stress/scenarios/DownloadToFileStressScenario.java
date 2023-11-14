package com.azure.storage.blob.stress.scenarios;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.stress.builders.DownloadToFileScenarioBuilder;
import com.azure.storage.blob.stress.scenarios.infra.BlobStressScenario;
import com.azure.storage.stress.RandomInputStream;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32;

import static com.azure.storage.stress.HttpFaultInjectingHttpClient.FAULT_TRACKING_CONTEXT_KEY;

public class DownloadToFileStressScenario extends BlobStressScenario<DownloadToFileScenarioBuilder> {

    private static final ClientLogger LOGGER = new ClientLogger(DownloadToFileScenarioBuilder.class);
    private static final Tracer TRACER = GlobalOpenTelemetry.getTracer("DownloadToFileStressScenario");
    // TODO: move setUp and originalDataChecksum to DownloadToFileScenarioBuilder
    private static long originalDataChecksum;
    private static byte[] originalContentHead = new byte[1024];

    private final Path originalDataPath;
    private final Path directoryPath;

    public DownloadToFileStressScenario(DownloadToFileScenarioBuilder builder) {
        super(builder, /*singletonBlob*/true, /*initializeBlob*/true);
        this.directoryPath = builder.getDirectoryPath();
        this.originalDataPath = directoryPath.resolve("original-data-" + UUID.randomUUID());
    }

    @Override
    public void run(Duration timeout) {
        long endTimeNano = System.nanoTime() + timeout.toNanos();
        long timeoutNano;

        while ((timeoutNano = endTimeNano - System.nanoTime()) > 0) {
            Path downloadPath = directoryPath.resolve(UUID.randomUUID() + ".txt");
            Span span = TRACER.spanBuilder("downloadToFile").startSpan();
            Scope s = span.makeCurrent();
            try {
                BlobDownloadToFileOptions options = new BlobDownloadToFileOptions(downloadPath.toString());

                getSyncBlobClient().downloadToFileWithResponse(options, Duration.ofNanos(timeoutNano), Context.NONE);

                validateDownloadedContents(downloadPath);
                logSuccess();
                LOGGER.info("success");
            } catch (Exception e) {
                if (e.getMessage().contains("Timeout on blocking read")) {
                    // test timed out, so break out of loop instead of counting as a failure
                    break;
                }
                LOGGER.error("failure", e);
                logFailure(e.getMessage());
                span.setStatus(StatusCode.ERROR, e.getMessage());
            } finally {
                s.close();
                span.end();
            }
        }
    }

    // do we need to delete the files after exit?
    @Override
    public Mono<Void> runAsync() {
        Path downloadPath = directoryPath.resolve(UUID.randomUUID() + ".txt");

        Queue<String> faultTypes = new ConcurrentLinkedQueue<>();
        Context context = new Context(FAULT_TRACKING_CONTEXT_KEY, faultTypes);
        BlobDownloadToFileOptions options = new BlobDownloadToFileOptions(downloadPath.toString());

        return getAsyncBlobClient()
            .downloadToFileWithResponse(options)
            .then(Mono.defer(() -> validateDownloadedContentsAsync(downloadPath)));
        //throw new UnsupportedOperationException("not implemented");
    }

    private void validateDownloadedContents(Path downloadPath) {
        // Use crc to check for file mismatch, avoiding every parallel test streaming original data from disk
        // If there's a mismatch, the original data can be streamed to check where the fault occurred
        // Data is streamed in the first place to avoid holding potentially gigs in memory
        int length = 0;
        byte[] contentHead = new byte[1024];
        CRC32 dataCrc = new CRC32();
        try (InputStream file = Files.newInputStream(downloadPath)) {
            byte[] buf = new byte[4 * 1024 * 1024];
            int read;
            boolean first = true;
            while ((read = file.read(buf)) != -1) {
                dataCrc.update(buf, 0, read);
                if (first) {
                    System.arraycopy(buf, 0, contentHead, 0, Math.min(read, contentHead.length));
                    first = false;
                }
                length += read;
            }
        }
        catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }

        long crc = dataCrc.getValue();
        if (crc != originalDataChecksum) {
            reportMismatch(crc, length, contentHead);
        }
    }

    private Mono<Void> validateDownloadedContentsAsync(Path downloadPath) {
        CRC32 dataCrc = new CRC32();
        AtomicLong length = new AtomicLong();
        Flux<Void> check = BinaryData.fromFile(downloadPath).toFluxByteBuffer()
            .map(bb -> {
                length.addAndGet(bb.remaining());
                dataCrc.update(bb);
                return null;
            });

        return check
            .doFinally(i -> {
                long crc = dataCrc.getValue();
                if (crc != originalDataChecksum) {
                    reportMismatch(crc, length.get(), new byte[0]);
                }
            })
            .then();
    }

    private void reportMismatch(long actualCrc, long actualLength, byte[] contentHead) {
        // future: if mismatch, compare against original file
        throw LOGGER.atError()
            .addKeyValue("expectedCrc", originalDataChecksum)
            .addKeyValue("actualCrc", actualCrc)
            .addKeyValue("expectedLength", getBlobSize())
            .addKeyValue("actualLength", actualLength)
            .addKeyValue("originalContentHead", () -> Base64.getEncoder().encodeToString(originalContentHead))
            .addKeyValue("actualContentHead", () -> Base64.getEncoder().encodeToString(contentHead))
            .log(new RuntimeException("mismatched crc"));
    }

    @Override
    protected void setupBlob() {
        try (InputStream data = new RandomInputStream(getBlobSize());
             OutputStream file = Files.newOutputStream(originalDataPath);
             OutputStream blob = getSyncBlobClientNoFault().getBlockBlobClient().getBlobOutputStream(true)) {

            CRC32 dataCrc = new CRC32();

            byte[] buf = new byte[4 * 1024 * 1024];
            int read;
            boolean first = true;
            while ((read = data.read(buf)) != -1) {
                file.write(buf, 0, read);
                blob.write(buf, 0, read);
                dataCrc.update(buf, 0, read);
                if (first) {
                    System.arraycopy(buf, 0, originalContentHead, 0, Math.min(read, originalContentHead.length));
                    first = false;
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
