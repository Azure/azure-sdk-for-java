package com.azure.storage.blob.stress.scenarios;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.stress.BlobStorageStressRunner;
import com.azure.storage.blob.stress.builders.DownloadToFileScenarioBuilder;
import com.azure.storage.blob.stress.scenarios.infra.BlobStressScenario;
import com.azure.storage.stress.RandomInputStream;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.CRC32;

import static com.azure.storage.stress.HttpFaultInjectingHttpClient.FAULT_TRACKING_CONTEXT_KEY;

public class DownloadToFileStressScenario extends BlobStressScenario<DownloadToFileScenarioBuilder> {

    private static final ClientLogger LOGGER = new ClientLogger(DownloadToFileScenarioBuilder.class);

    private static Path originalDataPath;
    private static long originalDataChecksum;

    private final Path directoryPath;

    public DownloadToFileStressScenario(DownloadToFileScenarioBuilder builder) {
        super(builder, /*singletonBlob*/true, /*initializeBlob*/true);
        this.directoryPath = builder.getDirectoryPath();
    }

    @Override
    public void run(Duration timeout) {
        long endTimeNano = System.nanoTime() + timeout.toNanos();
        long timeoutNano;
        while ((timeoutNano = endTimeNano - System.nanoTime()) > 0) {
            Path downloadPath = directoryPath.resolve(UUID.randomUUID() + ".txt");
            try {
                Queue<String> faultTypes = new ConcurrentLinkedQueue<>();
                Context context = new Context(FAULT_TRACKING_CONTEXT_KEY, faultTypes);
                BlobDownloadToFileOptions options = new BlobDownloadToFileOptions(downloadPath.toString());

                getSyncBlobClient().downloadToFileWithResponse(options, Duration.ofNanos(timeoutNano), context);

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
            }
        }
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException("not implemented");
//        Path downloadPath = directoryPath.resolve(UUID.randomUUID() + ".txt");

//        Queue<String> faultTypes = new ConcurrentLinkedQueue<>();
//        Context context = new Context(FAULT_TRACKING_CONTEXT_KEY, faultTypes);
//        BlobDownloadToFileOptions options = new BlobDownloadToFileOptions(downloadPath.toString());

//        return getAsyncBlobClient()
//            .downloadToFile(downloadPath.toString())
//            .then(Mono.defer(() -> validateDownloadedContentsAsync(downloadPath)));
    }

    private void validateDownloadedContents(Path downloadPath) {
        // Use crc to check for file mismatch, avoiding every parallel test streaming original data from disk
        // If there's a mismatch, the original data can be streamed to check where the fault occurred
        // Data is streamed in the first place to avoid holding potentially gigs in memory
        CRC32 dataCrc = new CRC32();
        try (InputStream file = Files.newInputStream(downloadPath)) {
            byte[] buf = new byte[4 * 1024 * 1024];
            int read;
            while ((read = file.read(buf)) != -1) {
                dataCrc.update(buf, 0, read);
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        long crc = dataCrc.getValue();
        if (crc != originalDataChecksum) {
            // future: if mismatch, compare against original file
            throw new RuntimeException("mismatched crc");
        }
    }

    private Mono<Void> validateDownloadedContentsAsync(Path downloadPath) {
        CRC32 dataCrc = new CRC32();
        return BinaryData.fromFile(downloadPath).toFluxByteBuffer()
            .map(bb -> {
                dataCrc.update(bb);
                return null;
            })
            .then(Mono.defer(() -> {
                long crc = dataCrc.getValue();
                if (crc != originalDataChecksum) {
                    // future: if mismatch, compare against original file
                    return Mono.error(new Exception("mismatched crc"));
                }
                return Mono.empty();
            }));
    }

    @Override
    protected void setupBlob() {
        originalDataPath = directoryPath.resolve("original-data-" + UUID.randomUUID());
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
