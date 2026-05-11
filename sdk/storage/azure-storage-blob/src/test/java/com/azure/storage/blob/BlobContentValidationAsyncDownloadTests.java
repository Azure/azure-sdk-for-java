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
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.contentvalidation.StorageCrc64Calculator;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.policy.MockPartialResponsePolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
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
    private static final int BLOCK_SIZE = 4 * Constants.MB;

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

}
