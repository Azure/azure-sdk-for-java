// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.models.BlobRequestConditions;
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
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static final long LARGE_UPLOAD_MIN_BYTES = 500L * Constants.MB;
    private static final long LARGE_UPLOAD_MAX_BYTES = Constants.GB;
    private static final long LARGE_UPLOAD_BLOCK_SIZE_BYTES = 8L * Constants.MB;
    private static final int LARGE_UPLOAD_MAX_CONCURRENCY = 8;

    private static final String MD5_AND_CRC64_EXCLUSIVE_MESSAGE
        = "Only one form of transactional content validation may be used.";

    private static final String UPLOAD_FROM_FILE_MD5_NOT_SUPPORTED_MESSAGE
        = "ContentValidationAlgorithm.MD5 is not supported for uploadFromFile. Use CRC64 or AUTO instead.";

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
     * Multi-part (chunked) upload; content validation uses structured message on each stage block.
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

    /**
     * Block blob simple upload rejects using both MD5 (contentMd5) and CRC64 (transfer validation checksum algorithm) at once.
     */
    @Test
    public void blockBlobSimpleUploadWithMd5AndCrc64Throws() throws NoSuchAlgorithmException {
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(randomData);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options = new BlockBlobSimpleUploadOptions(data).setContentMd5(md5)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(client.uploadWithResponse(options))
            .verifyErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
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

        StepVerifier.create(client.stageBlockWithResponse(options)).assertNext(response -> {
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
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

        StepVerifier.create(client.stageBlockWithResponse(options)).assertNext(response -> {
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
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

        StepVerifier.create(client.stageBlockWithResponse(options)).assertNext(response -> {
            assertTrue(hasNoContentValidationHeaders(recorded));
        }).verifyComplete();
    }

    /**
     * Stage block rejects using both MD5 (contentMd5) and CRC64 (transfer validation checksum algorithm) at once.
     */
    @Test
    public void stageBlockWithMd5AndCrc64Throws() throws NoSuchAlgorithmException {
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(randomData);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(getBlockID(), data).setContentMd5(md5)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(client.stageBlockWithResponse(options))
            .verifyErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
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

    /**
     * Append block rejects using both MD5 (contentMd5) and CRC64 (transfer validation checksum algorithm) at once.
     */
    @Test
    public void appendBlockWithMd5AndCrc64Throws() throws NoSuchAlgorithmException {
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        AppendBlobAsyncClient client = blobClient.getAppendBlobAsyncClient();
        client.create().block();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(randomData);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        AppendBlobAppendBlockOptions options = new AppendBlobAppendBlockOptions(data, UNDER_4MB).setContentMd5(md5)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(client.appendBlockWithResponse(options))
            .verifyErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
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

    /**
     * Upload pages rejects using both MD5 (contentMd5) and CRC64 (transfer validation checksum algorithm) at once.
     */
    @Test
    public void uploadPagesWithMd5AndCrc64Throws() throws NoSuchAlgorithmException {
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        PageBlobAsyncClient client = blobClient.getPageBlobAsyncClient();
        client.create(UNDER_4MB_PAGE_ALIGNED).block();

        byte[] randomData = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(randomData);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1), data)
                .setContentMd5(md5)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        StepVerifier.create(client.uploadPagesWithResponse(options))
            .verifyErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
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

        client.uploadWithResponse(options).block();

        byte[] downloaded = client.downloadContent().block().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded data (CRC64 header path)");
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

        client.uploadWithResponse(options).block();

        byte[] downloaded = client.downloadContent().block().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded data (structured message path)");
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

        client.uploadWithResponse(options).block();

        byte[] downloaded = client.downloadContent().block().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match uploaded data (chunked structured message path)");
    }

    @Test
    public void blockBlobSimpleUploadRoundTripDataIntegrity() {
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadWithResponse(options).block();

        byte[] downloaded = blobClient.downloadContent().block().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match uploaded data (block blob simple upload)");
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

        client.appendBlockWithResponse(options).block();

        byte[] downloaded = blobClient.downloadContent().block().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded data (append block)");
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

        client.uploadPagesWithResponse(options).block();

        byte[] downloaded = blobClient.downloadContent().block().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded data (page blob upload pages)");
    }

    // ===========================================================================================
    // Randomized payload sizes (exercises CRC64 header vs structured message lengths across runs)
    // ===========================================================================================

    @Test
    public void uploadWithRandomSizeCrc64HeaderRoundTripDataIntegrity() {
        int size = randomIntFromNamer(Constants.KB, EXACTLY_4MB);

        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        byte[] randomData = getRandomByteArray(size);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) size))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadWithResponse(options).block();

        byte[] downloaded = client.downloadContent().block().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match uploaded data (random size CRC64 header path, size=" + size + ")");
    }

    @LiveOnly // This test is too large for the test proxy.
    @Test
    public void uploadWithRandomSizeStructuredMessageRoundTripDataIntegrity() {
        int size = randomIntFromNamer(EXACTLY_4MB, 48 * Constants.MB + 1);

        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        byte[] randomData = getRandomByteArray(size);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) size))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadWithResponse(options).block();

        byte[] downloaded = client.downloadContent().block().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match uploaded data (random size structured message path, size=" + size + ")");
    }

    @LiveOnly // Put Block URLs include random block IDs; not replayable with the test proxy.
    @Test
    public void uploadChunkedRandomSizesRoundTripDataIntegrity() {
        Random rnd = newRandomFromNamer();
        long[] blockSizeChoices = { Constants.MB, 2L * Constants.MB, 4L * Constants.MB, 8L * Constants.MB };
        long blockSize = blockSizeChoices[rnd.nextInt(blockSizeChoices.length)];
        int minTotal = (int) Math.max(24L * Constants.MB, 2 * blockSize);
        int totalSize = minTotal + rnd.nextInt(80 * Constants.MB + 1 - minTotal);

        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        byte[] randomData = getRandomByteArray(totalSize);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadWithResponse(options).block();

        byte[] downloaded = client.downloadContent().block().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match uploaded data (random chunked path, total=" + totalSize + ", block=" + blockSize
                + ")");
    }

    @LiveOnly // This test is too large for the test proxy.
    @Test
    public void blockBlobSimpleUploadRandomSizeRoundTripDataIntegrity() {
        int size = randomIntFromNamer(Constants.KB, 48 * Constants.MB + 1);

        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();
        byte[] randomData = getRandomByteArray(size);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadWithResponse(options).block();

        byte[] downloaded = blobClient.downloadContent().block().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match uploaded data (random block blob simple upload, size=" + size + ")");
    }

    @LiveOnly // This test is too large for the test proxy.
    @Test
    public void stageBlockRandomSizeRoundTripDataIntegrity() {
        int size = randomIntFromNamer(Constants.KB, 40 * Constants.MB + 1);

        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();
        String blockId = getBlockID();
        byte[] randomData = getRandomByteArray(size);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(blockId, data)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.stageBlockWithResponse(options).block();
        client.commitBlockList(Collections.singletonList(blockId)).block();

        byte[] downloaded = blobClient.downloadContent().block().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match staged block (random size, size=" + size + ")");
    }

    @LiveOnly // This test is too large for the test proxy.
    @Test
    public void appendBlockRandomSizeRoundTripDataIntegrity() {
        int size = randomIntFromNamer(Constants.KB, 80 * Constants.MB + 1);

        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        AppendBlobAsyncClient client = blobClient.getAppendBlobAsyncClient();
        client.create().block();

        byte[] randomData = getRandomByteArray(size);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        AppendBlobAppendBlockOptions options = new AppendBlobAppendBlockOptions(data, size)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.appendBlockWithResponse(options).block();

        byte[] downloaded = blobClient.downloadContent().block().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match append block (random size, size=" + size + ")");
    }

    @Test
    public void uploadPagesRandomAlignedSizeRoundTripDataIntegrity() {
        // Put Page allows at most 4 MiB per request; keep one uploadPages call within that limit.
        int minPages = 1;
        int maxPages = FOUR_MB_PAGE_ALIGNED / PAGE_BYTES;
        int numPages = randomIntFromNamer(minPages, maxPages + 1);
        int sizeBytes = numPages * PAGE_BYTES;

        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        PageBlobAsyncClient client = blobClient.getPageBlobAsyncClient();
        client.create(sizeBytes).block();

        byte[] randomData = getRandomByteArray(sizeBytes);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(sizeBytes - 1), data)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadPagesWithResponse(options).block();

        byte[] downloaded = blobClient.downloadContent().block().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match page upload (random aligned size, size=" + sizeBytes + ")");
    }

    // ===========================================================================================
    // Large blob uploads (500 MiB–1 GiB) with parallel block staging and concurrency
    // ===========================================================================================

    @LiveOnly
    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileLargeBlobChunkedWithConcurrency(ContentValidationAlgorithm algorithm) throws IOException {
        int sizeBytes = (int) randomLongFromNamer(LARGE_UPLOAD_MIN_BYTES, LARGE_UPLOAD_MAX_BYTES + 1);
        File sourceFile = getRandomFile(sizeBytes);
        File outFile = Files.createTempFile("blob-cv-large-dl-async", ".bin").toFile();
        outFile.deleteOnExit();

        try {
            BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
            ParallelTransferOptions parallelTransferOptions
                = new ParallelTransferOptions().setBlockSizeLong(LARGE_UPLOAD_BLOCK_SIZE_BYTES)
                    .setMaxSingleUploadSizeLong(LARGE_UPLOAD_BLOCK_SIZE_BYTES)
                    .setMaxConcurrency(LARGE_UPLOAD_MAX_CONCURRENCY);

            BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(sourceFile.getAbsolutePath())
                .setParallelTransferOptions(parallelTransferOptions)
                .setContentValidationAlgorithm(algorithm);

            assertNotNull(client.uploadFromFileWithResponse(options).block().getValue().getETag());
            client.downloadToFile(outFile.getPath(), true).block();
            assertTrue(compareFiles(sourceFile, outFile, 0, sizeBytes),
                "Downloaded file must match source (large chunked upload, size=" + sizeBytes + ")");
        } finally {
            if (!sourceFile.delete()) {
                sourceFile.deleteOnExit();
            }
        }
    }
}
