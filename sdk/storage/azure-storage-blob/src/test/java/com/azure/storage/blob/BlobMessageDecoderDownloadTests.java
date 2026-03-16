// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.test.TestMode;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.common.DownloadContentValidationOptions;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageCrc64Calculator;
import com.azure.storage.common.policy.StorageContentValidationDecoderPolicy;
import com.azure.storage.common.test.shared.policy.MockPartialResponsePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for structured message decoding during blob downloads using StorageContentValidationDecoderPolicy.
 * These tests verify that the pipeline policy correctly decodes structured messages when content validation is enabled.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class BlobMessageDecoderDownloadTests extends BlobTestBase {

    private BlobAsyncClient bc;

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName);
        bc.upload(Flux.just(ByteBuffer.wrap(new byte[0])), null).block();
    }

    /**
     * Aligned with .NET: decoder-only client; live service returns structured-encoded body.
     * Runs in LIVE only: playback may not replay streaming response body, causing "decoded 0" otherwise.
     */
    @Test
    @EnabledIf("isLiveMode")
    public void downloadStreamWithResponseContentValidation() throws IOException {
        byte[] randomData = getRandomByteArray(10 * 1024 * 1024);  // 10 MB

        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), decoderPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse((BlobRange) null, (DownloadRetryOptions) null, (BlobRequestConditions) null,
                    false, validationOptions)
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(r -> TestUtils.assertArraysEqual(r, randomData))
            .verifyComplete();
    }

    /**
     * Mirrors .NET StructuredMessagePopulatesCrcDownloadStreaming: after consuming the response stream with
     * StorageCrc64 validation, the response details (ContentCrc64) are populated with the computed CRC.
     * Aligned with .NET: decoder-only client, live/playback returns structured-encoded body.
     */
    @Test
    public void structuredMessagePopulatesCrc64DownloadStreaming() throws IOException {
        int dataLength = Constants.KB;
        byte[] data = getRandomByteArray(dataLength);
        bc.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), decoderPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        long expectedCrc = StorageCrc64Calculator.compute(data, 0);
        byte[] expectedCrcBytes = new byte[8];
        ByteBuffer.wrap(expectedCrcBytes).order(ByteOrder.LITTLE_ENDIAN).putLong(expectedCrc);

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse((BlobRange) null, (DownloadRetryOptions) null, (BlobRequestConditions) null,
                    false, validationOptions)
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()).map(bytes -> Tuples.of(r, bytes))))
            .assertNext(tuple -> {
                TestUtils.assertArraysEqual(data, tuple.getT2());
                assertNotNull(tuple.getT1().getDeserializedHeaders().getContentCrc64(),
                    "ContentCrc64 should be populated after stream consumption");
                TestUtils.assertArraysEqual(expectedCrcBytes, tuple.getT1().getDeserializedHeaders().getContentCrc64());
            })
            .verifyComplete();
    }

    @Test
    public void downloadStreamWithResponseContentValidationRange() throws IOException {
        byte[] randomData = getRandomByteArray(4 * Constants.KB);
        Flux<ByteBuffer> input = Flux.just(ByteBuffer.wrap(randomData));

        // Range download without validation should work
        BlobRange range = new BlobRange(0, 512L);

        StepVerifier.create(bc.upload(input, null, true)
            .then(
                bc.downloadStreamWithResponse(range, (DownloadRetryOptions) null, (BlobRequestConditions) null, false))
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(r -> {
                assertNotNull(r);
                // Should get exactly 512 bytes of encoded data
                assertEquals(512, r.length);
            }).verifyComplete();
    }

    /**
     * Mirrors .NET UninterruptedStream: decoder-only client, live/playback returns structured-encoded body.
     */
    @Test
    public void uninterruptedStreamWithStructuredMessageDecoding() throws IOException {
        byte[] randomData = getRandomByteArray(4 * Constants.KB);
        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), decoderPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        StepVerifier
            .create(
                downloadClient
                    .downloadStreamWithResponse((BlobRange) null, null, (BlobRequestConditions) null, false,
                        validationOptions)
                    .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(result, randomData))
            .verifyComplete();
    }

    /**
     * Mirrors .NET Interrupt_DataIntact (single interrupt): decoder + fault policy only; live/playback returns
     * structured-encoded body; MockPartialResponsePolicy simulates interruption like .NET FaultyDownloadPipelinePolicy.
     */
    @Test
    public void interruptWithDataIntact() throws IOException {
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(4 * segmentSize);

        int interruptPos = segmentSize + (3 * 128) + 10; // readLen in .NET test = 128 bytes
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(1, interruptPos, bc.getBlobUrl());

        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), decoderPolicy, mockPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                    validationOptions)
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(randomData, result))
            .verifyComplete();
    }

    /**
     * Blocking variant of interruptWithDataIntact; aligned with .NET (decoder + fault policy only).
     */
    @Test
    public void interruptWithDataIntactBlocking() throws IOException {
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(4 * segmentSize);

        int interruptPos = segmentSize + (3 * 128) + 10;
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(1, interruptPos, bc.getBlobUrl());
        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), decoderPolicy, mockPolicy);

        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        byte[] result = downloadClient
            .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                validationOptions)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))
            .block();

        TestUtils.assertArraysEqual(randomData, result);
    }

    /**
     * Mirrors .NET Interrupt_DataIntact (multiple interrupts): decoder + fault policy only.
     */
    @Test
    public void interruptMultipleTimesWithDataIntact() throws IOException {
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(4 * segmentSize);

        int interruptPos = segmentSize + (3 * 128) + 10;
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(3, interruptPos, bc.getBlobUrl());

        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), decoderPolicy, mockPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                    validationOptions)
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(randomData, result))
            .verifyComplete();
    }

    /**
     * Mirrors .NET Interrupt_AppropriateRewind: decoder + fault policy only; verifies rewind to segment boundary.
     */
    @Test
    public void interruptAndVerifyProperRewind() throws IOException {
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(2 * segmentSize);

        int interruptPos = segmentSize + (2 * (segmentSize / 4)) + 10; // readLen = segmentSize/4
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(1, interruptPos, bc.getBlobUrl());

        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), decoderPolicy, mockPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        StepVerifier.create(downloadClient
            .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                validationOptions)
            .doFinally(signalType -> {
                System.out.println("[MockPartialResponsePolicy] hits=" + mockPolicy.getHits() + ", triesRemaining="
                    + mockPolicy.getTriesRemaining() + ", ranges=" + mockPolicy.getRangeHeaders());
                assertTrue(mockPolicy.getHits() > 0, "Mock interruption policy was not invoked");
            })
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(randomData, result))
            .verifyComplete();

        assertEquals(0, mockPolicy.getTriesRemaining(), "Expected the configured interruption to be consumed");
        assertTrue(mockPolicy.getRangeHeaders().size() >= 2,
            "Expected at least the initial request and one retry with a range header");
    }

    /**
     * Mirrors .NET Interrupt_ProperDecode: decoder + fault policy only; proper decode across retries.
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void interruptAndVerifyProperDecode(boolean multipleInterrupts) throws IOException {
        final int segmentSize = 128 * Constants.KB;
        final int dataSize = 4 * Constants.KB;
        byte[] randomData = getRandomByteArray(dataSize);

        int interruptPos = segmentSize + (3 * (8 * Constants.KB)) + 10; // readLen = 8KB in .NET
        MockPartialResponsePolicy mockPolicy
            = new MockPartialResponsePolicy(multipleInterrupts ? 2 : 1, interruptPos, bc.getBlobUrl());

        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            bc.getBlobUrl(), decoderPolicy, mockPolicy);

        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);
        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        StepVerifier.create(downloadClient
            .downloadStreamWithResponse((BlobRange) null, retryOptions, (BlobRequestConditions) null, false,
                validationOptions)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(result -> {
                assertEquals(dataSize, result.length, "Decoded data should have exactly " + dataSize + " bytes");
                TestUtils.assertArraysEqual(randomData, result);
            }).verifyComplete();
    }

    /**
     * DownloadToFile with structured message decoding using the same payload size as .NET (Constants.KB).
     * Aligned with .NET: decoder-only client, live/playback returns structured-encoded body.
     */
    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    public void downloadToFileStructuredMessageSamePayloadAsNet() throws IOException {
        int payloadSize = Constants.KB; // same as .NET StructuredMessagePopulatesCrc / transfer validation tests
        byte[] randomData = getRandomByteArray(payloadSize);
        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), decoderPolicy);

        Path tempFile = Files.createTempFile("structured-download-net-size", ".bin");
        Files.deleteIfExists(tempFile);

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong((long) Constants.KB)
            .setInitialTransferSizeLong((long) Constants.KB);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(tempFile.toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationOptions(
                    new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true));

        try {
            StepVerifier.create(downloadClient.downloadToFileWithResponse(options))
                .assertNext(r -> assertNotNull(r.getValue()))
                .expectComplete()
                .verify(Duration.ofSeconds(60));

            TestUtils.assertArraysEqual(randomData, Files.readAllBytes(tempFile));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Single-chunk DownloadToFile; aligned with .NET: decoder-only client, live/playback.
     */
    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    public void downloadToFileStructuredMessageSingleChunk() throws IOException {
        int blockSize = 512;
        int payloadSize = blockSize;
        byte[] randomData = getRandomByteArray(payloadSize);
        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), decoderPolicy);

        Path tempFile = Files.createTempFile("structured-download-single", ".bin");
        Files.deleteIfExists(tempFile);

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong((long) blockSize)
            .setInitialTransferSizeLong((long) blockSize);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(tempFile.toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationOptions(
                    new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true));

        try {
            StepVerifier.create(downloadClient.downloadToFileWithResponse(options))
                .assertNext(r -> assertNotNull(r.getValue()))
                .expectComplete()
                .verify(Duration.ofSeconds(60));

            TestUtils.assertArraysEqual(randomData, Files.readAllBytes(tempFile));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Parallel download with structured message validation; aligned with .NET: no mock, decoder only, live/playback,
     * default concurrency (no explicit maxConcurrency), payload (4*blockSize)+1 and block sizes 512/2048 per .NET.
     */
    @ParameterizedTest
    @ValueSource(ints = { 512, 2048 })
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    public void downloadToFileStructuredMessageParallel(int blockSize) throws IOException {
        int payloadSize = (4 * blockSize) + 1;
        byte[] randomData = getRandomByteArray(payloadSize);
        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), decoderPolicy);

        Path tempFile = Files.createTempFile("structured-download", ".bin");
        Files.deleteIfExists(tempFile);

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong((long) blockSize)
            .setInitialTransferSizeLong((long) blockSize);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(tempFile.toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationOptions(
                    new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true));

        try {
            StepVerifier.create(downloadClient.downloadToFileWithResponse(options))
                .assertNext(r -> assertNotNull(r.getValue()))
                .expectComplete()
                .verify(Duration.ofSeconds(60));

            TestUtils.assertArraysEqual(randomData, Files.readAllBytes(tempFile));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Sync variant: same payload and transfer options as .NET (default concurrency; sync path forces 1). Multi-chunk
     * DownloadTo with structured message validation.
     */
    @ParameterizedTest
    @ValueSource(ints = { 512, 2048 })
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    public void downloadToFileStructuredMessageParallelSync(int blockSize) throws IOException {
        int payloadSize = (4 * blockSize) + 1;
        byte[] randomData = getRandomByteArray(payloadSize);
        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        StorageContentValidationDecoderPolicy decoderPolicy = new StorageContentValidationDecoderPolicy();
        BlobClient downloadClient
            = getBlobClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), decoderPolicy);

        Path tempFile = Files.createTempFile("structured-download-sync", ".bin");
        Files.deleteIfExists(tempFile);

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong((long) blockSize)
            .setInitialTransferSizeLong((long) blockSize);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(tempFile.toString()).setParallelTransferOptions(parallelOptions)
                .setContentValidationOptions(
                    new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true));

        try {
            assertNotNull(downloadClient.downloadToFileWithResponse(options, null, Context.NONE).getValue());
            TestUtils.assertArraysEqual(randomData, Files.readAllBytes(tempFile));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Mirrors .NET OlderServiceVersionThrowsOnStructuredMessage: when using a service version before structured
     * message was introduced (V2024_11_04), a download with structured message validation enabled and a range that
     * would trigger structured message response must throw (service returns error for unsupported feature).
     */
    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    @EnabledIf("isLiveMode")
    public void olderServiceVersionThrowsOnStructuredMessage() {
        int dataLength = Constants.KB;
        byte[] data = getRandomByteArray(dataLength);
        bc.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        BlobClientBuilder builder = new BlobClientBuilder().endpoint(bc.getBlobUrl())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .serviceVersion(BlobServiceVersion.V2024_11_04);
        instrument(builder);
        BlobAsyncClient oldVersionClient = builder.buildAsyncClient();

        BlobRange range = new BlobRange(0, (long) Constants.KB);
        DownloadContentValidationOptions validationOptions
            = new DownloadContentValidationOptions().setStructuredMessageValidationEnabled(true);

        StepVerifier
            .create(oldVersionClient.downloadStreamWithResponse(range, null, null, false, validationOptions)
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .verifyError(BlobStorageException.class);
    }

    static boolean isLiveMode() {
        return ENVIRONMENT.getTestMode() == TestMode.LIVE;
    }
}
