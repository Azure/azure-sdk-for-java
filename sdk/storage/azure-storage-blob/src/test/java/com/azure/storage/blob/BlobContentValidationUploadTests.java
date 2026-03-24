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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
     * Single-part upload under 4MB: content validation uses CRC64 header only (no structured message).
     */
    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadWithCrc64Header(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(algorithm);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    /**
     * Single-part upload >= 4MB: content validation uses structured message.
     */
    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadWithStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(algorithm);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    /**
     * Multi-part (chunked) upload; content validation uses structured message on each stage block.
     */
    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadChunkedWithStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);
        long blockSize = 2 * (long) Constants.MB;

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(algorithm);

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

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobSimpleUploadWithCrc64Header(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setRequestChecksumAlgorithm(algorithm);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobSimpleUploadWithStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setRequestChecksumAlgorithm(algorithm);

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

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void stageBlockWithCrc64Header(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options
            = new BlockBlobStageBlockOptions(getBlockID(), data).setRequestChecksumAlgorithm(algorithm);

        client.stageBlockWithResponse(options, null, Context.NONE);
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void stageBlockWithStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options
            = new BlockBlobStageBlockOptions(getBlockID(), data).setRequestChecksumAlgorithm(algorithm);

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

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void appendBlockWithCrc64Header(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        InputStream data = new ByteArrayInputStream(randomData);

        AppendBlobAppendBlockOptions options
            = new AppendBlobAppendBlockOptions(data, UNDER_4MB).setRequestChecksumAlgorithm(algorithm);

        assertNotNull(client.appendBlockWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void appendBlockWithStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        AppendBlobAppendBlockOptions options
            = new AppendBlobAppendBlockOptions(data, TEN_MB).setRequestChecksumAlgorithm(algorithm);

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

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadPagesWithCrc64Header(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(UNDER_4MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);
        InputStream data = new ByteArrayInputStream(randomData);

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1), data)
                .setRequestChecksumAlgorithm(algorithm);

        assertNotNull(client.uploadPagesWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadPagesWithStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(FOUR_MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);
        InputStream data = new ByteArrayInputStream(randomData);

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1), data)
                .setRequestChecksumAlgorithm(algorithm);

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

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileWithCrc64Header(StorageChecksumAlgorithm algorithm) throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(UNDER_4MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestChecksumAlgorithm(algorithm);

        assertNotNull(client.uploadFromFileWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileWithStructuredMessage(StorageChecksumAlgorithm algorithm) throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(TEN_MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestChecksumAlgorithm(algorithm);

        assertNotNull(client.uploadFromFileWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileChunkedWithStructuredMessage(StorageChecksumAlgorithm algorithm) throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(TEN_MB);
        long blockSize = 2 * (long) Constants.MB;

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestChecksumAlgorithm(algorithm);

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

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void appendBlobOutputStreamWithCrc64Header(StorageChecksumAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(UNDER_4MB);

        try (BlobOutputStream os
            = client.getBlobOutputStream(new AppendBlobOutputStreamOptions().setRequestChecksumAlgorithm(algorithm))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void appendBlobOutputStreamWithStructuredMessage(StorageChecksumAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(TEN_MB);

        try (BlobOutputStream os
            = client.getBlobOutputStream(new AppendBlobOutputStreamOptions().setRequestChecksumAlgorithm(algorithm))) {
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

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobOutputStreamWithCrc64Header(StorageChecksumAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestChecksumAlgorithm(algorithm))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobOutputStreamWithStructuredMessage(StorageChecksumAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestChecksumAlgorithm(algorithm))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobOutputStreamChunkedWithStructuredMessage(StorageChecksumAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        long blockSize = 2 * (long) Constants.MB;

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestChecksumAlgorithm(algorithm))) {
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

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void pageBlobOutputStreamWithCrc64Header(StorageChecksumAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(UNDER_4MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);

        try (BlobOutputStream os = client.getBlobOutputStream(
            new PageBlobOutputStreamOptions(new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1))
                .setRequestChecksumAlgorithm(algorithm))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void pageBlobOutputStreamWithStructuredMessage(StorageChecksumAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(FOUR_MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);

        try (BlobOutputStream os = client.getBlobOutputStream(
            new PageBlobOutputStreamOptions(new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1))
                .setRequestChecksumAlgorithm(algorithm))) {
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

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void seekableByteChannelWriteWithCrc64Header(StorageChecksumAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);

        try (java.nio.channels.SeekableByteChannel channel = client.openSeekableByteChannelWrite(
            new BlockBlobSeekableByteChannelWriteOptions(BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE)
                .setRequestChecksumAlgorithm(algorithm))) {
            channel.write(ByteBuffer.wrap(randomData));
        }

        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void seekableByteChannelWriteWithStructuredMessage(StorageChecksumAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);

        try (java.nio.channels.SeekableByteChannel channel = client.openSeekableByteChannelWrite(
            new BlockBlobSeekableByteChannelWriteOptions(BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE)
                .setRequestChecksumAlgorithm(algorithm))) {
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
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(EXACTLY_4MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) EXACTLY_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(algorithm);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobSimpleUploadAtExactly4MBUsesStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(EXACTLY_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setRequestChecksumAlgorithm(algorithm);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = StorageChecksumAlgorithm.class, names = { "CRC64", "AUTO" })
    public void stageBlockAtExactly4MBUsesStructuredMessage(StorageChecksumAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(EXACTLY_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options
            = new BlockBlobStageBlockOptions(getBlockID(), data).setRequestChecksumAlgorithm(algorithm);

        client.stageBlockWithResponse(options, null, Context.NONE);
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
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
        assertEquals(expectedStructuredMessageEncodedLength(TEN_MB), progressReported.get(),
            "Progress should report encoded (structured message) byte count");
    }

    @Test
    public void uploadChunkedProgressReportsEncodedBytes() {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);
        long blockSize = 2L * Constants.MB;
        AtomicLong progressReported = new AtomicLong(0);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(blockSize)
                .setMaxSingleUploadSizeLong(blockSize)
                .setProgressListener(progressReported::set))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertEquals(expectedStructuredMessageEncodedLengthChunked(TEN_MB, blockSize), progressReported.get(),
            "Progress should report encoded (structured message) byte count");
    }

    @Test
    public void uploadFromFileProgressReportsEncodedBytes() throws IOException {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        File tempFile = getRandomFile(TEN_MB);
        AtomicLong progressReported = new AtomicLong(0);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB)
                .setProgressListener(progressReported::set))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadFromFileWithResponse(options, null, Context.NONE).getValue().getETag());
        assertEquals(expectedStructuredMessageEncodedLength(TEN_MB), progressReported.get(),
            "Progress should report encoded (structured message) byte count");
    }

    @Test
    public void uploadFromFileChunkedProgressReportsEncodedBytes() throws IOException {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        File tempFile = getRandomFile(TEN_MB);
        long blockSize = 2L * Constants.MB;
        AtomicLong progressReported = new AtomicLong(0);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(blockSize)
                .setMaxSingleUploadSizeLong(blockSize)
                .setProgressListener(progressReported::set))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        assertNotNull(client.uploadFromFileWithResponse(options, null, Context.NONE).getValue().getETag());
        assertEquals(expectedStructuredMessageEncodedLengthChunked(TEN_MB, blockSize), progressReported.get(),
            "Progress should report encoded (structured message) byte count");
    }

    @Test
    public void blockBlobOutputStreamProgressReportsEncodedBytes() throws Exception {
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

        assertEquals(expectedStructuredMessageEncodedLength(TEN_MB), progressReported.get(),
            "Progress should report encoded (structured message) byte count");
    }

    @Test
    public void blockBlobOutputStreamChunkedProgressReportsEncodedBytes() throws Exception {
        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        long blockSize = 2L * Constants.MB;
        AtomicLong progressReported = new AtomicLong(0);

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(blockSize)
                .setMaxSingleUploadSizeLong(blockSize)
                .setProgressListener(progressReported::set))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
            os.write(randomData);
        }

        assertEquals(expectedStructuredMessageEncodedLengthChunked(TEN_MB, blockSize), progressReported.get(),
            "Progress should report encoded (structured message) byte count");
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
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        client.uploadWithResponse(options, null, Context.NONE);

        byte[] downloaded = client.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded data (CRC64 header path)");
    }

    @Test
    public void uploadWithStructuredMessageRoundTripDataIntegrity() {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        client.uploadWithResponse(options, null, Context.NONE);

        byte[] downloaded = client.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded data (structured message path)");
    }

    @Test
    public void uploadChunkedWithStructuredMessageRoundTripDataIntegrity() {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);
        long blockSize = 2 * (long) Constants.MB;

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestConditions(new BlobRequestConditions())
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        client.uploadWithResponse(options, null, Context.NONE);

        byte[] downloaded = client.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match uploaded data (chunked structured message path)");
    }

    @Test
    public void blockBlobSimpleUploadRoundTripDataIntegrity() {
        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        client.uploadWithResponse(options, null, Context.NONE);

        byte[] downloaded = blobClient.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match uploaded data (block blob simple upload)");
    }

    @Test
    public void appendBlockRoundTripDataIntegrity() {
        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        AppendBlobAppendBlockOptions options = new AppendBlobAppendBlockOptions(data, TEN_MB)
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        client.appendBlockWithResponse(options, null, Context.NONE);

        byte[] downloaded = blobClient.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded data (append block)");
    }

    @Test
    public void uploadPagesRoundTripDataIntegrity() {
        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(FOUR_MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);
        InputStream data = new ByteArrayInputStream(randomData);

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1), data)
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        client.uploadPagesWithResponse(options, null, Context.NONE);

        byte[] downloaded = blobClient.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded data (page blob upload pages)");
    }

    @Test
    public void uploadFromFileRoundTripDataIntegrity() throws IOException {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(TEN_MB);
        File tempFile = File.createTempFile("blob-cv-roundtrip", ".bin");
        tempFile.deleteOnExit();
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
            fos.write(randomData);
        }

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64);

        client.uploadFromFileWithResponse(options, null, Context.NONE);

        byte[] downloaded = client.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded file data");
    }
}
