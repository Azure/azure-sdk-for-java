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
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.implementation.contentvalidation.ContentValidationModeResolver;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
    /* single-shot uploads with length < 4MB use CRC64 header; >= 4MB use structured message. */
    private static final int UNDER_4MB = 2 * Constants.MB;

    /** Chunked uploadFromFile tests spanning 500 MiB–1 GiB (must use parallel block upload; max single Put Blob is lower). */
    private static final long LARGE_UPLOAD_MIN_BYTES = 500L * Constants.MB;
    private static final long LARGE_UPLOAD_MAX_BYTES = 1L * Constants.GB;
    private static final long LARGE_UPLOAD_BLOCK_SIZE_BYTES = 8L * Constants.MB;
    private static final int LARGE_UPLOAD_MAX_CONCURRENCY = 8;

    private static final String MD5_AND_CRC64_EXCLUSIVE_MESSAGE
        = "Only one form of transactional content validation may be used.";

    private static final String UPLOAD_FROM_FILE_MD5_NOT_SUPPORTED_MESSAGE
        = "ContentValidationAlgorithm.MD5 is not supported for uploadFromFile. Use CRC64 or AUTO instead.";

    // ===========================================================================================
    // BlobClient.uploadWithResponse
    // ===========================================================================================

    /**
     * Single-shot upload under 4MB: content validation uses CRC64 header only (no structured message).
     */
    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadWithCrc64Header(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(algorithm);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    /**
     * Single-shot upload >= 4MB: content validation uses structured message.
     */
    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadWithStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(algorithm);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    /**
     * Multi-shot (chunked) upload; content validation uses structured message on each stage block.
     */
    @LiveOnly // Put Block URLs include random block IDs; not replayable with the test proxy.
    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadChunkedWithStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);
        long blockSize = 2 * (long) Constants.MB;

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(algorithm);

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
            .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    /**
     * Blob parallel upload rejects using both computeMd5 (SDK-computed MD5) and CRC64 (transfer validation checksum algorithm) at once.
     */
    @Test
    public void uploadWithComputeMd5AndCrc64Throws() {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setComputeMd5(true)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> client.uploadWithResponse(options, null, Context.NONE));
        assertTrue(ex.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
    }

    // ===========================================================================================
    // BlockBlobClient.uploadWithResponse (BlockBlobSimpleUpload / Put Blob) tests
    // ===========================================================================================

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobSimpleUploadWithCrc64Header(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setContentValidationAlgorithm(algorithm);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobSimpleUploadWithStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setContentValidationAlgorithm(algorithm);

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
            = new BlockBlobSimpleUploadOptions(data).setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    /**
     * Block blob simple upload rejects using both MD5 (contentMd5) and CRC64 (transfer validation checksum algorithm) at once.
     */
    @Test
    public void blockBlobSimpleUploadWithMd5AndCrc64Throws() throws NoSuchAlgorithmException {
        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(randomData);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options = new BlockBlobSimpleUploadOptions(data).setContentMd5(md5)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> client.uploadWithResponse(options, null, Context.NONE));
        assertTrue(ex.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
    }

    // ===========================================================================================
    // BlockBlobClient.stageBlockWithResponse (Put Block) tests
    // ===========================================================================================

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void stageBlockWithCrc64Header(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options
            = new BlockBlobStageBlockOptions(getBlockID(), data).setContentValidationAlgorithm(algorithm);

        client.stageBlockWithResponse(options, null, Context.NONE);
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void stageBlockWithStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options
            = new BlockBlobStageBlockOptions(getBlockID(), data).setContentValidationAlgorithm(algorithm);

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
            .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        client.stageBlockWithResponse(options, null, Context.NONE);
        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    /**
     * Stage block rejects using both MD5 (contentMd5) and CRC64 (transfer validation checksum algorithm) at once.
     */
    @Test
    public void stageBlockWithMd5AndCrc64Throws() throws NoSuchAlgorithmException {
        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(randomData);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(getBlockID(), data).setContentMd5(md5)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> client.stageBlockWithResponse(options, null, Context.NONE));
        assertTrue(ex.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
    }

    // ===========================================================================================
    // AppendBlobClient.appendBlockWithResponse (Append Block) tests
    // ===========================================================================================

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void appendBlockWithCrc64Header(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(UNDER_4MB);
        InputStream data = new ByteArrayInputStream(randomData);

        AppendBlobAppendBlockOptions options
            = new AppendBlobAppendBlockOptions(data, UNDER_4MB).setContentValidationAlgorithm(algorithm);

        assertNotNull(client.appendBlockWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void appendBlockWithStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        AppendBlobAppendBlockOptions options
            = new AppendBlobAppendBlockOptions(data, TEN_MB).setContentValidationAlgorithm(algorithm);

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

        AppendBlobAppendBlockOptions options = new AppendBlobAppendBlockOptions(data, TEN_MB)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        assertNotNull(client.appendBlockWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    /**
     * Append block rejects using both MD5 (contentMd5) and CRC64 (transfer validation checksum algorithm) at once.
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
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

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
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadPagesWithCrc64Header(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(UNDER_4MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);
        InputStream data = new ByteArrayInputStream(randomData);

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1), data)
                .setContentValidationAlgorithm(algorithm);

        assertNotNull(client.uploadPagesWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadPagesWithStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(FOUR_MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);
        InputStream data = new ByteArrayInputStream(randomData);

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1), data)
                .setContentValidationAlgorithm(algorithm);

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
                .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        assertNotNull(client.uploadPagesWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    /**
     * Upload pages rejects using both MD5 (contentMd5) and CRC64 (transfer validation checksum algorithm) at once.
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
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> client.uploadPagesWithResponse(options, null, Context.NONE));
        assertTrue(ex.getMessage().contains(MD5_AND_CRC64_EXCLUSIVE_MESSAGE));
    }

    // ===========================================================================================
    // BlobClient.uploadFromFileWithResponse tests
    // ===========================================================================================

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileWithCrc64Header(ContentValidationAlgorithm algorithm) throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(UNDER_4MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setContentValidationAlgorithm(algorithm);

        assertNotNull(client.uploadFromFileWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileWithStructuredMessage(ContentValidationAlgorithm algorithm) throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(TEN_MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setContentValidationAlgorithm(algorithm);

        assertNotNull(client.uploadFromFileWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @LiveOnly // Put Block URLs include random block IDs; not replayable with the test proxy.
    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileChunkedWithStructuredMessage(ContentValidationAlgorithm algorithm) throws IOException {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        File tempFile = getRandomFile(TEN_MB);
        long blockSize = 2 * (long) Constants.MB;

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setContentValidationAlgorithm(algorithm);

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
            .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        assertNotNull(client.uploadFromFileWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    // ===========================================================================================
    // Sync BlobOutputStream tests (getBlobOutputStream)
    // ===========================================================================================

    // --- AppendBlobClient.getBlobOutputStream ---

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void appendBlobOutputStreamWithCrc64Header(ContentValidationAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(UNDER_4MB);

        try (BlobOutputStream os = client
            .getBlobOutputStream(new AppendBlobOutputStreamOptions().setContentValidationAlgorithm(algorithm))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void appendBlobOutputStreamWithStructuredMessage(ContentValidationAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(TEN_MB);

        try (BlobOutputStream os = client
            .getBlobOutputStream(new AppendBlobOutputStreamOptions().setContentValidationAlgorithm(algorithm))) {
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
            new AppendBlobOutputStreamOptions().setContentValidationAlgorithm(ContentValidationAlgorithm.NONE))) {
            os.write(randomData);
        }

        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    // --- BlockBlobClient.getBlobOutputStream ---

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobOutputStreamWithCrc64Header(ContentValidationAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) UNDER_4MB))
            .setContentValidationAlgorithm(algorithm))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobOutputStreamWithStructuredMessage(ContentValidationAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB))
            .setContentValidationAlgorithm(algorithm))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @LiveOnly // Put Block URLs include random block IDs; not replayable with the test proxy.
    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobOutputStreamChunkedWithStructuredMessage(ContentValidationAlgorithm algorithm)
        throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);
        long blockSize = 2 * (long) Constants.MB;

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setContentValidationAlgorithm(algorithm))) {
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
            .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE))) {
            os.write(randomData);
        }

        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    // --- PageBlobClient.getBlobOutputStream ---

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void pageBlobOutputStreamWithCrc64Header(ContentValidationAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(UNDER_4MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);

        try (BlobOutputStream os = client.getBlobOutputStream(
            new PageBlobOutputStreamOptions(new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1))
                .setContentValidationAlgorithm(algorithm))) {
            os.write(randomData);
        }

        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void pageBlobOutputStreamWithStructuredMessage(ContentValidationAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(FOUR_MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);

        try (BlobOutputStream os = client.getBlobOutputStream(
            new PageBlobOutputStreamOptions(new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1))
                .setContentValidationAlgorithm(algorithm))) {
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
                .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE))) {
            os.write(randomData);
        }

        assertTrue(hasNoContentValidationHeaders(recorded));
    }

    // ===========================================================================================
    // BlockBlobClient.openSeekableByteChannelWrite tests
    // ===========================================================================================

    @LiveOnly // Seekable channel staging uses Put Block with random block IDs; not replayable with the test proxy.
    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void seekableByteChannelWriteWithCrc64Header(ContentValidationAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(UNDER_4MB);

        try (java.nio.channels.SeekableByteChannel channel = client.openSeekableByteChannelWrite(
            new BlockBlobSeekableByteChannelWriteOptions(BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE)
                .setContentValidationAlgorithm(algorithm))) {
            channel.write(ByteBuffer.wrap(randomData));
        }

        assertTrue(hasOnlyCrc64Headers(recorded));
    }

    @LiveOnly // Put Block URLs include random block IDs; not replayable with the test proxy.
    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void seekableByteChannelWriteWithStructuredMessage(ContentValidationAlgorithm algorithm) throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);

        try (java.nio.channels.SeekableByteChannel channel = client.openSeekableByteChannelWrite(
            new BlockBlobSeekableByteChannelWriteOptions(BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE)
                .setContentValidationAlgorithm(algorithm))) {
            channel.write(ByteBuffer.wrap(randomData));
        }

        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @LiveOnly // Put Block URLs include random block IDs; not replayable with the test proxy.
    @Test
    public void seekableByteChannelWriteWithNoContentValidation() throws Exception {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(TEN_MB);

        try (java.nio.channels.SeekableByteChannel channel = client.openSeekableByteChannelWrite(
            new BlockBlobSeekableByteChannelWriteOptions(BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE))) {
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
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadAtExactly4MBUsesStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithRequestSniffer(recorded);

        byte[] randomData = getRandomByteArray(EXACTLY_4MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) EXACTLY_4MB))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(algorithm);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobSimpleUploadAtExactly4MBUsesStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(EXACTLY_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setContentValidationAlgorithm(algorithm);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void stageBlockAtExactly4MBUsesStructuredMessage(ContentValidationAlgorithm algorithm) {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(EXACTLY_4MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options
            = new BlockBlobStageBlockOptions(getBlockID(), data).setContentValidationAlgorithm(algorithm);

        client.stageBlockWithResponse(options, null, Context.NONE);
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    // ===========================================================================================
    // Progress reporting (transfer validation must be NONE/null when a progress listener is set)
    // ===========================================================================================

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadWithProgressAndNonNoneContentValidationThrows(ContentValidationAlgorithm algorithm) {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        byte[] randomData = getRandomByteArray(TEN_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data).setParallelTransferOptions(
            new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB).setProgressListener(l -> {
            })).setRequestConditions(new BlobRequestConditions()).setContentValidationAlgorithm(algorithm);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> client.uploadWithResponse(options, null, Context.NONE));
        assertEquals(ContentValidationModeResolver.PROGRESS_CONFLICTS_TRANSFER_CONTENT_VALIDATION_MESSAGE,
            ex.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadFromFileWithProgressAndNonNoneContentValidationThrows(ContentValidationAlgorithm algorithm)
        throws IOException {
        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());

        File tempFile = getRandomFile(TEN_MB);

        BlobUploadFromFileOptions options
            = new BlobUploadFromFileOptions(tempFile.getAbsolutePath()).setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) TEN_MB).setProgressListener(l -> {
                })).setContentValidationAlgorithm(algorithm);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> client.uploadFromFileWithResponse(options, null, Context.NONE));
        assertEquals(ContentValidationModeResolver.PROGRESS_CONFLICTS_TRANSFER_CONTENT_VALIDATION_MESSAGE,
            ex.getMessage());
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
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

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
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadWithResponse(options, null, Context.NONE);

        byte[] downloaded = client.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded data (structured message path)");
    }

    @LiveOnly // Put Block URLs include random block IDs; not replayable with the test proxy.
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
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

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
            = new BlockBlobSimpleUploadOptions(data).setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

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
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

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
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

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
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadFromFileWithResponse(options, null, Context.NONE);

        byte[] downloaded = client.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded file data");
    }

    // ===========================================================================================
    // Randomized payload sizes (exercises CRC64 header vs structured message lengths across runs)
    // ===========================================================================================

    @Test
    public void uploadWithRandomSizeCrc64HeaderRoundTripDataIntegrity() {
        int size = randomIntFromNamer(Constants.KB, EXACTLY_4MB);

        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        byte[] randomData = getRandomByteArray(size);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) size))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadWithResponse(options, null, Context.NONE);

        byte[] downloaded = client.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match uploaded data (random size CRC64 header path, size=" + size + ")");
    }

    @LiveOnly // This test is too large for the test proxy.
    @Test
    public void uploadWithRandomSizeStructuredMessageRoundTripDataIntegrity() {
        int size = randomIntFromNamer(EXACTLY_4MB, 48 * Constants.MB + 1);

        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        byte[] randomData = getRandomByteArray(size);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) size))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadWithResponse(options, null, Context.NONE);

        byte[] downloaded = client.downloadContent().toBytes();
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

        BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        byte[] randomData = getRandomByteArray(totalSize);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadWithResponse(options, null, Context.NONE);

        byte[] downloaded = client.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match uploaded data (random chunked path, total=" + totalSize + ", block=" + blockSize
                + ")");
    }

    @LiveOnly // This test is too large for the test proxy.
    @Test
    public void blockBlobSimpleUploadRandomSizeRoundTripDataIntegrity() {
        int size = randomIntFromNamer(Constants.KB, 48 * Constants.MB + 1);

        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobClient client = blobClient.getBlockBlobClient();
        byte[] randomData = getRandomByteArray(size);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadWithResponse(options, null, Context.NONE);

        byte[] downloaded = blobClient.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match uploaded data (random block blob simple upload, size=" + size + ")");
    }

    @LiveOnly // This test is too large for the test proxy.
    @Test
    public void stageBlockRandomSizeRoundTripDataIntegrity() {
        int size = randomIntFromNamer(Constants.KB, 40 * Constants.MB + 1);

        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        BlockBlobClient client = blobClient.getBlockBlobClient();
        String blockId = getBlockID();
        byte[] randomData = getRandomByteArray(size);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(blockId, data)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.stageBlockWithResponse(options, null, Context.NONE);
        client.commitBlockList(Collections.singletonList(blockId));

        byte[] downloaded = blobClient.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded,
            "Downloaded data must match staged block (random size, size=" + size + ")");
    }

    @LiveOnly // This test is too large for the test proxy.
    @Test
    public void appendBlockRandomSizeRoundTripDataIntegrity() {
        int size = randomIntFromNamer(Constants.KB, 80 * Constants.MB + 1);

        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(size);
        InputStream data = new ByteArrayInputStream(randomData);

        AppendBlobAppendBlockOptions options = new AppendBlobAppendBlockOptions(data, size)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.appendBlockWithResponse(options, null, Context.NONE);

        byte[] downloaded = blobClient.downloadContent().toBytes();
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

        BlobClient blobClient = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(sizeBytes);

        byte[] randomData = getRandomByteArray(sizeBytes);
        InputStream data = new ByteArrayInputStream(randomData);

        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(sizeBytes - 1), data)
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadPagesWithResponse(options, null, Context.NONE);

        byte[] downloaded = blobClient.downloadContent().toBytes();
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
        File outFile = Files.createTempFile("blob-cv-large-dl", ".bin").toFile();
        outFile.deleteOnExit();

        try {
            BlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>());
            ParallelTransferOptions parallelTransferOptions
                = new ParallelTransferOptions().setBlockSizeLong(LARGE_UPLOAD_BLOCK_SIZE_BYTES)
                    .setMaxSingleUploadSizeLong(LARGE_UPLOAD_BLOCK_SIZE_BYTES)
                    .setMaxConcurrency(LARGE_UPLOAD_MAX_CONCURRENCY);

            BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(sourceFile.getAbsolutePath())
                .setParallelTransferOptions(parallelTransferOptions)
                .setContentValidationAlgorithm(algorithm);

            assertNotNull(client.uploadFromFileWithResponse(options, null, Context.NONE).getValue().getETag());
            client.downloadToFile(outFile.getPath(), true);
            assertTrue(compareFiles(sourceFile, outFile, 0, sizeBytes),
                "Downloaded file must match source (large chunked upload, size=" + sizeBytes + ")");
        } finally {
            if (!sourceFile.delete()) {
                sourceFile.deleteOnExit();
            }
        }
    }
}
