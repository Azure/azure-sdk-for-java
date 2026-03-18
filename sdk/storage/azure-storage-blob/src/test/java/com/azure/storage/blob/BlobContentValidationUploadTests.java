// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.AppendBlobAppendBlockOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.AppendBlobOutputStreamOptions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.options.BlockBlobSeekableByteChannelWriteOptions;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockOptions;
import com.azure.storage.blob.options.PageBlobOutputStreamOptions;
import com.azure.storage.blob.options.PageBlobUploadPagesOptions;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.common.StorageChecksumAlgorithm;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests content validation (CRC64 / structured message) for upload operations using sync clients.
 * Upload types that have no async counterpart (OutputStream, SeekableByteChannel) are tested only here.
 * Async counterparts of the same operations are in {@link BlobContentValidationAsyncUploadTests}.
 */
public class BlobContentValidationUploadTests extends BlobTestBase {
    private static final int TEN_MB = 10 * Constants.MB;
    /* Single-part uploads with length < 4MB use CRC64 header; >= 4MB use structured message. */
    private static final int UNDER_4MB = 2 * Constants.MB;

    private static final String MD5_AND_CRC64_EXCLUSIVE_MESSAGE
        = "Only one form of transactional content validation may be used.";

    private static final String UPLOAD_FROM_FILE_MD5_NOT_SUPPORTED_MESSAGE
        = "StorageChecksumAlgorithm.MD5 is not supported for uploadFromFile. Use CRC64 or AUTO instead.";

    // ===========================================================================================
    // BlobClient.uploadWithResponse
    // ===========================================================================================

    /**
     * Single-part upload; 4MB with CRC64: content validation uses CRC64 header only (no structured message).
     */
    @Test
    public void uploadWithCrc64Header() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    /**
     * Single-part upload; 10MB (>= 4MB): content validation uses structured message.
     */
    @Test
    public void uploadWithStructuredMessage() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    /**
     * Multi-part (chunked) upload; content validation uses structured message on each stage block.
     */
    @Test
    public void uploadChunkedWithStructuredMessage() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);
        long blockSize = 2 * (long) Constants.MB;

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @Test
    public void uploadWithoutContentValidation() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    /**
     * Blob parallel upload rejects using both computeMd5 (SDK-computed MD5) and CRC64 (requestChecksumAlgorithm) at once.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void uploadWithComputeMd5AndCrc64Throws() {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setComputeMd5(true)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> client.uploadWithResponse(options, null, Context.NONE));
        assertTrue(ex.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
    }

    // ===========================================================================================
    // BlockBlobClient.uploadWithResponse (BlockBlobSimpleUpload / Put Blob) tests
    // ===========================================================================================

    @Test
    public void blockBlobSimpleUploadWithCrc64Header() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @Test
    public void blockBlobSimpleUploadWithStructuredMessage() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @Test
    public void blockBlobSimpleUploadWithNoContentValidation() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    /**
     * Block blob simple upload rejects using both MD5 (contentMd5) and CRC64 (requestChecksumAlgorithm) at once.
     */
    @Test
    public void blockBlobSimpleUploadWithMd5AndCrc64Throws() throws NoSuchAlgorithmException {
        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(randomData);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options = new BlockBlobSimpleUploadOptions(data).setContentMd5(md5)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> client.uploadWithResponse(options, null, Context.NONE));
        assertTrue(ex.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
    }

    // ===========================================================================================
    // BlockBlobClient.stageBlockWithResponse (Put Block) tests
    // ===========================================================================================

    @Test
    public void stageBlockWithCrc64Header() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(getBlockID(), data)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        client.stageBlockWithResponse(options, null, Context.NONE);
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @Test
    public void stageBlockWithStructuredMessage() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(getBlockID(), data)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        client.stageBlockWithResponse(options, null, Context.NONE);
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @Test
    public void stageBlockWithNoContentValidation() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(getBlockID(), data)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        client.stageBlockWithResponse(options, null, Context.NONE);
        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    /**
     * Stage block rejects using both MD5 (contentMd5) and CRC64 (requestChecksumAlgorithm) at once.
     */
    @Test
    public void stageBlockWithMd5AndCrc64Throws() throws NoSuchAlgorithmException {
        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(randomData);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(getBlockID(), data).setContentMd5(md5)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> client.stageBlockWithResponse(options, null, Context.NONE));
        assertTrue(ex.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
    }

    // ===========================================================================================
    // AppendBlobClient.appendBlockWithResponse (Append Block) tests
    // ===========================================================================================

    @Test
    public void appendBlockWithCrc64Header() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        InputStream data = new ByteArrayInputStream(randomData);

        AppendBlobAppendBlockOptions options = new AppendBlobAppendBlockOptions(data, UNDER_4MB)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.appendBlockWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @Test
    public void appendBlockWithStructuredMessage() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        AppendBlobAppendBlockOptions options = new AppendBlobAppendBlockOptions(data, TEN_MB)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.appendBlockWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @Test
    public void appendBlockWithNoContentValidation() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        AppendBlobAppendBlockOptions options
            = new AppendBlobAppendBlockOptions(data, TEN_MB).setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        assertNotNull(client.appendBlockWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    /**
     * Append block rejects using both MD5 (contentMd5) and CRC64 (requestChecksumAlgorithm) at once.
     */
    @Test
    public void appendBlockWithMd5AndCrc64Throws() throws NoSuchAlgorithmException {
        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(randomData);
        InputStream data = new ByteArrayInputStream(randomData);

        AppendBlobAppendBlockOptions options = new AppendBlobAppendBlockOptions(data, UNDER_4MB).setContentMd5(md5)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> client.appendBlockWithResponse(options, null, Context.NONE));
        assertTrue(ex.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
    }

    // ===========================================================================================
    // PageBlobClient.uploadPagesWithResponse (Put Page) tests
    // ===========================================================================================

    private static final int PAGE_BYTES = PageBlobClient.PAGE_BYTES;
    private static final int UNDER_4MB_PAGE_ALIGNED = (UNDER_4MB / PAGE_BYTES) * PAGE_BYTES;
    private static final int FOUR_MB_PAGE_ALIGNED = (4 * Constants.MB / PAGE_BYTES) * PAGE_BYTES;

    @Test
    public void uploadPagesWithCrc64Header() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(UNDER_4MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);
        InputStream data = new ByteArrayInputStream(randomData);

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1), data)
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadPagesWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @Test
    public void uploadPagesWithStructuredMessage() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(FOUR_MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);
        InputStream data = new ByteArrayInputStream(randomData);

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1), data)
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadPagesWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @Test
    public void uploadPagesWithNoContentValidation() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(FOUR_MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);
        InputStream data = new ByteArrayInputStream(randomData);

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1), data)
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        assertNotNull(client.uploadPagesWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    /**
     * Upload pages rejects using both MD5 (contentMd5) and CRC64 (requestChecksumAlgorithm) at once.
     */
    @Test
    public void uploadPagesWithMd5AndCrc64Throws() throws NoSuchAlgorithmException {
        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(UNDER_4MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(randomData);
        InputStream data = new ByteArrayInputStream(randomData);

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1), data)
                .setContentMd5(md5)
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> client.uploadPagesWithResponse(options, null, Context.NONE));
        assertTrue(ex.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
    }

    // ===========================================================================================
    // BlobClient.uploadFromFileWithResponse tests
    // ===========================================================================================

    @Test
    public void uploadFromFileWithCrc64Header() throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(UNDER_4MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadFromFileWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @Test
    public void uploadFromFileWithStructuredMessage() throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(TEN_MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadFromFileWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @Test
    public void uploadFromFileChunkedWithStructuredMessage() throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(TEN_MB);
        long blockSize = 2 * (long) Constants.MB;

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadFromFileWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @Test
    public void uploadFromFileWithNoContentValidation() throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(TEN_MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE);

        assertNotNull(client.uploadFromFileWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    /**
     * Upload from file rejects requestChecksumAlgorithm MD5 because the SDK does not support computing MD5 for file uploads.
     */
    @Test
    public void uploadFromFileWithRequestChecksumAlgorithmMd5Throws() throws IOException {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        File tempFile = getRandomFile(UNDER_4MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.MD5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> client.uploadFromFileWithResponse(options, null, Context.NONE));
        assertTrue(ex.getMessage().contains(UPLOAD_FROM_FILE_MD5_NOT_SUPPORTED_MESSAGE));
    }

    // ===========================================================================================
    // Sync BlobOutputStream tests (getBlobOutputStream)
    // ===========================================================================================

    // --- AppendBlobClient.getBlobOutputStream ---

    @Test
    public void appendBlobOutputStreamWithCrc64Header() throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(UNDER_4MB);

        try (BlobOutputStream os = client.getBlobOutputStream(
            new AppendBlobOutputStreamOptions().setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @Test
    public void appendBlobOutputStreamWithStructuredMessage() throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(TEN_MB);

        try (BlobOutputStream os = client.getBlobOutputStream(
            new AppendBlobOutputStreamOptions().setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @Test
    public void appendBlobOutputStreamWithNoContentValidation() throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(TEN_MB);

        try (BlobOutputStream os = client.getBlobOutputStream(
            new AppendBlobOutputStreamOptions().setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE))) {
            os.write(randomData);
        }

        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    // --- BlockBlobClient.getBlobOutputStream ---

    @Test
    public void blockBlobOutputStreamWithCrc64Header() throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @Test
    public void blockBlobOutputStreamWithStructuredMessage() throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @Test
    public void blockBlobOutputStreamChunkedWithStructuredMessage() throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        long blockSize = 2 * (long) Constants.MB;

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @Test
    public void blockBlobOutputStreamWithNoContentValidation() throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE))) {
            os.write(randomData);
        }

        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    // --- PageBlobClient.getBlobOutputStream ---

    @Test
    public void pageBlobOutputStreamWithCrc64Header() throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(UNDER_4MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);

        try (BlobOutputStream os = client.getBlobOutputStream(
            new PageBlobOutputStreamOptions(new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1))
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @Test
    public void pageBlobOutputStreamWithStructuredMessage() throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(FOUR_MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);

        try (BlobOutputStream os = client.getBlobOutputStream(
            new PageBlobOutputStreamOptions(new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1))
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @Test
    public void pageBlobOutputStreamWithNoContentValidation() throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(FOUR_MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);

        try (BlobOutputStream os = client.getBlobOutputStream(
            new PageBlobOutputStreamOptions(new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1))
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE))) {
            os.write(randomData);
        }

        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    // ===========================================================================================
    // BlockBlobClient.openSeekableByteChannelWrite tests
    // ===========================================================================================

    @Test
    public void seekableByteChannelWriteWithCrc64Header() throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);

        try (java.nio.channels.SeekableByteChannel channel = client.openSeekableByteChannelWrite(
            new BlockBlobSeekableByteChannelWriteOptions(BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE)
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
            channel.write(ByteBuffer.wrap(randomData));
        }

        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @Test
    public void seekableByteChannelWriteWithStructuredMessage() throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);

        try (java.nio.channels.SeekableByteChannel channel = client.openSeekableByteChannelWrite(
            new BlockBlobSeekableByteChannelWriteOptions(BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE)
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
            channel.write(ByteBuffer.wrap(randomData));
        }

        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @Test
    public void seekableByteChannelWriteWithNoContentValidation() throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);

        try (java.nio.channels.SeekableByteChannel channel = client.openSeekableByteChannelWrite(
            new BlockBlobSeekableByteChannelWriteOptions(BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE)
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE))) {
            channel.write(ByteBuffer.wrap(randomData));
        }

        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    // ===========================================================================================
    // Progress reporting tests with content validation (structured message)
    //
    // These tests verify that the ProgressListener reports the original (pre-encoded) byte count,
    // not the encoded byte count which includes structured message overhead.
    // Only APIs that accept ParallelTransferOptions (and thus setProgressListener) are tested here.
    // ===========================================================================================

    @Test
    public void uploadProgressReportsPreEncodedBytes() {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);
        AtomicLong progressReported = new AtomicLong(0);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB)
                .setProgressListener(progressReported::set))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertEquals((long) TEN_MB, progressReported.get(),
            "Progress should report pre-encoded byte count, not encoded byte count");
    }

    @Test
    public void uploadChunkedProgressReportsPreEncodedBytes() {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);
        long blockSize = 2 * (long) Constants.MB;
        AtomicLong progressReported = new AtomicLong(0);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(blockSize)
                .setMaxSingleUploadSizeLong(blockSize)
                .setProgressListener(progressReported::set))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertEquals((long) TEN_MB, progressReported.get(),
            "Progress should report pre-encoded byte count, not encoded byte count");
    }

    @Test
    public void uploadFromFileProgressReportsPreEncodedBytes() throws IOException {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        File tempFile = getRandomFile(TEN_MB);
        AtomicLong progressReported = new AtomicLong(0);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB)
                .setProgressListener(progressReported::set))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadFromFileWithResponse(options, null, Context.NONE).getValue().getETag());
        assertEquals((long) TEN_MB, progressReported.get(),
            "Progress should report pre-encoded byte count, not encoded byte count");
    }

    @Test
    public void uploadFromFileChunkedProgressReportsPreEncodedBytes() throws IOException {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        File tempFile = getRandomFile(TEN_MB);
        long blockSize = 2 * (long) Constants.MB;
        AtomicLong progressReported = new AtomicLong(0);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(blockSize)
                .setMaxSingleUploadSizeLong(blockSize)
                .setProgressListener(progressReported::set))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadFromFileWithResponse(options, null, Context.NONE).getValue().getETag());
        assertEquals((long) TEN_MB, progressReported.get(),
            "Progress should report pre-encoded byte count, not encoded byte count");
    }

    @Test
    public void blockBlobOutputStreamProgressReportsPreEncodedBytes() throws Exception {
        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        AtomicLong progressReported = new AtomicLong(0);

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB)
                .setProgressListener(progressReported::set))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
            os.write(randomData);
        }

        assertEquals((long) TEN_MB, progressReported.get(),
            "Progress should report pre-encoded byte count, not encoded byte count");
    }

    @Test
    public void blockBlobOutputStreamChunkedProgressReportsPreEncodedBytes() throws Exception {
        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        long blockSize = 2 * (long) Constants.MB;
        AtomicLong progressReported = new AtomicLong(0);

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(blockSize)
                .setMaxSingleUploadSizeLong(blockSize)
                .setProgressListener(progressReported::set))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
            os.write(randomData);
        }

        assertEquals((long) TEN_MB, progressReported.get(),
            "Progress should report pre-encoded byte count, not encoded byte count");
    }
}
