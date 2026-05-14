// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    @AfterEach
    public void cleanup() {
        createdFiles.forEach(File::delete);
    }

    /**
     * downloadStreamWithResponse with CRC64 content validation.
     */
    @Test
    public void downloadStreamWithResponseContentValidation() {
        byte[] data = getRandomByteArray(TEN_MB);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient downloadClient = createBlobAsyncClientWithRequestSniffer(recorded);
        downloadClient.upload(BinaryData.fromBytes(data)).block();

        BlobDownloadStreamOptions options
            = new BlobDownloadStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier
            .create(downloadClient.downloadStreamWithResponse(options)
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(data, result))
            .verifyComplete();
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * downloadContentWithResponse with CRC64 content validation.
     */
    @Test
    public void downloadContentWithResponseContentValidation() {
        byte[] data = getRandomByteArray(TEN_MB);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient downloadClient = createBlobAsyncClientWithRequestSniffer(recorded);
        downloadClient.upload(BinaryData.fromBytes(data)).block();

        BlobDownloadContentOptions options
            = new BlobDownloadContentOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(downloadClient.downloadContentWithResponse(options))
            .assertNext(r -> TestUtils.assertArraysEqual(data, r.getValue().toBytes()))
            .verifyComplete();
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
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
    public void downloadToFileWithResponseContentValidation(int fileSize) throws IOException {
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient downloadClient = createBlobAsyncClientWithRequestSniffer(recorded);
        downloadClient.uploadFromFile(file.toPath().toString(), true).block();

        File outFile = new File(prefix + ".txt");
        createdFiles.add(outFile);
        outFile.deleteOnExit();
        Files.deleteIfExists(outFile.toPath());

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(outFile.toPath().toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(downloadClient.downloadToFileWithResponse(options))
            .assertNext(r -> assertNotNull(r.getValue()))
            .verifyComplete();

        assertTrue(compareFiles(file, outFile, 0, fileSize));
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
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
    public void downloadToFileLargeWithResponseContentValidation(int fileSize) throws IOException {
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient downloadClient = createBlobAsyncClientWithRequestSniffer(recorded);
        downloadClient.uploadFromFile(file.toPath().toString(), true).block();

        File outFile = new File(prefix + ".txt");
        createdFiles.add(outFile);
        outFile.deleteOnExit();
        Files.deleteIfExists(outFile.toPath());

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(outFile.toPath().toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(downloadClient.downloadToFileWithResponse(options))
            .assertNext(r -> assertNotNull(r.getValue()))
            .verifyComplete();

        assertTrue(compareFiles(file, outFile, 0, fileSize));
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * Range download without content validation works correctly.
     */
    @Test
    public void downloadStreamWithResponseContentValidationRange() {
        byte[] randomData = getRandomByteArray(4 * Constants.KB);
        Flux<ByteBuffer> input = Flux.just(ByteBuffer.wrap(randomData));
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient downloadClient = createBlobAsyncClientWithRequestSniffer(recorded);

        BlobRange range = new BlobRange(0, 512L);

        StepVerifier.create(downloadClient.upload(input, null, true)
            .then(downloadClient.downloadStreamWithResponse(range, null, null, false))
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(r -> {
                assertNotNull(r);
                assertEquals(512, r.length);
            }).verifyComplete();
        assertFalse(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * Default behavior: when no algorithm is specified, default is NONE (no validation).
     */
    @Test
    public void downloadStreamDefaultAlgorithmIsNone() {
        byte[] data = getRandomByteArray(TEN_MB);
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient downloadClient = createBlobAsyncClientWithRequestSniffer(recorded);
        downloadClient.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        StepVerifier.create(downloadClient.downloadStreamWithResponse(new BlobDownloadStreamOptions())
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(result -> {
                assertNotNull(result);
                assertEquals(data.length, result.length);
            }).verifyComplete();
        assertFalse(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * AUTO on downloadStream resolves to CRC64 behavior.
     */
    @Test
    public void downloadStreamWithAuto() {
        byte[] data = getRandomByteArray(TEN_MB);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient downloadClient = createBlobAsyncClientWithRequestSniffer(recorded);
        downloadClient.upload(BinaryData.fromBytes(data)).block();

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse(
                    new BlobDownloadStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.AUTO))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(data, result))
            .verifyComplete();
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * downloadContentWithResponse with NONE: no validation triggered.
     */
    @Test
    public void downloadContentWithNone() {
        byte[] data = getRandomByteArray(TEN_MB);
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient downloadClient = createBlobAsyncClientWithRequestSniffer(recorded);
        downloadClient.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        StepVerifier
            .create(downloadClient.downloadContentWithResponse(
                new BlobDownloadContentOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.NONE)))
            .assertNext(r -> TestUtils.assertArraysEqual(data, r.getValue().toBytes()))
            .verifyComplete();
        assertFalse(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * downloadContentWithResponse with AUTO resolves to CRC64 behavior.
     */
    @Test
    public void downloadContentWithAuto() {
        byte[] data = getRandomByteArray(TEN_MB);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient downloadClient = createBlobAsyncClientWithRequestSniffer(recorded);
        downloadClient.upload(BinaryData.fromBytes(data)).block();

        StepVerifier
            .create(downloadClient.downloadContentWithResponse(
                new BlobDownloadContentOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.AUTO)))
            .assertNext(r -> TestUtils.assertArraysEqual(data, r.getValue().toBytes()))
            .verifyComplete();
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * Interrupt with proper rewind to segment boundary; verifies retry range headers.
     */
    @Test
    public void interruptAndVerifyProperRewind() {
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(2 * segmentSize);
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);

        int interruptPos = segmentSize + (2 * (segmentSize / 4)) + 10;
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(1, interruptPos, blobClient.getBlobUrl());
        HttpPipelinePolicy sniffPolicy = (context, next) -> {
            recorded.add(context.getHttpRequest().getHeaders());
            return next.process();
        };

        blobClient.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(), sniffPolicy, mockPolicy);

        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse(new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64))
                .doFinally(
                    signalType -> assertTrue(mockPolicy.getHits() > 0, "Mock interruption policy was not invoked"))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(randomData, result))
            .verifyComplete();

        assertEquals(0, mockPolicy.getTriesRemaining(), "Expected the configured interruption to be consumed");
        assertTrue(mockPolicy.getRangeHeaders().size() >= 2,
            "Expected at least the initial request and one retry with a range header");
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * Proper decode across retries (single and multiple interrupts).
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void interruptAndVerifyProperDecode(boolean multipleInterrupts) {
        final int segmentSize = 128 * Constants.KB;
        final int dataSize = 4 * Constants.KB;
        byte[] randomData = getRandomByteArray(dataSize);
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);

        int interruptPos = segmentSize + (3 * (8 * Constants.KB)) + 10;
        MockPartialResponsePolicy mockPolicy
            = new MockPartialResponsePolicy(multipleInterrupts ? 2 : 1, interruptPos, blobClient.getBlobUrl());
        HttpPipelinePolicy sniffPolicy = (context, next) -> {
            recorded.add(context.getHttpRequest().getHeaders());
            return next.process();
        };

        blobClient.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(), sniffPolicy, mockPolicy);

        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        StepVerifier.create(downloadClient
            .downloadStreamWithResponse(new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64))
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(result -> {
                assertEquals(dataSize, result.length, "Decoded data should have exactly " + dataSize + " bytes");
                TestUtils.assertArraysEqual(randomData, result);
            }).verifyComplete();
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * After consuming the response stream with CRC64 validation, decoded payload preserves the expected CRC64.
     */
    @Test
    public void structuredMessageVerifiesDecodedCrc64DownloadStreaming() {
        byte[] data = getRandomByteArray(TEN_MB);

        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient downloadClient = createBlobAsyncClientWithRequestSniffer(recorded);
        downloadClient.upload(BinaryData.fromBytes(data)).block();

        long expectedCrc = StorageCrc64Calculator.compute(data, 0);

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse(
                    new BlobDownloadStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()).map(bytes -> Tuples.of(r, bytes))))
            .assertNext(tuple -> {
                TestUtils.assertArraysEqual(data, tuple.getT2());
                long actualCrc = StorageCrc64Calculator.compute(tuple.getT2(), 0);
                assertEquals(expectedCrc, actualCrc);
            })
            .verifyComplete();
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * Single interrupt with data intact: fault policy + decoder; structured message retry recovers.
     */
    @Test
    public void interruptWithDataIntact() {
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(4 * segmentSize);
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);

        int interruptPos = segmentSize + (3 * 128) + 10;
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(1, interruptPos, blobClient.getBlobUrl());
        HttpPipelinePolicy sniffPolicy = (context, next) -> {
            recorded.add(context.getHttpRequest().getHeaders());
            return next.process();
        };

        blobClient.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(), sniffPolicy, mockPolicy);

        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse(new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(randomData, result))
            .verifyComplete();
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
    }

    /**
     * Multiple interrupts with data intact: fault policy + decoder; structured message retry recovers.
     */
    @Test
    public void interruptMultipleTimesWithDataIntact() {
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(4 * segmentSize);
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);

        int interruptPos = segmentSize + (3 * 128) + 10;
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(3, interruptPos, blobClient.getBlobUrl());
        HttpPipelinePolicy sniffPolicy = (context, next) -> {
            recorded.add(context.getHttpRequest().getHeaders());
            return next.process();
        };

        blobClient.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            blobClient.getBlobUrl(), sniffPolicy, mockPolicy);

        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse(new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(randomData, result))
            .verifyComplete();
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded));
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
        assertTrue(hasOnlyStructuredMessageDownloadHeaders(recorded), assertionMessage);
    }

}
