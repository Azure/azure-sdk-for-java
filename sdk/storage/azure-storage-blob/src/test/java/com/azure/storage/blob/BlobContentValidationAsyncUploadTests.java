// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.AppendBlobAppendBlockOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockOptions;
import com.azure.storage.blob.options.PageBlobUploadPagesOptions;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.implementation.contentvalidation.ContentValidationModeResolver;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests content validation (CRC64 / structured message) for upload operations using async clients.
 * Upload types that have no async counterpart (e.g. OutputStream, SeekableByteChannel) are tested in
 * {@link BlobContentValidationUploadTests}.
 */
public class BlobContentValidationAsyncUploadTests extends BlobTestBase {
    private static final int TEN_MB = 10 * Constants.MB;
    /* Single-part uploads with length < 4MB use CRC64 header; >= 4MB use structured message. */
    private static final int UNDER_4MB = 2 * Constants.MB;

    /**
     * Live-only random payload band (256–500 MiB, inclusive upper bound via {@code randomLongFromNamer}+1) for
     * {@code uploadWithResponse}, {@code uploadFromFileWithResponse}, and single-block {@code stageBlock}.
     */
    private static final long LIVE_RANDOM_PARALLEL_PAYLOAD_MIN_BYTES_EXCLUSIVE = 256L * Constants.MB;
    private static final long LIVE_RANDOM_PARALLEL_PAYLOAD_MAX_BYTES_INCLUSIVE = 500L * Constants.MB;

    /**
     * Live-only random payload band for sequential append-block puts only ({@link
     * #appendBlockLiveRandomRoundTripDataIntegrity()}): {@code Flux.concatMap} issues one append REST call per chunk in
     * order (not parallel staging); use a smaller band than {@link #LIVE_RANDOM_PARALLEL_PAYLOAD_MIN_BYTES_EXCLUSIVE}.
     */
    private static final long LIVE_RANDOM_SEQUENTIAL_APPEND_PAYLOAD_MIN_BYTES_EXCLUSIVE = 32L * Constants.MB;
    private static final long LIVE_RANDOM_SEQUENTIAL_APPEND_PAYLOAD_MAX_BYTES_INCLUSIVE = 64L * Constants.MB;

    private static final String MD5_AND_CRC64_EXCLUSIVE_MESSAGE
        = "Only one form of transactional content validation may be used.";

    // ===========================================================================================
    // BlobAsyncClient.uploadWithResponse
    // ===========================================================================================

    /**
     * Single-part upload under 4MB: content validation uses CRC64 header only (no structured message).
     */
    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadWithCrc64Header(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    /**
     * Single-part upload >= 4MB: content validation uses structured message.
     */
    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadWithStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
    }

    /**
     * Multipart (chunked) upload; content validation uses structured message on each stage block.
     */
    @LiveOnly // Put Block URLs include random block IDs; not replayable with the test proxy.
    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadChunkedWithStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));
        long blockSize = 2 * (long) Constants.MB;

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
    }

    @Test
    public void uploadWithoutContentValidation() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasNoContentValidationHeaders(recorded));
        }).verifyComplete();
    }

    /**
     * Blob parallel upload rejects using both computeMd5 (SDK-computed MD5) and CRC64 (transfer validation checksum algorithm) at once.
     */
    @Test
    public void uploadWithComputeMd5AndCrc64Throws() {
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setComputeMd5(true)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(client.uploadWithResponse(options))
            .verifyErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
    }

    // ===========================================================================================
    // BlockBlobAsyncClient.uploadWithResponse (BlockBlobSimpleUpload / Put Blob) tests
    // ===========================================================================================

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobSimpleUploadWithCrc64Header(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobSimpleUploadWithStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
    }

    @Test
    public void blockBlobSimpleUploadWithNoContentValidation() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasNoContentValidationHeaders(recorded));
        }).verifyComplete();
    }

    // ===========================================================================================
    // BlockBlobAsyncClient.stageBlockWithResponse (Put Block) tests
    // ===========================================================================================

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void stageBlockWithCrc64Header(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options
            = new BlockBlobStageBlockOptions(getBlockID(), data).setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.stageBlockWithResponse(options))
            .assertNext(response -> assertTrue(hasOnlyCrc64Headers(recorded)))
            .verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void stageBlockWithStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options
            = new BlockBlobStageBlockOptions(getBlockID(), data).setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.stageBlockWithResponse(options))
            .assertNext(response -> assertTrue(hasOnlyStructuredMessageHeaders(recorded)))
            .verifyComplete();
    }

    @Test
    public void stageBlockWithNoContentValidation() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(getBlockID(), data)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        StepVerifier.create(client.stageBlockWithResponse(options))
            .assertNext(response -> assertTrue(hasNoContentValidationHeaders(recorded)))
            .verifyComplete();
    }

    // ===========================================================================================
    // AppendBlobAsyncClient.appendBlockWithResponse (Append Block) tests
    // ===========================================================================================

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void appendBlockWithCrc64Header(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        AppendBlobAsyncClient client = blobClient.getAppendBlobAsyncClient();
        client.create().block();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        AppendBlobAppendBlockOptions options
            = new AppendBlobAppendBlockOptions(data, UNDER_4MB).setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.appendBlockWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void appendBlockWithStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        AppendBlobAsyncClient client = blobClient.getAppendBlobAsyncClient();
        client.create().block();

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        AppendBlobAppendBlockOptions options
            = new AppendBlobAppendBlockOptions(data, TEN_MB).setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.appendBlockWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
    }

    @Test
    public void appendBlockWithNoContentValidation() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        AppendBlobAsyncClient client = blobClient.getAppendBlobAsyncClient();
        client.create().block();

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        AppendBlobAppendBlockOptions options = new AppendBlobAppendBlockOptions(data, TEN_MB)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        StepVerifier.create(client.appendBlockWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasNoContentValidationHeaders(recorded));
        }).verifyComplete();
    }

    // ===========================================================================================
    // PageBlobAsyncClient.uploadPagesWithResponse (Put Page) tests
    // ===========================================================================================

    private static final int PAGE_BYTES = PageBlobClient.PAGE_BYTES;
    private static final int UNDER_4MB_PAGE_ALIGNED = (UNDER_4MB / PAGE_BYTES) * PAGE_BYTES;
    private static final int FOUR_MB_PAGE_ALIGNED = (4 * Constants.MB / PAGE_BYTES) * PAGE_BYTES;

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadPagesWithCrc64Header(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        PageBlobAsyncClient client = blobClient.getPageBlobAsyncClient();
        client.create(UNDER_4MB_PAGE_ALIGNED).block();

        byte[] randomData = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1), data)
                .setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.uploadPagesWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadPagesWithStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        PageBlobAsyncClient client = blobClient.getPageBlobAsyncClient();
        client.create(FOUR_MB_PAGE_ALIGNED).block();

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1), data)
                .setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.uploadPagesWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
    }

    @Test
    public void uploadPagesWithNoContentValidation() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        PageBlobAsyncClient client = blobClient.getPageBlobAsyncClient();
        client.create(FOUR_MB_PAGE_ALIGNED).block();

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1), data)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        StepVerifier.create(client.uploadPagesWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasNoContentValidationHeaders(recorded));
        }).verifyComplete();
    }

    // ===========================================================================================
    // BlobAsyncClient.uploadFromFileWithResponse tests
    // ===========================================================================================

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileWithCrc64Header(ContentValidationAlgorithm algorithm) throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(UNDER_4MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.uploadFromFileWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileWithStructuredMessage(ContentValidationAlgorithm algorithm) throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(TEN_MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.uploadFromFileWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
    }

    @LiveOnly // Put Block URLs include random block IDs; not replayable with the test proxy.
    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileChunkedWithStructuredMessage(ContentValidationAlgorithm algorithm) throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(TEN_MB);
        long blockSize = 2 * (long) Constants.MB;

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.uploadFromFileWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
    }

    @Test
    public void uploadFromFileWithNoContentValidation() throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(TEN_MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        StepVerifier.create(client.uploadFromFileWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasNoContentValidationHeaders(recorded));
        }).verifyComplete();
    }

    // ===========================================================================================
    // Exact 4MB boundary tests
    //
    // The cutoff between CRC64 header and structured message is exactly 4MB.
    // Uploads of exactly 4MB should use structured message (>= threshold), not CRC64 header.
    // ===========================================================================================

    private static final int EXACTLY_4MB = 4 * Constants.MB;

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadAtExactly4MBUsesStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(EXACTLY_4MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) EXACTLY_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobSimpleUploadAtExactly4MBUsesStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(EXACTLY_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
    }

    // ===========================================================================================
    // Progress reporting (transfer validation must be NONE/null when a progress listener is set)
    // ===========================================================================================

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadWithProgressAndNonNoneContentValidationThrows(ContentValidationAlgorithm algorithm) {
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data).setParallelTransferOptions(
            new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB).setProgressListener(l -> {
            })).setRequestConditions(new BlobRequestConditions()).setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.uploadWithResponse(options))
            .expectErrorSatisfies(ex -> assertEquals(
                ContentValidationModeResolver.PROGRESS_CONFLICTS_TRANSFER_CONTENT_VALIDATION_MESSAGE, ex.getMessage()))
            .verify();
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileWithProgressAndNonNoneContentValidationThrows(ContentValidationAlgorithm algorithm)
        throws IOException {
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        File tempFile = getRandomFile(TEN_MB);

        BlobUploadFromFileOptions options
            = new BlobUploadFromFileOptions(tempFile.getAbsolutePath()).setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB).setProgressListener(l -> {
                })).setContentValidationAlgorithm(algorithm);

        StepVerifier.create(client.uploadFromFileWithResponse(options))
            .expectErrorSatisfies(ex -> assertEquals(
                ContentValidationModeResolver.PROGRESS_CONFLICTS_TRANSFER_CONTENT_VALIDATION_MESSAGE, ex.getMessage()))
            .verify();
    }

    // ===========================================================================================
    // Data integrity round-trip tests (upload with content validation, download, verify)
    //
    // Previous tests verify that the correct headers are sent. These tests verify end-to-end
    // integrity: the data uploaded with CRC64/structured message can be downloaded and matches
    // the original byte-for-byte.
    // ===========================================================================================

    @Test
    public void uploadWithCrc64RoundTripDataIntegrity() {
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(client.uploadWithResponse(options).then(client.downloadContent()))
            .assertNext(downloaded -> assertArrayEquals(randomData, downloaded.toBytes()))
            .verifyComplete();
    }

    @Test
    public void uploadWithStructuredMessageRoundTripDataIntegrity() {
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(client.uploadWithResponse(options).then(client.downloadContent()))
            .assertNext(downloaded -> assertArrayEquals(randomData, downloaded.toBytes()))
            .verifyComplete();
    }

    @LiveOnly // Put Block URLs include random block IDs; not replayable with the test proxy.
    @Test
    public void uploadChunkedWithStructuredMessageRoundTripDataIntegrity() {
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));
        long blockSize = 2 * (long) Constants.MB;

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(client.uploadWithResponse(options).then(client.downloadContent()))
            .assertNext(downloaded -> assertArrayEquals(randomData, downloaded.toBytes()))
            .verifyComplete();
    }

    @Test
    public void blockBlobSimpleUploadRoundTripDataIntegrity() {
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(client.uploadWithResponse(options).then(client.downloadContent()))
            .assertNext(downloaded -> assertArrayEquals(randomData, downloaded.toBytes()))
            .verifyComplete();
    }

    @Test
    public void appendBlockRoundTripDataIntegrity() {
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        AppendBlobAsyncClient client = blobClient.getAppendBlobAsyncClient();
        client.create().block();

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        AppendBlobAppendBlockOptions options = new AppendBlobAppendBlockOptions(data, TEN_MB)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(client.appendBlockWithResponse(options).then(client.downloadContent()))
            .assertNext(downloaded -> assertArrayEquals(randomData, downloaded.toBytes()))
            .verifyComplete();
    }

    @Test
    public void uploadPagesRoundTripDataIntegrity() {
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        PageBlobAsyncClient client = blobClient.getPageBlobAsyncClient();
        client.create(FOUR_MB_PAGE_ALIGNED).block();

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1), data)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(client.uploadPagesWithResponse(options).then(client.downloadContent()))
            .assertNext(downloaded -> assertArrayEquals(randomData, downloaded.toBytes()))
            .verifyComplete();
    }

    // ===========================================================================================
    // Live-only random payload bands.
    //   - 256–500 MiB: parallelUpload, uploadFromFile, stageBlock — parallel staging / giant block; default transfer
    //     options where applicable.
    //   - 32–64 MiB (sequential append blocks only): appendBlockLiveRandom… — one append REST call per chunk in order.
    // ===========================================================================================

    @LiveOnly // This test is too large for the test proxy.
    @Test
    public void parallelUploadLiveRandomRoundTripDataIntegrity() throws Exception {
        int chosenPayloadSizeBytes = (int) randomLongFromNamer(LIVE_RANDOM_PARALLEL_PAYLOAD_MIN_BYTES_EXCLUSIVE + 1,
            LIVE_RANDOM_PARALLEL_PAYLOAD_MAX_BYTES_INCLUSIVE + 1);
        try {
            String prefix = "chosenPayloadSizeBytes=" + chosenPayloadSizeBytes + ". ";
            BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
            File sourceFile = getRandomFile(chosenPayloadSizeBytes);
            File outFile = Files.createTempFile("blob-cv-live-par-dl-async", ".bin").toFile();
            outFile.deleteOnExit();

            try {
                try (InputStream data = new FileInputStream(sourceFile)) {
                    BlobParallelUploadOptions options
                        = new BlobParallelUploadOptions(data).setRequestConditions(new BlobRequestConditions())
                            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
                    client.uploadWithResponse(options).block();
                }

                client.downloadToFile(outFile.getPath(), true).block();
                assertTrue(compareFiles(sourceFile, outFile, 0, chosenPayloadSizeBytes), prefix);
            } finally {
                if (!sourceFile.delete()) {
                    sourceFile.deleteOnExit();
                }
                if (!outFile.delete()) {
                    outFile.deleteOnExit();
                }
            }
        } catch (Exception e) {
            throw new Exception("chosenPayloadSizeBytes=" + chosenPayloadSizeBytes + ". " + e.getMessage(), e);
        }
    }

    @LiveOnly // This test is too large for the test proxy.
    @Test
    public void stageBlockLiveRandomRoundTripDataIntegrity() throws Exception {
        int chosenPayloadSizeBytes = (int) randomLongFromNamer(LIVE_RANDOM_PARALLEL_PAYLOAD_MIN_BYTES_EXCLUSIVE + 1,
            LIVE_RANDOM_PARALLEL_PAYLOAD_MAX_BYTES_INCLUSIVE + 1);
        try {
            String prefix = "chosenPayloadSizeBytes=" + chosenPayloadSizeBytes + ". ";
            BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
            BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();
            String blockId = getBlockID();

            File sourceFile = getRandomFile(chosenPayloadSizeBytes);
            File outFile = Files.createTempFile("blob-cv-live-stage-async-dl", ".bin").toFile();
            outFile.deleteOnExit();
            try {
                BinaryData binaryData = BinaryData.fromFile(sourceFile.toPath());
                BlockBlobStageBlockOptions stageOptions = new BlockBlobStageBlockOptions(blockId, binaryData)
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
                client.stageBlockWithResponse(stageOptions).block();
                client.commitBlockList(Collections.singletonList(blockId)).block();
                blobClient.downloadToFile(outFile.getPath(), true).block();
                assertTrue(compareFiles(sourceFile, outFile, 0, chosenPayloadSizeBytes), prefix);
            } finally {
                if (!sourceFile.delete()) {
                    sourceFile.deleteOnExit();
                }
                if (!outFile.delete()) {
                    outFile.deleteOnExit();
                }
            }
        } catch (Exception e) {
            throw new Exception("chosenPayloadSizeBytes=" + chosenPayloadSizeBytes + ". " + e.getMessage(), e);
        }
    }

    @LiveOnly // This test is too large for the test proxy.
    @Test
    public void appendBlockLiveRandomRoundTripDataIntegrity() throws Exception {
        int chosenPayloadSizeBytes
            = (int) randomLongFromNamer(LIVE_RANDOM_SEQUENTIAL_APPEND_PAYLOAD_MIN_BYTES_EXCLUSIVE + 1,
                LIVE_RANDOM_SEQUENTIAL_APPEND_PAYLOAD_MAX_BYTES_INCLUSIVE + 1);
        try {
            String prefix = "chosenPayloadSizeBytes=" + chosenPayloadSizeBytes + ". ";
            BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
            AppendBlobAsyncClient client = blobClient.getAppendBlobAsyncClient();
            client.create().block();

            int maxAppendBlockBytes = client.getMaxAppendBlockBytes();
            File sourceFile = getRandomFile(chosenPayloadSizeBytes);
            File outFile = Files.createTempFile("blob-cv-live-append-async-dl", ".bin").toFile();
            outFile.deleteOnExit();

            try {
                try (AsynchronousFileChannel channel
                    = AsynchronousFileChannel.open(sourceFile.toPath(), StandardOpenOption.READ)) {
                    FluxUtil.readFile(channel, maxAppendBlockBytes, 0, chosenPayloadSizeBytes).concatMap(bb -> {
                        AppendBlobAppendBlockOptions appendOptions
                            = new AppendBlobAppendBlockOptions(Flux.just(bb), bb.remaining())
                                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
                        return client.appendBlockWithResponse(appendOptions);
                    }).then().block();
                }

                blobClient.downloadToFile(outFile.getPath(), true).block();
                assertTrue(compareFiles(sourceFile, outFile, 0, chosenPayloadSizeBytes), prefix);
            } finally {
                if (!sourceFile.delete()) {
                    sourceFile.deleteOnExit();
                }
                if (!outFile.delete()) {
                    outFile.deleteOnExit();
                }
            }
        } catch (Exception e) {
            throw new Exception("chosenPayloadSizeBytes=" + chosenPayloadSizeBytes + ". " + e.getMessage(), e);
        }
    }

    @LiveOnly // This test is too large for the test proxy.
    @Test
    public void uploadFromFileLiveRandomRoundTripDataIntegrity() throws Exception {
        int chosenPayloadSizeBytes = (int) randomLongFromNamer(LIVE_RANDOM_PARALLEL_PAYLOAD_MIN_BYTES_EXCLUSIVE + 1,
            LIVE_RANDOM_PARALLEL_PAYLOAD_MAX_BYTES_INCLUSIVE + 1);
        try {
            String prefix = "chosenPayloadSizeBytes=" + chosenPayloadSizeBytes + ". ";
            BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
            File sourceFile = getRandomFile(chosenPayloadSizeBytes);
            File outFile = Files.createTempFile("blob-cv-live-uploadfromfile-async-dl", ".bin").toFile();
            outFile.deleteOnExit();
            try {
                BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(sourceFile.getAbsolutePath())
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
                assertNotNull(client.uploadFromFileWithResponse(options).block().getValue().getETag(),
                    prefix + "Missing E-Tag on upload-from-file.");
                client.downloadToFile(outFile.getPath(), true).block();
                assertTrue(compareFiles(sourceFile, outFile, 0, chosenPayloadSizeBytes), prefix);
            } finally {
                if (!sourceFile.delete()) {
                    sourceFile.deleteOnExit();
                }
                if (!outFile.delete()) {
                    outFile.deleteOnExit();
                }
            }
        } catch (Exception e) {
            throw new Exception("chosenPayloadSizeBytes=" + chosenPayloadSizeBytes + ". " + e.getMessage(), e);
        }
    }

    // ---------- Deterministic parallel upload (async) ----------

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelUploadPutBlobReplayableCases")
    public void fuzzyParallelUploadPutBlobReplayableRoundTrip(int payloadBytes, long segmentBytes, int maxConcurrency)
        throws IOException {
        assertParallelUploadFuzzyRoundTripAsync("putBlobReplay", payloadBytes, segmentBytes, maxConcurrency);
    }

    @LiveOnly // Staging-only cases: Put Block URLs include random IDs.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelUploadSmallPayloadStagingCases")
    public void fuzzyParallelUploadSmallPayloadRoundTripRequiresLiveStaging(int payloadBytes, long segmentBytes,
        int maxConcurrency) throws IOException {
        assertParallelUploadFuzzyRoundTripAsync("smallPayloadStaging", payloadBytes, segmentBytes, maxConcurrency);
    }

    @LiveOnly // payload > segment for every tuple; always staging/Put Block.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelUploadSub4MiBCases")
    public void fuzzyParallelUploadSubFourMiBBlobRoundTrip(int payloadBytes, long segmentBytes, int maxConcurrency)
        throws IOException {
        assertParallelUploadFuzzyRoundTripAsync("subFourMiB", payloadBytes, segmentBytes, maxConcurrency);
    }

    @LiveOnly // Staging-only cases.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelUploadFourMiBBoundaryStagingCases")
    public void fuzzyParallelUploadFourMiBBoundaryRoundTripRequiresLiveStaging(int payloadBytes, long segmentBytes,
        int maxConcurrency) throws IOException {
        assertParallelUploadFuzzyRoundTripAsync("fourMiBBoundaryStaging", payloadBytes, segmentBytes, maxConcurrency);
    }

    @LiveOnly // Chunked uploads only.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelUploadMediumMultiPartCases")
    public void fuzzyParallelUploadMediumMultiPartRoundTrip(int payloadBytes, long segmentBytes, int maxConcurrency)
        throws IOException {
        assertParallelUploadFuzzyRoundTripAsync("mediumMultiPart", payloadBytes, segmentBytes, maxConcurrency);
    }

    @LiveOnly // Large chunked uploads.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelUploadLargeMultiPartCases")
    public void fuzzyParallelUploadLargeMultiPartRoundTrip(int payloadBytes, long segmentBytes, int maxConcurrency)
        throws IOException {
        assertParallelUploadFuzzyRoundTripAsync("largeMultiPart", payloadBytes, segmentBytes, maxConcurrency);
    }

    private void assertParallelUploadFuzzyRoundTripAsync(String caseKind, int payloadBytes, long segmentBytes,
        int maxConcurrency) throws IOException {
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong(segmentBytes)
            .setMaxSingleUploadSizeLong(segmentBytes)
            .setMaxConcurrency(maxConcurrency);

        String assertionMessage = "Fuzzy parallel upload [" + caseKind + "] payloadBytes=" + payloadBytes
            + ", segmentBytes=" + segmentBytes + ", maxConcurrency=" + maxConcurrency;

        // above this threshold the fuzzy parallel upload helpers stream from a temp source file
        // to avoid materializing the full payload twice in heap.
        if (payloadBytes >= 96 * Constants.MB) {
            File sourceFile = getRandomFile(payloadBytes);
            File outFile = Files.createTempFile("blob-cv-fuzzy-parallel-dl-async", ".bin").toFile();
            outFile.deleteOnExit();
            int readChunkSize = (int) Math.min(8L * Constants.MB, Math.max(64 * Constants.KB, segmentBytes));
            AsynchronousFileChannel channel
                = AsynchronousFileChannel.open(sourceFile.toPath(), StandardOpenOption.READ);
            try {
                try {
                    Flux<ByteBuffer> data = FluxUtil.readFile(channel, readChunkSize, 0, payloadBytes);
                    BlobParallelUploadOptions options
                        = new BlobParallelUploadOptions(data).setParallelTransferOptions(parallelOptions)
                            .setRequestConditions(new BlobRequestConditions())
                            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
                    client.uploadWithResponse(options).block();
                } finally {
                    channel.close();
                }
                client.downloadToFile(outFile.getPath(), true).block();
                assertTrue(compareFiles(sourceFile, outFile, 0, payloadBytes), assertionMessage);
            } finally {
                if (!sourceFile.delete()) {
                    sourceFile.deleteOnExit();
                }
                if (!outFile.delete()) {
                    outFile.deleteOnExit();
                }
            }
        } else {
            byte[] randomData = getRandomByteArray(payloadBytes);
            Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));
            BlobParallelUploadOptions options
                = new BlobParallelUploadOptions(data).setParallelTransferOptions(parallelOptions)
                    .setRequestConditions(new BlobRequestConditions())
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

            StepVerifier.create(client.uploadWithResponse(options).then(client.downloadContent()))
                .assertNext(downloaded -> assertArrayEquals(randomData, downloaded.toBytes(), assertionMessage))
                .verifyComplete();
        }
    }

    // ===========================================================================================
    // Customer Provided MD5 Byte[] with Content Validation Algorithm
    // ===========================================================================================

    private static final byte[] DEFAULT_MD5 = createDefaultMd5();
    private static final String MESSAGE = "Both x-ms-content-crc64 header and Content-MD5 header are present.";

    private static byte[] createDefaultMd5() {
        try {
            return Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes()));
        } catch (NoSuchAlgorithmException ex) {
            throw LOGGER.logExceptionAsError(new RuntimeException("MD5 algorithm unavailable.", ex));
        }
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobUploadWithCustomerProvidedMd5AndCrc64Header(ContentValidationAlgorithm algorithm) {
        BlockBlobAsyncClient client
            = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>()).getBlockBlobAsyncClient();

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(DATA.getDefaultBinaryData()).setContentValidationAlgorithm(algorithm)
                .setContentMd5(DEFAULT_MD5);

        StepVerifier.create(client.uploadWithResponse(options)).verifyErrorSatisfies(ex -> {
            BlobStorageException e = assertInstanceOf(BlobStorageException.class, ex);
            assertEquals(400, e.getStatusCode());
            assertTrue(e.getMessage().contains(MESSAGE));
        });
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void stageBlockWithCustomerProvidedMd5AndCrc64Header(ContentValidationAlgorithm algorithm) {
        BlockBlobAsyncClient client
            = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>()).getBlockBlobAsyncClient();

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(getBlockID(), DATA.getDefaultBinaryData())
            .setContentValidationAlgorithm(algorithm)
            .setContentMd5(DEFAULT_MD5);

        StepVerifier.create(client.stageBlockWithResponse(options)).verifyErrorSatisfies(ex -> {
            BlobStorageException e = assertInstanceOf(BlobStorageException.class, ex);
            assertEquals(400, e.getStatusCode());
            assertTrue(e.getMessage().contains(MESSAGE));
        });
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void appendBlockWithCustomerProvidedMd5AndCrc64Header(ContentValidationAlgorithm algorithm) {
        AppendBlobAsyncClient client
            = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>()).getAppendBlobAsyncClient();
        client.create().block();

        byte[] randomData = DATA.getDefaultBytes();
        AppendBlobAppendBlockOptions options
            = new AppendBlobAppendBlockOptions(Flux.just(ByteBuffer.wrap(randomData)), randomData.length)
                .setContentValidationAlgorithm(algorithm)
                .setContentMd5(DEFAULT_MD5);

        StepVerifier.create(client.appendBlockWithResponse(options)).verifyErrorSatisfies(ex -> {
            BlobStorageException e = assertInstanceOf(BlobStorageException.class, ex);
            assertEquals(400, e.getStatusCode());
            assertTrue(e.getMessage().contains(MESSAGE));
        });
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadPagesWithCustomerProvidedMd5AndCrc64Header(ContentValidationAlgorithm algorithm)
        throws NoSuchAlgorithmException {
        PageBlobAsyncClient client
            = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>()).getPageBlobAsyncClient();
        client.create(UNDER_4MB_PAGE_ALIGNED).block();

        byte[] randomData = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(randomData);
        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1),
                Flux.just(ByteBuffer.wrap(randomData))).setContentValidationAlgorithm(algorithm).setContentMd5(md5);

        StepVerifier.create(client.uploadPagesWithResponse(options)).verifyErrorSatisfies(ex -> {
            BlobStorageException e = assertInstanceOf(BlobStorageException.class, ex);
            assertEquals(400, e.getStatusCode());
            assertTrue(e.getMessage().contains(MESSAGE));
        });
    }
}
