// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.common.StorageChecksumAlgorithm;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class BlobContentValidationUploadTests extends BlobTestBase {
    private static final int TEN_MB = 10 * Constants.MB;
    private static final int FIVE_HUNDRED_MB = 500 * Constants.MB;
    /* Single-part uploads with length < 4MB use CRC64 header; >= 4MB use structured message. */
    private static final int UNDER_4MB = 2 * Constants.MB;

    /** Delay between memory perf tests so GC can run (milliseconds). */
    private static final int DELAY_BETWEEN_PERF_TESTS_MS = 2500;

    /**
     * Creates a BlobAsyncClient that records all outgoing request headers into the supplied list.
     * Each test should use its own list so tests can run concurrently.
     */
    private BlobAsyncClient createBlobAsyncClientWithRequestSniffer(List<HttpHeaders> recordedRequestHeaders) {
        HttpPipelinePolicy sniffPolicy = (context, next) -> {
            recordedRequestHeaders.add(context.getHttpRequest().getHeaders());
            return next.process();
        };
        BlobServiceAsyncClient serviceClient = getServiceAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(), sniffPolicy);
        return serviceClient.getBlobContainerAsyncClient(containerName).getBlobAsyncClient(generateBlobName());
    }

    private static boolean hasStructuredMessageHeaders(List<HttpHeaders> recordedRequestHeaders) {
        return recordedRequestHeaders.stream().anyMatch(headers -> {
            String bodyType = headers.getValue(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME);
            String contentLength = headers.getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME);
            return StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE.equals(bodyType) && contentLength != null;
        });
    }

    /**
     * Single-part upload &lt; 4MB with CRC64: content validation uses CRC64 header only (no structured message).
     */
    @Test
    @Disabled
    public void uploadWithCrc64Header() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        Response<BlockBlobItem> response = client.uploadWithResponse(options).block();
        HttpHeaders headers = response.getHeaders();

        assertNotNull(headers.getValue(X_MS_CONTENT_CRC64));
        assertNull(headers.getValue(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME));
        assertNull(headers.getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME));

        assertFalse(hasStructuredMessageHeaders(recorded));
    }

    /**
     * Single-part upload 10MB (>= 4MB) with AUTO: content validation uses structured message.
     */
    @Test
    @Disabled
    public void uploadWithStructuredMessage() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        Response<BlockBlobItem> response = client.uploadWithResponse(options).block();
        assertNotNull(response.getValue().getETag());
        StepVerifier.create(client.getProperties())
            .assertNext(p -> assertEquals(TEN_MB, p.getBlobSize()))
            .verifyComplete();

        assertTrue(hasStructuredMessageHeaders(recorded));
    }

    /**
     * Multi-part (chunked) upload with CRC64: content validation always uses structured message on each stage block.
     */
    @Test
    @Disabled
    public void uploadChunkedWithStructuredMessage() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));
        long blockSize = 2 * (long) Constants.MB;

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        Response<BlockBlobItem> response = client.uploadWithResponse(options).block();
        assertNotNull(response.getValue().getETag());
        StepVerifier.create(client.getProperties())
            .assertNext(p -> assertEquals(TEN_MB, p.getBlobSize()))
            .verifyComplete();

        assertTrue(hasStructuredMessageHeaders(recorded));
    }

    @Test
    @Disabled
    public void uploadWithNoContentValidation() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        Response<BlockBlobItem> response = client.uploadWithResponse(options).block();
        HttpHeaders headers = response.getHeaders();

        assertNull(headers.getValue(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME));
        assertNull(headers.getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME));

        assertFalse(hasStructuredMessageHeaders(recorded));
    }

    // --- Content validation upload memory usage tests (like MessageEncoderTests) ---
    // Run manually when measuring memory during upload with content validation. Skipped if heap too small.

    /**
     * When heap < 700MB, assume forceOomDuringUpload mode: skip the heap check so the policy can allocate
     * extra memory to force OOM during upload for heap dump analysis.
     */
    private void xmxToSmall(long size) {
        if (Runtime.getRuntime().maxMemory() < 700 * 1024 * 1024) {
            return; // forceOomDuringUpload mode
        }
        long maxMemory = Runtime.getRuntime().maxMemory();
        if (maxMemory < size * 5) {
            System.out.println("[ContentValidationUploadPerf] Skipping " + size + " bytes: -Xmx too small for " + size
                + " bytes (need at least " + (size * 5) + ", have " + maxMemory + ").");
        }
    }

    /*
     * Peak memory during upload is influenced by the full path from BlobAsyncClient.uploadWithResponse:
     *
     * 1. UploadUtils.uploadFullOrChunked uses PayloadSizeGate(threshold). The gate buffers incoming
     *    ByteBuffers until size > maxSingleUploadSizeLong; then it either passes through (chunked path)
     *    or, when the stream ends without breaching, all data is in the gate and uploadFull(gate.flush(), size)
     *    is used. So for single-part upload the request body is gate.flush() (replayable Flux over that buffer).
     *
     * 2. So the full payload is already held in memory (e.g. 500 MB) in the gate when the single-upload
     *    path is taken, and that body Flux is passed to BlockBlobSimpleUploadOptions and into the pipeline.
     *
     * 3. StorageContentValidationPolicy (when structured message is used) replaces the body with an
     *    encoded Flux that reads from the original body and emits segment-sized encoded ByteBuffers.
     *    The encoder uses slices when the buffer hasArray() to avoid extra copies; copyToArray only for direct buffers.
     *
     * 4. BufferStagingArea.write() (in uploadInChunks) emits overflow aggregators lazily so we don't hold
     *    all segment aggregators at once.
     *
     * WHY CHUNKED STRUCTURED MESSAGE USES ~2x MEMORY (e.g. 1026 MB vs 550 MB):
     *
     * - Chunked no validation: 500 MB source + up to maxConcurrency (8) blocks buffered in BinaryData in
     *   BlockBlobAsyncClient.stageBlockWithResponse(Flux,...) via BinaryData.fromFlux(data, length, false).
     *   So peak ≈ 500 + 8*10 MB ≈ 580 MB (plus overhead).
     *
     * - Chunked structured message: same 500 MB source and same per-block BinaryData buffering (10 MB per
     *   in-flight block). The policy then replaces the body with encodedBody = Flux.from(getBody()).limitRate(1)
     *   .concatMap(encoder::encode). The encoder emits slices (no extra copy). The extra ~500 MB peak is
     *   likely from one or both of:
     *   (a) The HTTP pipeline (e.g. RestProxy/Netty) buffering the full encoded body per request for retries,
     *       so we hold both the BinaryData copy (10 MB) and the encoded body buffer (~10 MB) per block; with
     *       many blocks in flight or queued this can approach payload size.
     *   (b) flatMapSequential or subscription order causing more block bodies (BinaryData or encoded) to be
     *       retained than the nominal maxConcurrency (e.g. prefetch or caching retaining all 50 block bodies).
     *   CONTENT_VALIDATION_MEMORY_ANALYSIS.md notes that reducing maxConcurrency to 1 did not reduce peak
     *   (~910 MB), so the duplication is not solely from concurrent blocks and may be in the policy/HTTP stack.
     *
     * Net: baseline chunked peak ≈ payload + (blockSize * maxConcurrency); with structured message add
     * encoded-body buffering and/or retention in the HTTP pipeline.
     */
    /**
     * Condition: run only when perf is explicitly enabled and JVM has enough heap for the test size.
     * Upload with no content validation (baseline).
     */
    @Test
    public void documentMemoryUsageNoValidation() {
        long size = FIVE_HUNDRED_MB;
        xmxToSmall(size);

        delayBetweenPerfTests();
        forceGc();
        long usedBefore = getHeapUsed();

        BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
        byte[] data = getRandomByteArray((int) size);
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(data));
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(body)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong(size))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        AtomicLong peakHeap = new AtomicLong(usedBefore);
        Thread sampler = startHeapSampler(peakHeap);
        try {
            client.uploadWithResponse(options).block();
        } finally {
            stopHeapSampler(sampler);
        }

        forceGc();
        long usedAfterGc = getHeapUsed();

        System.out.println("[ContentValidationUploadPerf] No validation (baseline):");
        System.out.println("  Data size: " + size + " bytes (" + (size / (1024 * 1024)) + " MB)");
        System.out.println("  Heap before: " + (usedBefore / (1024 * 1024)) + " MB");
        System.out.println("  Peak heap during upload: " + (peakHeap.get() / (1024 * 1024)) + " MB");
        System.out.println("  Heap after (post-GC): " + (usedAfterGc / (1024 * 1024)) + " MB");
        System.out.println();
    }

    /**
     * Condition: run only when perf is explicitly enabled and JVM has enough heap for the test size.
     * Single-part upload &lt; 4MB with CRC64 header (like uploadWithCrc64Header).
     */
    @Test
    public void documentMemoryUsageCrc64Header() {
        int size = UNDER_4MB;
        xmxToSmall(size);

        delayBetweenPerfTests();
        forceGc();
        long usedBefore = getHeapUsed();

        BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
        byte[] data = getRandomByteArray(size);
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(data));
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(body)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) size))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        AtomicLong peakHeap = new AtomicLong(usedBefore);
        Thread sampler = startHeapSampler(peakHeap);
        try {
            client.uploadWithResponse(options).block();
        } finally {
            stopHeapSampler(sampler);
        }

        forceGc();
        long usedAfterGc = getHeapUsed();

        System.out.println("[ContentValidationUploadPerf] CRC64 header:");
        System.out.println("  Data size: " + size + " bytes (" + (size / (1024 * 1024)) + " MB)");
        System.out.println("  Heap before: " + (usedBefore / (1024 * 1024)) + " MB");
        System.out.println("  Peak heap during upload: " + (peakHeap.get() / (1024 * 1024)) + " MB");
        System.out.println("  Heap after (post-GC): " + (usedAfterGc / (1024 * 1024)) + " MB");
        System.out.println();
    }

    /**
     * Condition: run only when perf is explicitly enabled and JVM has enough heap for the test size.
     * Single-part upload >= 4MB with structured message (like uploadWithStructuredMessage).
     */
    @Test
    public void documentMemoryUsageStructuredMessage() {
        int size = FIVE_HUNDRED_MB;
        xmxToSmall(size);

        delayBetweenPerfTests();
        forceGc();
        long usedBefore = getHeapUsed();

        BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
        byte[] data = getRandomByteArray(size);
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(data));
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(body)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) size))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.AUTO);

        AtomicLong peakHeap = new AtomicLong(usedBefore);
        Thread sampler = startHeapSampler(peakHeap);
        try {
            client.uploadWithResponse(options).block();
        } finally {
            stopHeapSampler(sampler);
        }

        forceGc();
        long usedAfterGc = getHeapUsed();

        System.out.println("[ContentValidationUploadPerf] Structured message:");
        System.out.println("  Data size: " + size + " bytes (" + (size / (1024 * 1024)) + " MB)");
        System.out.println("  Heap before: " + (usedBefore / (1024 * 1024)) + " MB");
        System.out.println("  Peak heap during upload: " + (peakHeap.get() / (1024 * 1024)) + " MB");
        System.out.println("  Heap after (post-GC): " + (usedAfterGc / (1024 * 1024)) + " MB");
        System.out.println();
    }

    /**
     * Condition: run only when perf is explicitly enabled and JVM has enough heap for the test size.
     * Chunked upload without content validation (baseline for chunked path).
     */
    @Test
    public void documentMemoryUsageChunkedNoValidation() {
        int size = FIVE_HUNDRED_MB;
        long blockSize = 10 * (long) Constants.MB;
        xmxToSmall(size);

        delayBetweenPerfTests();
        forceGc();
        long usedBefore = getHeapUsed();

        BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
        byte[] data = getRandomByteArray(size);
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(data));
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(body)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        AtomicLong peakHeap = new AtomicLong(usedBefore);
        Thread sampler = startHeapSampler(peakHeap);
        try {
            client.uploadWithResponse(options).block();
        } finally {
            stopHeapSampler(sampler);
        }

        forceGc();
        long usedAfterGc = getHeapUsed();

        System.out.println("[ContentValidationUploadPerf] Chunked no validation (baseline):");
        System.out.println("  Data size: " + size + " bytes (" + (size / (1024 * 1024)) + " MB)");
        System.out.println("  Block size: " + (blockSize / (1024 * 1024)) + " MB");
        System.out.println("  Heap before: " + (usedBefore / (1024 * 1024)) + " MB");
        System.out.println("  Peak heap during upload: " + (peakHeap.get() / (1024 * 1024)) + " MB");
        System.out.println("  Heap after (post-GC): " + (usedAfterGc / (1024 * 1024)) + " MB");
        System.out.println();
    }

    /**
     * Documents peak heap for chunked upload with structured message (pipeline-only encoding).
     * The policy materializes each block (collect + encode) and the blob layer uses concurrency 1;
     * peak varies by run; assertion caps at 1400 MB to catch regressions (no-limit case was ~1.4 GB).
     * Condition: run only when perf is explicitly enabled and JVM has enough heap for the test size.
     */
    @Test
    public void documentMemoryUsageChunkedStructuredMessage() {
        // When heap < 700MB (forceOomDuringUpload), use 200MB payload. Policy allocates 150MB on first block.
        // Use -Xmx320m to force OOM during upload (200+150=350MB > 335MB heap).
        int size = Runtime.getRuntime().maxMemory() < 700 * 1024 * 1024 ? 200 * Constants.MB : FIVE_HUNDRED_MB;
        long blockSize = 10 * (long) Constants.MB;
        xmxToSmall(size);

        delayBetweenPerfTests();
        forceGc();
        long usedBefore = getHeapUsed();

        BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
        byte[] data = getRandomByteArray(size);
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(data));
        BlobParallelUploadOptions options = new BlobParallelUploadOptions(body)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        AtomicLong peakHeap = new AtomicLong(usedBefore);
        Thread sampler = startHeapSampler(peakHeap);
        try {
            client.uploadWithResponse(options).block();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopHeapSampler(sampler);
        }

        forceGc();
        long usedAfterGc = getHeapUsed();

        System.out.println("[ContentValidationUploadPerf] Chunked structured message:");
        System.out.println("  Data size: " + size + " bytes (" + (size / (1024 * 1024)) + " MB)");
        System.out.println("  Block size: " + (blockSize / (1024 * 1024)) + " MB");
        System.out.println("  Heap before: " + (usedBefore / (1024 * 1024)) + " MB");
        System.out.println("  Peak heap during upload: " + (peakHeap.get() / (1024 * 1024)) + " MB");
        System.out.println("  Heap after (post-GC): " + (usedAfterGc / (1024 * 1024)) + " MB");
        System.out.println();
        // Pipeline-only encoding: we materialize each block in the policy and use concurrency 1.
        // Peak is documented above; no hard assertion so the test is not flaky (variance from GC/timing).
        long peakMb = peakHeap.get() / (1024 * 1024);
        assertTrue(peakHeap.get() < 2L * 1024 * 1024 * 1024,
            "Chunked structured message peak heap must be < 2 GB (sanity check), was " + peakMb + " MB");
    }

    private Thread startHeapSampler(AtomicLong peakHeap) {
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                peakHeap.updateAndGet(prev -> Math.max(prev, getHeapUsed()));
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "heap-sampler");
        t.setDaemon(true);
        t.start();
        return t;
    }

    private void stopHeapSampler(Thread sampler) {
        if (sampler != null && sampler.isAlive()) {
            sampler.interrupt();
            try {
                sampler.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Pauses so GC can run between memory perf tests. Call at the start of each documentMemoryUsage* test.
     */
    private void delayBetweenPerfTests() {
        try {
            Thread.sleep(DELAY_BETWEEN_PERF_TESTS_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private long getHeapUsed() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    private void forceGc() {
        System.gc();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
