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
import com.azure.storage.common.StorageChecksumAlgorithm;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

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

    private static final String MD5_AND_CRC64_EXCLUSIVE_MESSAGE
        = "Only one form of transactional content validation may be used.";

    private static final String UPLOAD_FROM_FILE_MD5_NOT_SUPPORTED_MESSAGE
        = "StorageChecksumAlgorithm.MD5 is not supported for uploadFromFile. Use CRC64 or AUTO instead.";

    // ===========================================================================================
    // BlobAsyncClient.uploadWithResponse
    // ===========================================================================================

    /**
     * Single-part upload under 4MB: content validation uses CRC64 header only (no structured message).
     */
    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadWithCrc64Header(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(algorithm);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    /**
     * Single-part upload >= 4MB: content validation uses structured message.
     */
    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadWithStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(algorithm);

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
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadChunkedWithStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));
        long blockSize = 2 * (long) Constants.MB;

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(algorithm);

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
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasNoContentValidationHeaders(recorded));
        }).verifyComplete();
    }

    /**
     * Blob parallel upload rejects using both computeMd5 (SDK-computed MD5) and CRC64 (requestChecksumAlgorithm) at once.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void uploadWithComputeMd5AndCrc64Throws() {
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setComputeMd5(true)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.uploadWithResponse(options))
            .verifyErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
    }

    // ===========================================================================================
    // BlockBlobAsyncClient.uploadWithResponse (BlockBlobSimpleUpload / Put Blob) tests
    // ===========================================================================================

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobSimpleUploadWithCrc64Header(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setRequestChecksumAlgorithm(algorithm);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobSimpleUploadWithStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setRequestChecksumAlgorithm(algorithm);

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
            = new BlockBlobSimpleUploadOptions(data).setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasNoContentValidationHeaders(recorded));
        }).verifyComplete();
    }

    /**
     * Block blob simple upload rejects using both MD5 (contentMd5) and CRC64 (requestChecksumAlgorithm) at once.
     */
    @Test
    public void blockBlobSimpleUploadWithMd5AndCrc64Throws() throws NoSuchAlgorithmException {
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(randomData);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options = new BlockBlobSimpleUploadOptions(data).setContentMd5(md5)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.uploadWithResponse(options))
            .verifyErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
    }

    // ===========================================================================================
    // BlockBlobAsyncClient.stageBlockWithResponse (Put Block) tests
    // ===========================================================================================

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void stageBlockWithCrc64Header(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options
            = new BlockBlobStageBlockOptions(getBlockID(), data).setRequestChecksumAlgorithm(algorithm);

        StepVerifier.create(client.stageBlockWithResponse(options)).assertNext(response -> {
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void stageBlockWithStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options
            = new BlockBlobStageBlockOptions(getBlockID(), data).setRequestChecksumAlgorithm(algorithm);

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
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        StepVerifier.create(client.stageBlockWithResponse(options)).assertNext(response -> {
            assertTrue(hasNoContentValidationHeaders(recorded));
        }).verifyComplete();
    }

    /**
     * Stage block rejects using both MD5 (contentMd5) and CRC64 (requestChecksumAlgorithm) at once.
     */
    @Test
    public void stageBlockWithMd5AndCrc64Throws() throws NoSuchAlgorithmException {
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(randomData);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(getBlockID(), data).setContentMd5(md5)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.stageBlockWithResponse(options))
            .verifyErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
    }

    // ===========================================================================================
    // AppendBlobAsyncClient.appendBlockWithResponse (Append Block) tests
    // ===========================================================================================

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void appendBlockWithCrc64Header(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        AppendBlobAsyncClient client = blobClient.getAppendBlobAsyncClient();
        client.create().block();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        AppendBlobAppendBlockOptions options
            = new AppendBlobAppendBlockOptions(data, UNDER_4MB).setRequestChecksumAlgorithm(algorithm);

        StepVerifier.create(client.appendBlockWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void appendBlockWithStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        AppendBlobAsyncClient client = blobClient.getAppendBlobAsyncClient();
        client.create().block();

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        AppendBlobAppendBlockOptions options
            = new AppendBlobAppendBlockOptions(data, TEN_MB).setRequestChecksumAlgorithm(algorithm);

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

        AppendBlobAppendBlockOptions options
            = new AppendBlobAppendBlockOptions(data, TEN_MB).setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        StepVerifier.create(client.appendBlockWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasNoContentValidationHeaders(recorded));
        }).verifyComplete();
    }

    /**
     * Append block rejects using both MD5 (contentMd5) and CRC64 (requestChecksumAlgorithm) at once.
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
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

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
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadPagesWithCrc64Header(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        PageBlobAsyncClient client = blobClient.getPageBlobAsyncClient();
        client.create(UNDER_4MB_PAGE_ALIGNED).block();

        byte[] randomData = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1), data)
                .setRequestChecksumAlgorithm(algorithm);

        StepVerifier.create(client.uploadPagesWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadPagesWithStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        PageBlobAsyncClient client = blobClient.getPageBlobAsyncClient();
        client.create(FOUR_MB_PAGE_ALIGNED).block();

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1), data)
                .setRequestChecksumAlgorithm(algorithm);

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
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        StepVerifier.create(client.uploadPagesWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasNoContentValidationHeaders(recorded));
        }).verifyComplete();
    }

    /**
     * Upload pages rejects using both MD5 (contentMd5) and CRC64 (requestChecksumAlgorithm) at once.
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
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.uploadPagesWithResponse(options))
            .verifyErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
    }

    // ===========================================================================================
    // BlobAsyncClient.uploadFromFileWithResponse tests
    // ===========================================================================================

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileWithCrc64Header(StorageChecksumAlgorithm algorithm) throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(UNDER_4MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestChecksumAlgorithm(algorithm);

        StepVerifier.create(client.uploadFromFileWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileWithStructuredMessage(StorageChecksumAlgorithm algorithm) throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(TEN_MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestChecksumAlgorithm(algorithm);

        StepVerifier.create(client.uploadFromFileWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
    }

    @LiveOnly // Put Block URLs include random block IDs; not replayable with the test proxy.
    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileChunkedWithStructuredMessage(StorageChecksumAlgorithm algorithm) throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(TEN_MB);
        long blockSize = 2 * (long) Constants.MB;

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestChecksumAlgorithm(algorithm);

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
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        StepVerifier.create(client.uploadFromFileWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasNoContentValidationHeaders(recorded));
        }).verifyComplete();
    }

    /**
     * Upload from file rejects requestChecksumAlgorithm MD5 because the SDK does not support computing MD5 for file uploads.
     */
    @Test
    public void uploadFromFileWithRequestChecksumAlgorithmMd5Throws() throws IOException {
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        File tempFile = getRandomFile(UNDER_4MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.MD5);

        StepVerifier.create(client.uploadFromFileWithResponse(options))
            .verifyErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && UPLOAD_FROM_FILE_MD5_NOT_SUPPORTED_MESSAGE.equals(throwable.getMessage()));
    }

    // ===========================================================================================
    // Exact 4MB boundary tests
    //
    // The cutoff between CRC64 header and structured message is exactly 4MB.
    // Uploads of exactly 4MB should use structured message (>= threshold), not CRC64 header.
    // ===========================================================================================

    private static final int EXACTLY_4MB = 4 * Constants.MB;

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadAtExactly4MBUsesStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(EXACTLY_4MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) EXACTLY_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(algorithm);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobSimpleUploadAtExactly4MBUsesStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(EXACTLY_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setRequestChecksumAlgorithm(algorithm);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
    }

    // ===========================================================================================
    // Progress reporting tests with content validation (structured message)
    //
    // ProgressListener receives the byte count of the wire payload (structured message), not raw content length.
    // Expected values match {@link StructuredMessageEncoder} with segment size
    // {@link StructuredMessageConstants#V1_DEFAULT_SEGMENT_CONTENT_LENGTH}, as in {@code StorageContentValidationPolicy}.
    // Only APIs that accept ParallelTransferOptions (and thus setProgressListener) are tested here.
    // The pre-encoded byte count is difficult if not impossible to compute without subclassing ProgressListener.
    // ===========================================================================================

    @Test
    public void uploadProgressReportsEncodedBytes() {
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));
        AtomicLong progressReported = new AtomicLong(0);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB)
                .setProgressListener(progressReported::set))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertEquals(expectedStructuredMessageEncodedLength(TEN_MB), progressReported.get(),
                "Progress should report encoded (structured message) byte count");
        }).verifyComplete();
    }

    @LiveOnly // Put Block URLs include random block IDs; not replayable with the test proxy.
    @Test
    public void uploadChunkedProgressReportsEncodedBytes() {
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));
        long blockSize = 2L * Constants.MB;
        AtomicLong progressReported = new AtomicLong(0);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(blockSize)
                .setMaxSingleUploadSizeLong(blockSize)
                .setProgressListener(progressReported::set))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertEquals(expectedStructuredMessageEncodedLengthChunked(TEN_MB, blockSize), progressReported.get(),
                "Progress should report encoded (structured message) byte count");
        }).verifyComplete();
    }

    @Test
    public void uploadFromFileProgressReportsEncodedBytes() throws IOException {
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        File tempFile = getRandomFile(TEN_MB);
        AtomicLong progressReported = new AtomicLong(0);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB)
                .setProgressListener(progressReported::set))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.uploadFromFileWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertEquals(expectedStructuredMessageEncodedLength(TEN_MB), progressReported.get(),
                "Progress should report encoded (structured message) byte count");
        }).verifyComplete();
    }

    @LiveOnly // Put Block URLs include random block IDs; not replayable with the test proxy.
    @Test
    public void uploadFromFileChunkedProgressReportsEncodedBytes() throws IOException {
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        File tempFile = getRandomFile(TEN_MB);
        long blockSize = 2L * Constants.MB;
        AtomicLong progressReported = new AtomicLong(0);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(blockSize)
                .setMaxSingleUploadSizeLong(blockSize)
                .setProgressListener(progressReported::set))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.uploadFromFileWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertEquals(expectedStructuredMessageEncodedLengthChunked(TEN_MB, blockSize), progressReported.get(),
                "Progress should report encoded (structured message) byte count");
        }).verifyComplete();
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
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

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
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

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
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

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
            = new BlockBlobSimpleUploadOptions(data).setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

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
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

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
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        client.uploadPagesWithResponse(options).block();

        byte[] downloaded = blobClient.downloadContent().block().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded data (page blob upload pages)");
    }
}
