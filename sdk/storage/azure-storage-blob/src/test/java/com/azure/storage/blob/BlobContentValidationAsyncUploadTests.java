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
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
     * Single-part upload; 4MB with CRC64: content validation uses CRC64 header only (no structured message).
     */
    @Test
    public void uploadWithCrc64Header() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    /**
     * Single-part upload; 10MB (>= 4MB): content validation uses structured message.
     */
    @Test
    public void uploadWithStructuredMessage() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
    }

    /**
     * Multi-part (chunked) upload; content validation uses structured message on each stage block.
     */
    @Test
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

    @Test
    public void blockBlobSimpleUploadWithCrc64Header() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.uploadWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    @Test
    public void blockBlobSimpleUploadWithStructuredMessage() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

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

    @Test
    public void stageBlockWithCrc64Header() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(getBlockID(), data)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.stageBlockWithResponse(options)).assertNext(response -> {
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    @Test
    public void stageBlockWithStructuredMessage() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        BlockBlobAsyncClient client = blobClient.getBlockBlobAsyncClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(getBlockID(), data)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

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

    @Test
    public void appendBlockWithCrc64Header() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        AppendBlobAsyncClient client = blobClient.getAppendBlobAsyncClient();
        client.create().block();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        AppendBlobAppendBlockOptions options = new AppendBlobAppendBlockOptions(data, UNDER_4MB)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.appendBlockWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    @Test
    public void appendBlockWithStructuredMessage() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        AppendBlobAsyncClient client = blobClient.getAppendBlobAsyncClient();
        client.create().block();

        byte[] randomData = getRandomByteArray(TEN_MB);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        AppendBlobAppendBlockOptions options = new AppendBlobAppendBlockOptions(data, TEN_MB)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

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

    @Test
    public void uploadPagesWithCrc64Header() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        PageBlobAsyncClient client = blobClient.getPageBlobAsyncClient();
        client.create(UNDER_4MB_PAGE_ALIGNED).block();

        byte[] randomData = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1), data)
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.uploadPagesWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    @Test
    public void uploadPagesWithStructuredMessage() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient blobClient = createBlobAsyncClientWithRequestSniffer(recorded);
        PageBlobAsyncClient client = blobClient.getPageBlobAsyncClient();
        client.create(FOUR_MB_PAGE_ALIGNED).block();

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);
        Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap(randomData));

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1), data)
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

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

    @Test
    public void uploadFromFileWithCrc64Header() throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(UNDER_4MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.uploadFromFileWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyCrc64Headers(recorded));
        }).verifyComplete();
    }

    @Test
    public void uploadFromFileWithStructuredMessage() throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(TEN_MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        StepVerifier.create(client.uploadFromFileWithResponse(options)).assertNext(response -> {
            assertNotNull(response.getValue().getETag());
            assertTrue(hasOnlyStructuredMessageHeaders(recorded));
        }).verifyComplete();
    }

    @Test
    public void uploadFromFileChunkedWithStructuredMessage() throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobAsyncClient client = createBlobAsyncClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(TEN_MB);
        long blockSize = 2 * (long) Constants.MB;

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

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
}
