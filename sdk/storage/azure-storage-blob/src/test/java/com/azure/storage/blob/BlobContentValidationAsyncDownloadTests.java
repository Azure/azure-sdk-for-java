// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.ProgressListener;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.options.BlobDownloadContentOptions;
import com.azure.storage.blob.options.BlobDownloadStreamOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.contentvalidation.StorageCrc64Calculator;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.policy.MockPartialResponsePolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Async tests for structured message decoding during blob downloads using StorageContentValidationDecoderPolicy.
 * These tests verify that the pipeline policy correctly decodes structured messages when content validation is enabled.
 */
public class BlobContentValidationAsyncDownloadTests extends BlobTestBase {
    private static final int TEN_MB = 10 * Constants.MB;
    private static final int BLOCK_SIZE = 4 * Constants.MB;
    /**
     * {@link BlobTestBase#fuzzyParallelDownloadLargeMultiPartCases()} starts at ~96 MiB; above this threshold fuzzy
     * parallel download helpers use temp files + {@link BlobTestBase#compareFiles(File, File, long, long)} so the full
     * payload never lives twice in heap.
     */
    private static final int FUZZY_PARALLEL_DOWNLOAD_FILE_ROUND_TRIP_THRESHOLD_BYTES = 96 * Constants.MB;

    /**
     * Live-only random payload band for the dedicated random-size parallel-download fuzzy test
     * ({@link #fuzzyParallelDownloadLiveRandomRoundTrip(ContentValidationAlgorithm)}): each run draws a per-run
     * payload size in {@code (256 MiB, 500 MiB]} (matches the encoder fuzzy upload range) so the structured-message
     * decoder is exercised against payloads whose size varies per run in addition to the random byte contents.
     */
    private static final long LIVE_RANDOM_PARALLEL_DOWNLOAD_PAYLOAD_MIN_BYTES_EXCLUSIVE = 256L * Constants.MB;
    private static final long LIVE_RANDOM_PARALLEL_DOWNLOAD_PAYLOAD_MAX_BYTES_INCLUSIVE = 500L * Constants.MB;

    private final List<File> createdFiles = new ArrayList<>();

    private File createRandomFile(Path tempDir, int size) throws IOException {
        File file = Files.createTempFile(tempDir, "blob-cv-source", ".bin").toFile();

        if (size > Constants.MB) {
            try (OutputStream outputStream = Files.newOutputStream(file.toPath())) {
                byte[] data = getRandomByteArray(Constants.MB);
                int mbChunks = size / Constants.MB;
                int remaining = size % Constants.MB;
                for (int i = 0; i < mbChunks; i++) {
                    outputStream.write(data);
                }
                if (remaining > 0) {
                    outputStream.write(data, 0, remaining);
                }
            }
        } else {
            Files.write(file.toPath(), getRandomByteArray(size));
        }

        return file;
    }

    /**
     * downloadStreamWithResponse with CRC64 content validation.
     */
    @Test
    public void downloadStreamWithResponseContentValidation() {
        byte[] data = getRandomByteArray(TEN_MB);
        List<HttpHeaders> recordedRequestHeaders = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recordedRequestHeaders);
        blobClient.upload(BinaryData.fromBytes(data)).block();

        BlobDownloadStreamOptions options
            = new BlobDownloadStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(blobClient.downloadStreamWithResponse(options).flatMap(r -> {
            assertTrue(hasStructuredMessageDownloadResponseHeaders(r.getHeaders()));
            return FluxUtil.collectBytesInByteBufferStream(r.getValue());
        })).assertNext(result -> TestUtils.assertArraysEqual(data, result)).verifyComplete();
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders, false));
    }

    /**
     * downloadContentWithResponse with CRC64 content validation.
     */
    @Test
    public void downloadContentWithResponseContentValidation() {
        byte[] data = getRandomByteArray(TEN_MB);
        List<HttpHeaders> recordedRequestHeaders = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recordedRequestHeaders);
        blobClient.upload(BinaryData.fromBytes(data)).block();

        BlobDownloadContentOptions options
            = new BlobDownloadContentOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(blobClient.downloadContentWithResponse(options)).assertNext(r -> {
            assertTrue(hasStructuredMessageDownloadResponseHeaders(r.getHeaders()));
            TestUtils.assertArraysEqual(data, r.getValue().toBytes());
        }).verifyComplete();
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders, false));
    }

    /**
     * downloadToFileWithResponse with CRC64 content validation.
     */
    @ParameterizedTest
    @ValueSource(
        ints = {
            0, // empty file
            20, // small file
            16 * 1024 * 1024, // medium file in several chunks
            8 * 1026 * 1024 + 10, // medium file not aligned to block
        })
    public void downloadToFileWithResponseContentValidation(int fileSize, @TempDir Path tempDir) throws IOException {
        File file = createRandomFile(tempDir, fileSize);
        File outFile = tempDir.resolve("download.bin").toFile();
        List<HttpHeaders> recordedRequestHeaders = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recordedRequestHeaders);
        blobClient.uploadFromFile(file.toPath().toString(), true).block();

        Files.deleteIfExists(outFile.toPath());

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong((long) BLOCK_SIZE);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(outFile.toPath().toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(blobClient.downloadToFileWithResponse(options)).assertNext(r -> {
            assertTrue(hasStructuredMessageDownloadResponseHeaders(r.getHeaders()));
            assertNotNull(r.getValue());
        }).verifyComplete();

        assertTrue(compareFiles(file, outFile, 0, fileSize));
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders, false));
    }

    /**
     * downloadToFileWithResponse with CRC64 content validation (parallel, multiple block sizes).
     */
    @LiveOnly
    @ParameterizedTest
    @ValueSource(
        ints = {
            50 * Constants.MB, //large file requiring multiple requests
            50 * Constants.MB + 22 // large file not on MB boundary
        })
    public void downloadToFileLargeWithResponseContentValidation(int fileSize, @TempDir Path tempDir)
        throws IOException {
        File file = createRandomFile(tempDir, fileSize);
        File outFile = tempDir.resolve("download.bin").toFile();
        List<HttpHeaders> recordedRequestHeaders = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recordedRequestHeaders);
        blobClient.uploadFromFile(file.toPath().toString(), true).block();

        Files.deleteIfExists(outFile.toPath());

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong((long) BLOCK_SIZE);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(outFile.toPath().toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(blobClient.downloadToFileWithResponse(options)).assertNext(r -> {
            assertTrue(hasStructuredMessageDownloadResponseHeaders(r.getHeaders()));
            assertNotNull(r.getValue());
        }).verifyComplete();

        assertTrue(compareFiles(file, outFile, 0, fileSize));
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders, false));
    }

    /**
     * Default behavior: when no algorithm is specified, default is NONE (no validation).
     */
    @Test
    public void downloadStreamDefaultAlgorithmIsNone() {
        byte[] data = getRandomByteArray(TEN_MB);
        List<HttpHeaders> recordedRequestHeaders = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recordedRequestHeaders);
        blobClient.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        StepVerifier.create(blobClient.downloadStreamWithResponse(new BlobDownloadStreamOptions())
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(result -> {
                assertNotNull(result);
                assertEquals(data.length, result.length);
            }).verifyComplete();
        assertFalse(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders, false));
    }

    @Test
    public void downloadStreamWithResponseContentValidationRange() {
        byte[] data = getRandomByteArray(4 * Constants.KB);
        List<HttpHeaders> recordedRequestHeaders = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recordedRequestHeaders);
        blobClient.upload(BinaryData.fromBytes(data)).block();

        BlobDownloadStreamOptions options = new BlobDownloadStreamOptions().setRange(new BlobRange(0, 512L));

        StepVerifier.create(blobClient.downloadStreamWithResponse(options).flatMap(r -> {
            assertFalse(hasStructuredMessageDownloadResponseHeaders(r.getHeaders()));
            return FluxUtil.collectBytesInByteBufferStream(r.getValue());
        })).assertNext(result -> assertEquals(512, result.length)).verifyComplete();

        assertFalse(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders, false));
    }

    /**
     * AUTO on downloadStream resolves to CRC64 behavior.
     */
    @Test
    public void downloadStreamWithAuto() {
        byte[] data = getRandomByteArray(TEN_MB);
        List<HttpHeaders> recordedRequestHeaders = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recordedRequestHeaders);
        blobClient.upload(BinaryData.fromBytes(data)).block();

        StepVerifier.create(blobClient
            .downloadStreamWithResponse(
                new BlobDownloadStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.AUTO))
            .flatMap(r -> {
                assertTrue(hasStructuredMessageDownloadResponseHeaders(r.getHeaders()));
                return FluxUtil.collectBytesInByteBufferStream(r.getValue());
            })).assertNext(result -> TestUtils.assertArraysEqual(data, result)).verifyComplete();
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders, false));
    }

    /**
     * downloadContentWithResponse with NONE: no validation triggered.
     */
    @Test
    public void downloadContentWithNone() {
        byte[] data = getRandomByteArray(TEN_MB);
        List<HttpHeaders> recordedRequestHeaders = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recordedRequestHeaders);
        blobClient.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        StepVerifier
            .create(blobClient.downloadContentWithResponse(
                new BlobDownloadContentOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.NONE)))
            .assertNext(r -> TestUtils.assertArraysEqual(data, r.getValue().toBytes()))
            .verifyComplete();
        assertFalse(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders, false));
    }

    /**
     * downloadContentWithResponse with AUTO resolves to CRC64 behavior.
     */
    @Test
    public void downloadContentWithAuto() {
        byte[] data = getRandomByteArray(TEN_MB);
        List<HttpHeaders> recordedRequestHeaders = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recordedRequestHeaders);
        blobClient.upload(BinaryData.fromBytes(data)).block();

        StepVerifier
            .create(blobClient.downloadContentWithResponse(
                new BlobDownloadContentOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.AUTO)))
            .assertNext(r -> {
                assertTrue(hasStructuredMessageDownloadResponseHeaders(r.getHeaders()));
                TestUtils.assertArraysEqual(data, r.getValue().toBytes());
            })
            .verifyComplete();
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders, false));
    }

    /**
     * Interrupt with proper rewind to segment boundary; verifies retry range headers.
     */
    @Test
    public void interruptAndVerifyProperRewind() {
        final int segmentSize = Constants.KB;
        byte[] data = getRandomByteArray(2 * segmentSize);
        List<HttpHeaders> recordedRequestHeaders = new CopyOnWriteArrayList<>();
        HttpHeaders recordedResponseHeaders = new HttpHeaders();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recordedRequestHeaders);

        int interruptPos = segmentSize + (2 * (segmentSize / 4)) + 10;
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(1, interruptPos, blobClient.getBlobUrl());
        HttpPipelinePolicy sniffPolicy = getRequestAndResponseHeaderSniffer(blobClient.getBlobUrl(),
            recordedRequestHeaders, recordedResponseHeaders);

        blobClient.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(), sniffPolicy, mockPolicy);

        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        StepVerifier.create(downloadClient
            .downloadStreamWithResponse(new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64))
            .doFinally(signalType -> assertTrue(mockPolicy.getHits() > 0, "Mock interruption policy was not invoked"))
            .flatMap(r -> {
                assertTrue(hasStructuredMessageDownloadResponseHeaders(r.getHeaders()));
                return FluxUtil.collectBytesInByteBufferStream(r.getValue());
            })).assertNext(result -> TestUtils.assertArraysEqual(data, result)).verifyComplete();

        assertEquals(0, mockPolicy.getTriesRemaining(), "Expected the configured interruption to be consumed");
        assertTrue(mockPolicy.getRangeHeaders().size() >= 2,
            "Expected at least the initial request and one retry with a range header");
        assertTrue(hasStructuredMessageDownloadResponseHeaders(recordedResponseHeaders));
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders, false));
    }

    /**
     * Proper decode across retries (single and multiple interrupts).
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void interruptAndVerifyProperDecode(boolean multipleInterrupts) {
        final int segmentSize = 128 * Constants.KB;
        final int dataSize = 4 * Constants.KB;
        byte[] data = getRandomByteArray(dataSize);
        List<HttpHeaders> recordedRequestHeaders = new CopyOnWriteArrayList<>();
        HttpHeaders recordedResponseHeaders = new HttpHeaders();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recordedRequestHeaders);

        int interruptPos = segmentSize + (3 * (8 * Constants.KB)) + 10;
        MockPartialResponsePolicy mockPolicy
            = new MockPartialResponsePolicy(multipleInterrupts ? 2 : 1, interruptPos, blobClient.getBlobUrl());
        HttpPipelinePolicy sniffPolicy = getRequestAndResponseHeaderSniffer(blobClient.getBlobUrl(),
            recordedRequestHeaders, recordedResponseHeaders);

        blobClient.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(), sniffPolicy, mockPolicy);

        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        StepVerifier.create(downloadClient
            .downloadStreamWithResponse(new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64))
            .flatMap(r -> {
                assertTrue(hasStructuredMessageDownloadResponseHeaders(r.getHeaders()));
                return FluxUtil.collectBytesInByteBufferStream(r.getValue());
            })).assertNext(result -> {
                assertEquals(dataSize, result.length, "Decoded data should have exactly " + dataSize + " bytes");
                TestUtils.assertArraysEqual(data, result);
            }).verifyComplete();
        assertTrue(hasStructuredMessageDownloadResponseHeaders(recordedResponseHeaders));
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders, false));
    }

    /**
     * After consuming the response stream with CRC64 validation, decoded payload preserves the expected CRC64.
     */
    @Test
    public void structuredMessageVerifiesDecodedCrc64DownloadStreaming() {
        byte[] data = getRandomByteArray(TEN_MB);
        List<HttpHeaders> recordedRequestHeaders = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recordedRequestHeaders);
        blobClient.upload(BinaryData.fromBytes(data)).block();

        long expectedCrc = StorageCrc64Calculator.compute(data, 0);

        StepVerifier
            .create(blobClient
                .downloadStreamWithResponse(
                    new BlobDownloadStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()).map(bytes -> Tuples.of(r, bytes))))
            .assertNext(tuple -> {
                TestUtils.assertArraysEqual(data, tuple.getT2());
                long actualCrc = StorageCrc64Calculator.compute(tuple.getT2(), 0);
                assertEquals(expectedCrc, actualCrc);
            })
            .verifyComplete();
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders, false));
    }

    /**
     * Single interrupt with data intact: fault policy + decoder; structured message retry recovers.
     */
    @Test
    public void interruptWithDataIntact() {
        final int segmentSize = Constants.KB;
        byte[] data = getRandomByteArray(4 * segmentSize);
        List<HttpHeaders> recordedRequestHeaders = new CopyOnWriteArrayList<>();
        HttpHeaders recordedResponseHeaders = new HttpHeaders();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recordedRequestHeaders);

        int interruptPos = segmentSize + (3 * 128) + 10;
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(1, interruptPos, blobClient.getBlobUrl());
        HttpPipelinePolicy sniffPolicy = getRequestAndResponseHeaderSniffer(blobClient.getBlobUrl(),
            recordedRequestHeaders, recordedResponseHeaders);

        blobClient.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(), sniffPolicy, mockPolicy);

        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse(new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(data, result))
            .verifyComplete();
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders, false));
        assertTrue(hasStructuredMessageDownloadResponseHeaders(recordedResponseHeaders));
    }

    /**
     * Multiple interrupts with data intact: fault policy + decoder; structured message retry recovers.
     */
    @Test
    public void interruptMultipleTimesWithDataIntact() {
        final int segmentSize = Constants.KB;
        byte[] data = getRandomByteArray(4 * segmentSize);
        List<HttpHeaders> recordedRequestHeaders = new CopyOnWriteArrayList<>();
        HttpHeaders recordedResponseHeaders = new HttpHeaders();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recordedRequestHeaders);

        int interruptPos = segmentSize + (3 * 128) + 10;
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(3, interruptPos, blobClient.getBlobUrl());
        HttpPipelinePolicy sniffPolicy = getRequestAndResponseHeaderSniffer(blobClient.getBlobUrl(),
            recordedRequestHeaders, recordedResponseHeaders);

        blobClient.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(), sniffPolicy, mockPolicy);

        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse(new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(data, result))
            .verifyComplete();
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recordedRequestHeaders, false));
        assertTrue(hasStructuredMessageDownloadResponseHeaders(recordedResponseHeaders));
    }

    @Test
    public void verifyProgressListenerIsCompatibleWithContentValidation(@TempDir Path tempDir) throws IOException {
        byte[] data = getRandomByteArray(10 * Constants.MB);

        BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());

        MockProgressListener mockListenerWithContentVal = new MockProgressListener();
        MockProgressListener mockListenerWithoutContentVal = new MockProgressListener();

        ParallelTransferOptions parallelOptionsWithContentVal
            = new ParallelTransferOptions().setProgressListener(mockListenerWithContentVal);
        ParallelTransferOptions parallelOptionsWithoutContentVal
            = new ParallelTransferOptions().setProgressListener(mockListenerWithoutContentVal);

        File fileWithContentVal = createRandomFile(tempDir, 10 * Constants.MB);
        File outFileWithContentVal = tempDir.resolve("withcontentval.bin").toFile();
        File fileWithoutContentVal = createRandomFile(tempDir, 10 * Constants.MB);
        File outFileWithoutContentVal = tempDir.resolve("withoutcontentval.bin").toFile();

        Files.deleteIfExists(outFileWithContentVal.toPath());
        Files.deleteIfExists(outFileWithoutContentVal.toPath());

        BlobDownloadToFileOptions optionsWithContentVal
            = new BlobDownloadToFileOptions(outFileWithContentVal.getAbsolutePath())
                .setParallelTransferOptions(parallelOptionsWithContentVal)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
        BlobDownloadToFileOptions optionsWithoutContentVal
            = new BlobDownloadToFileOptions(outFileWithoutContentVal.getAbsolutePath())
                .setParallelTransferOptions(parallelOptionsWithoutContentVal);

        StepVerifier.create(client.upload(BinaryData.fromBytes(data))
            .then(client.downloadToFileWithResponse(optionsWithContentVal))
            .then(client.downloadToFileWithResponse(optionsWithoutContentVal))).assertNext(ignored -> {
                long expectedBytes = data.length;
                assertEquals(expectedBytes, mockListenerWithContentVal.getReportedByteCount());
                assertEquals(expectedBytes, mockListenerWithoutContentVal.getReportedByteCount());
            }).verifyComplete();
    }

    private static final class MockProgressListener implements ProgressListener {
        private final AtomicLong reportedByteCount = new AtomicLong(0L);

        @Override
        public void handleProgress(long bytesTransferred) {
            this.reportedByteCount.updateAndGet(current -> Math.max(current, bytesTransferred));
        }

        long getReportedByteCount() {
            return this.reportedByteCount.get();
        }
    }

    // ---------- Fuzzy parallel download (deterministic grids) ----------

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelDownloadReplayableCases")
    public void fuzzyParallelDownloadReplayableRoundTrip(int payloadBytes, long blockSizeBytes, int maxConcurrency)
        throws IOException {
        assertParallelDownloadFuzzyRoundTripAsync("replayable", payloadBytes, blockSizeBytes, maxConcurrency);
    }

    @LiveOnly // payload > blockSize with tiny totals; many small range GETs not replayable under the proxy.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelDownloadSmallMultiPartCases")
    public void fuzzyParallelDownloadSmallMultiPartRoundTrip(int payloadBytes, long blockSizeBytes, int maxConcurrency)
        throws IOException {
        assertParallelDownloadFuzzyRoundTripAsync("smallMultiPart", payloadBytes, blockSizeBytes, maxConcurrency);
    }

    @LiveOnly // sub-4 MiB chunked range GETs not replayable under the proxy.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelDownloadSubFourMiBCases")
    public void fuzzyParallelDownloadSubFourMiBRoundTrip(int payloadBytes, long blockSizeBytes, int maxConcurrency)
        throws IOException {
        assertParallelDownloadFuzzyRoundTripAsync("subFourMiB", payloadBytes, blockSizeBytes, maxConcurrency);
    }

    @LiveOnly // 4 MiB boundary tuples that fan out into chunked range GETs.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelDownloadFourMiBBoundaryCases")
    public void fuzzyParallelDownloadFourMiBBoundaryRoundTrip(int payloadBytes, long blockSizeBytes, int maxConcurrency)
        throws IOException {
        assertParallelDownloadFuzzyRoundTripAsync("fourMiBBoundary", payloadBytes, blockSizeBytes, maxConcurrency);
    }

    @LiveOnly // payload > blockSize for every tuple; chunked range GETs across many requests.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelDownloadMediumMultiPartCases")
    public void fuzzyParallelDownloadMediumMultiPartRoundTrip(int payloadBytes, long blockSizeBytes, int maxConcurrency)
        throws IOException {
        assertParallelDownloadFuzzyRoundTripAsync("mediumMultiPart", payloadBytes, blockSizeBytes, maxConcurrency);
    }

    @LiveOnly // payload >> blockSize; ~96-320 MiB downloads.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelDownloadLargeMultiPartCases")
    public void fuzzyParallelDownloadLargeMultiPartRoundTrip(int payloadBytes, long blockSizeBytes, int maxConcurrency)
        throws IOException {
        assertParallelDownloadFuzzyRoundTripAsync("largeMultiPart", payloadBytes, blockSizeBytes, maxConcurrency);
    }

    @LiveOnly // ~1 GiB single case; far too large for the test proxy.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelDownloadOneGiBCases")
    public void fuzzyParallelDownloadOneGiBRoundTrip(int payloadBytes, long blockSizeBytes, int maxConcurrency)
        throws IOException {
        assertParallelDownloadFuzzyRoundTripAsync("oneGiB", payloadBytes, blockSizeBytes, maxConcurrency);
    }

    /**
     * Live-only random-size parallel download fuzzy round-trip. Each run draws a per-run payload size in
     * {@code (256 MiB, 500 MiB]} (matches the encoder fuzzy upload range) and exercises both CRC64 and AUTO
     * content-validation algorithms so the structured-message decoder is tested against payloads whose total size
     * varies per run in addition to the random byte contents that the deterministic grids already exercise. Kept
     * separate from the parameterized {@link #fuzzyParallelDownloadLargeMultiPartRoundTrip(int, long, int)} so the
     * deterministic per-grid round-trips and the randomized round-trip don't share work or cost.
     */
    @LiveOnly
    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void fuzzyParallelDownloadLiveRandomRoundTrip(ContentValidationAlgorithm algorithm) throws IOException {
        int sizeBytes = (int) randomLongFromNamer(LIVE_RANDOM_PARALLEL_DOWNLOAD_PAYLOAD_MIN_BYTES_EXCLUSIVE + 1,
            LIVE_RANDOM_PARALLEL_DOWNLOAD_PAYLOAD_MAX_BYTES_INCLUSIVE + 1);
        assertParallelDownloadFuzzyRoundTripAsync("liveRandom", sizeBytes, 8L * Constants.MB, 8, algorithm);
    }

    private void assertParallelDownloadFuzzyRoundTripAsync(String caseKind, int payloadBytes, long blockSizeBytes,
        int maxConcurrency) throws IOException {
        assertParallelDownloadFuzzyRoundTripAsync(caseKind, payloadBytes, blockSizeBytes, maxConcurrency,
            ContentValidationAlgorithm.CRC64);
    }

    private void assertParallelDownloadFuzzyRoundTripAsync(String caseKind, int payloadBytes, long blockSizeBytes,
        int maxConcurrency, ContentValidationAlgorithm algorithm) throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        ParallelTransferOptions parallelOptions
            = new ParallelTransferOptions().setBlockSizeLong(blockSizeBytes).setMaxConcurrency(maxConcurrency);

        String assertionMessage = "Fuzzy parallel download [" + caseKind + "] payloadBytes=" + payloadBytes
            + ", blockSize=" + blockSizeBytes + ", maxConcurrency=" + maxConcurrency + ", algorithm=" + algorithm;

        if (payloadBytes >= FUZZY_PARALLEL_DOWNLOAD_FILE_ROUND_TRIP_THRESHOLD_BYTES) {
            File sourceFile = getRandomFile(payloadBytes);
            sourceFile.deleteOnExit();
            createdFiles.add(sourceFile);
            File outFile = Files.createTempFile("blob-cv-fuzzy-parallel-dl-async", ".bin").toFile();
            outFile.deleteOnExit();
            createdFiles.add(outFile);
            Files.deleteIfExists(outFile.toPath());

            BlobUploadFromFileOptions uploadOptions
                = new BlobUploadFromFileOptions(sourceFile.getAbsolutePath()).setParallelTransferOptions(
                    new com.azure.storage.blob.models.ParallelTransferOptions().setBlockSizeLong(blockSizeBytes)
                        .setMaxConcurrency(maxConcurrency));
            assertNotNull(client.uploadFromFileWithResponse(uploadOptions).block().getValue().getETag(),
                assertionMessage);

            BlobDownloadToFileOptions downloadOptions
                = new BlobDownloadToFileOptions(outFile.toPath().toString()).setParallelTransferOptions(parallelOptions)
                    .setContentValidationAlgorithm(algorithm);

            StepVerifier.create(client.downloadToFileWithResponse(downloadOptions))
                .assertNext(r -> assertNotNull(r.getValue(), assertionMessage))
                .verifyComplete();

            assertTrue(compareFiles(sourceFile, outFile, 0, payloadBytes), assertionMessage);
        } else {
            byte[] randomData = getRandomByteArray(payloadBytes);
            client.upload(BinaryData.fromBytes(randomData), true).block();

            if (payloadBytes > blockSizeBytes) {
                File outFile = Files.createTempFile("blob-cv-fuzzy-parallel-dl-async-mp", ".bin").toFile();
                outFile.deleteOnExit();
                createdFiles.add(outFile);
                Files.deleteIfExists(outFile.toPath());

                BlobDownloadToFileOptions downloadOptions = new BlobDownloadToFileOptions(outFile.toPath().toString())
                    .setParallelTransferOptions(parallelOptions)
                    .setContentValidationAlgorithm(algorithm);

                StepVerifier.create(client.downloadToFileWithResponse(downloadOptions))
                    .assertNext(r -> assertNotNull(r.getValue(), assertionMessage))
                    .verifyComplete();

                byte[] downloaded = Files.readAllBytes(outFile.toPath());
                assertArrayEquals(randomData, downloaded, assertionMessage);
            } else {
                BlobDownloadContentOptions downloadOptions
                    = new BlobDownloadContentOptions().setContentValidationAlgorithm(algorithm);

                StepVerifier.create(client.downloadContentWithResponse(downloadOptions))
                    .assertNext(r -> assertArrayEquals(randomData, r.getValue().toBytes(), assertionMessage))
                    .verifyComplete();

                BlobDownloadStreamOptions streamOptions
                    = new BlobDownloadStreamOptions().setContentValidationAlgorithm(algorithm);
                StepVerifier
                    .create(client.downloadStreamWithResponse(streamOptions)
                        .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
                    .assertNext(bytes -> assertArrayEquals(randomData, bytes, assertionMessage))
                    .verifyComplete();
            }
        }
        assertTrue(hasStructuredMessageDownloadRequestHeaders(recorded, false), assertionMessage);
    }

}
