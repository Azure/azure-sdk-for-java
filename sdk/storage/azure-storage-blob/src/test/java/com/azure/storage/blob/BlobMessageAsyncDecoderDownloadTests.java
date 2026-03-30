// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.test.TestMode;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.options.BlobDownloadContentOptions;
import com.azure.storage.blob.options.BlobDownloadStreamOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.StorageChecksumAlgorithm;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.contentvalidation.StorageCrc64Calculator;
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
 * Async tests for structured message decoding during blob downloads using StorageContentValidationDecoderPolicy.
 * These tests verify that the pipeline policy correctly decodes structured messages when content validation is enabled.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class BlobMessageAsyncDecoderDownloadTests extends BlobTestBase {

    private BlobAsyncClient bc;

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName);
        bc.upload(Flux.just(ByteBuffer.wrap(new byte[0])), null).block();
    }

    /**
     * downloadStreamWithResponse with CRC64 content validation.
     */
    @Test
    public void downloadStreamWithResponseContentValidation() throws IOException {
        byte[] randomData = getRandomByteArray(10 * 1024 * 1024);

        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl());

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse(
                    new BlobDownloadStreamOptions().setResponseChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(r -> TestUtils.assertArraysEqual(r, randomData))
            .verifyComplete();
    }

    /**
     * downloadContentWithResponse with CRC64 content validation.
     */
    @Test
    public void downloadContentWithResponseContentValidation() {
        byte[] data = getRandomByteArray(10 * 1024 * 1024);
        bc.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl());

        StepVerifier
            .create(downloadClient.downloadContentWithResponse(
                new BlobDownloadContentOptions().setResponseChecksumAlgorithm(StorageChecksumAlgorithm.CRC64)))
            .assertNext(r -> TestUtils.assertArraysEqual(data, r.getValue().toBytes()))
            .verifyComplete();
    }

    /**
     * downloadToFileWithResponse with CRC64 content validation (parallel, multiple block sizes).
     */
    @ParameterizedTest
    @ValueSource(ints = { 512, 2048 })
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    public void downloadToFileWithResponseContentValidation(int blockSize) throws IOException {
        int payloadSize = (4 * blockSize) + 1;
        byte[] randomData = getRandomByteArray(payloadSize);
        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl());

        Path tempFile = Files.createTempFile("structured-download", ".bin");
        Files.deleteIfExists(tempFile);

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong((long) blockSize)
            .setInitialTransferSizeLong((long) blockSize);
        BlobDownloadToFileOptions options
            = new BlobDownloadToFileOptions(tempFile.toString()).setParallelTransferOptions(parallelOptions)
                .setResponseChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

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
     * After consuming the response stream with CRC64 validation, ContentCrc64 header is populated.
     */
    @Test
    public void structuredMessagePopulatesCrc64DownloadStreaming() throws IOException {
        int dataLength = 10 * 1024 * 1024;
        byte[] data = getRandomByteArray(dataLength);
        bc.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl());

        long expectedCrc = StorageCrc64Calculator.compute(data, 0);
        byte[] expectedCrcBytes = new byte[8];
        ByteBuffer.wrap(expectedCrcBytes).order(ByteOrder.LITTLE_ENDIAN).putLong(expectedCrc);

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse(
                    new BlobDownloadStreamOptions().setResponseChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()).map(bytes -> Tuples.of(r, bytes))))
            .assertNext(tuple -> {
                TestUtils.assertArraysEqual(data, tuple.getT2());
                assertNotNull(tuple.getT1().getDeserializedHeaders().getContentCrc64(),
                    "ContentCrc64 should be populated after stream consumption");
                TestUtils.assertArraysEqual(expectedCrcBytes, tuple.getT1().getDeserializedHeaders().getContentCrc64());
            })
            .verifyComplete();
    }

    /**
     * Range download without content validation works correctly.
     */
    @Test
    public void downloadStreamWithResponseContentValidationRange() throws IOException {
        byte[] randomData = getRandomByteArray(4 * Constants.KB);
        Flux<ByteBuffer> input = Flux.just(ByteBuffer.wrap(randomData));

        BlobRange range = new BlobRange(0, 512L);

        StepVerifier.create(bc.upload(input, null, true)
            .then(
                bc.downloadStreamWithResponse(range, (DownloadRetryOptions) null, (BlobRequestConditions) null, false))
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(r -> {
                assertNotNull(r);
                assertEquals(512, r.length);
            }).verifyComplete();
    }

    /**
     * Single interrupt with data intact: fault policy + decoder; structured message retry recovers.
     */
    @Test
    public void interruptWithDataIntact() throws IOException {
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(4 * segmentSize);

        int interruptPos = segmentSize + (3 * 128) + 10;
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(1, interruptPos, bc.getBlobUrl());

        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), mockPolicy);

        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse(new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
                    .setResponseChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(randomData, result))
            .verifyComplete();
    }

    /**
     * Multiple interrupts with data intact: fault policy + decoder; structured message retry recovers.
     */
    @Test
    public void interruptMultipleTimesWithDataIntact() throws IOException {
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(4 * segmentSize);

        int interruptPos = segmentSize + (3 * 128) + 10;
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(3, interruptPos, bc.getBlobUrl());

        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), mockPolicy);

        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse(new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
                    .setResponseChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(randomData, result))
            .verifyComplete();
    }

    /**
     * Interrupt with proper rewind to segment boundary; verifies retry range headers.
     */
    @Test
    public void interruptAndVerifyProperRewind() throws IOException {
        final int segmentSize = Constants.KB;
        byte[] randomData = getRandomByteArray(2 * segmentSize);

        int interruptPos = segmentSize + (2 * (segmentSize / 4)) + 10;
        MockPartialResponsePolicy mockPolicy = new MockPartialResponsePolicy(1, interruptPos, bc.getBlobUrl());

        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), mockPolicy);

        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);

        StepVerifier.create(downloadClient
            .downloadStreamWithResponse(new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
                .setResponseChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))
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
     * Proper decode across retries (single and multiple interrupts).
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void interruptAndVerifyProperDecode(boolean multipleInterrupts) throws IOException {
        final int segmentSize = 128 * Constants.KB;
        final int dataSize = 4 * Constants.KB;
        byte[] randomData = getRandomByteArray(dataSize);

        int interruptPos = segmentSize + (3 * (8 * Constants.KB)) + 10;
        MockPartialResponsePolicy mockPolicy
            = new MockPartialResponsePolicy(multipleInterrupts ? 2 : 1, interruptPos, bc.getBlobUrl());

        bc.upload(Flux.just(ByteBuffer.wrap(randomData)), null, true).block();

        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl(), mockPolicy);

        DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(10);

        StepVerifier.create(downloadClient
            .downloadStreamWithResponse(new BlobDownloadStreamOptions().setDownloadRetryOptions(retryOptions)
                .setResponseChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(result -> {
                assertEquals(dataSize, result.length, "Decoded data should have exactly " + dataSize + " bytes");
                TestUtils.assertArraysEqual(randomData, result);
            }).verifyComplete();
    }

    /**
     * Older service version throws when structured message validation is requested.
     */
    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    @EnabledIf("isLiveMode")
    public void olderServiceVersionThrowsOnStructuredMessage() {
        int dataLength = 10 * 1024 * 1024;
        byte[] data = getRandomByteArray(dataLength);
        bc.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        BlobClientBuilder builder = new BlobClientBuilder().endpoint(bc.getBlobUrl())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .serviceVersion(BlobServiceVersion.V2024_11_04);
        instrument(builder);
        BlobAsyncClient oldVersionClient = builder.buildAsyncClient();

        StepVerifier
            .create(oldVersionClient
                .downloadStreamWithResponse(
                    new BlobDownloadStreamOptions().setRange(new BlobRange(0, (long) (10 * 1024 * 1024)))
                        .setResponseChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .verifyError(BlobStorageException.class);
    }

    /**
     * Default behavior: when no algorithm is specified, default is NONE (no validation).
     */
    @Test
    public void downloadStreamDefaultAlgorithmIsNone() {
        byte[] data = getRandomByteArray(10 * 1024 * 1024);
        bc.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        StepVerifier.create(bc.downloadStreamWithResponse(new BlobDownloadStreamOptions())
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue()))).assertNext(result -> {
                assertNotNull(result);
                assertEquals(data.length, result.length);
            }).verifyComplete();
    }

    /**
     * MD5 on downloadStream: MD5 transactional validation path.
     */
    @Test
    public void downloadStreamWithMd5() {
        byte[] data = getRandomByteArray(10 * 1024 * 1024);
        bc.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        StepVerifier
            .create(bc
                .downloadStreamWithResponse(
                    new BlobDownloadStreamOptions().setRange(new BlobRange(0, (long) data.length))
                        .setResponseChecksumAlgorithm(StorageChecksumAlgorithm.MD5))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(data, result))
            .verifyComplete();
    }

    /**
     * AUTO on downloadStream resolves to CRC64 behavior.
     */
    @Test
    public void downloadStreamWithAuto() {
        byte[] data = getRandomByteArray(10 * 1024 * 1024);
        bc.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl());

        StepVerifier
            .create(downloadClient
                .downloadStreamWithResponse(
                    new BlobDownloadStreamOptions().setResponseChecksumAlgorithm(StorageChecksumAlgorithm.AUTO))
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(r.getValue())))
            .assertNext(result -> TestUtils.assertArraysEqual(data, result))
            .verifyComplete();
    }

    /**
     * downloadContentWithResponse with NONE: no validation triggered.
     */
    @Test
    public void downloadContentWithNone() {
        byte[] data = getRandomByteArray(10 * 1024 * 1024);
        bc.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        StepVerifier
            .create(bc.downloadContentWithResponse(
                new BlobDownloadContentOptions().setResponseChecksumAlgorithm(StorageChecksumAlgorithm.NONE)))
            .assertNext(r -> TestUtils.assertArraysEqual(data, r.getValue().toBytes()))
            .verifyComplete();
    }

    /**
     * downloadContentWithResponse with AUTO resolves to CRC64 behavior.
     */
    @Test
    public void downloadContentWithAuto() {
        byte[] data = getRandomByteArray(10 * 1024 * 1024);
        bc.upload(Flux.just(ByteBuffer.wrap(data)), null, true).block();

        BlobAsyncClient downloadClient
            = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), bc.getBlobUrl());

        StepVerifier
            .create(downloadClient.downloadContentWithResponse(
                new BlobDownloadContentOptions().setResponseChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)))
            .assertNext(r -> TestUtils.assertArraysEqual(data, r.getValue().toBytes()))
            .verifyComplete();
    }

    static boolean isLiveMode() {
        return ENVIRONMENT.getTestMode() == TestMode.LIVE;
    }
}
