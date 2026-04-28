// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.options.BlobDownloadContentOptions;
import com.azure.storage.blob.options.BlobDownloadStreamOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.contentvalidation.StorageCrc64Calculator;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.policy.MockPartialResponsePolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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

}
