// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.ContentValidationTestUtils.RecordedRequest;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PageBlobRequestConditions;
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
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.azure.storage.blob.ContentValidationTestUtils.allUploadsUseCrc64Header;
import static com.azure.storage.blob.ContentValidationTestUtils.allUploadsUseStructuredMessage;
import static com.azure.storage.blob.ContentValidationTestUtils.assertCrc64HeaderMatches;
import static com.azure.storage.blob.ContentValidationTestUtils.assertStructuredMessageLengths;
import static com.azure.storage.blob.ContentValidationTestUtils.contentBearingUploadRequests;
import static com.azure.storage.blob.ContentValidationTestUtils.hasCommitBlockListRequest;
import static com.azure.storage.blob.ContentValidationTestUtils.noUploadUsesContentValidation;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    // Generic ">= 4 MiB so the single-shot upload path uses a structured message" payload for the replayable
    // (non-live) tests. 5 MiB keeps a single Put Blob / Put Block / Append Block request (still above the 4 MiB
    // structured-message threshold) while roughly halving recording and heap/disk/CPU cost vs the old 10 MiB.
    private static final int FIVE_MB = 5 * Constants.MB;
    /* single-shot uploads with length < 4MB use CRC64 header; >= 4MB use structured message. */
    private static final int UNDER_4MB = 2 * Constants.MB;

    /**
     * Live-only random payload band (256–500 MiB, inclusive upper bound via {@code randomLongFromNamer}+1) for
     * {@code uploadWithResponse}, {@code uploadFromFileWithResponse}, and single-block {@code stageBlock}.
     */
    private static final long LIVE_RANDOM_PARALLEL_PAYLOAD_MIN_BYTES_EXCLUSIVE = 256L * Constants.MB;
    private static final long LIVE_RANDOM_PARALLEL_PAYLOAD_MAX_BYTES_INCLUSIVE = 500L * Constants.MB;

    /**
     * Live-only random payload band for sequential append-block puts only.
     * {@code Flux.concatMap} issues one append REST call per chunk in
     * order (not parallel staging); use a smaller band than {@link #LIVE_RANDOM_PARALLEL_PAYLOAD_MIN_BYTES_EXCLUSIVE}.
     */
    private static final long LIVE_RANDOM_SEQUENTIAL_APPEND_PAYLOAD_MIN_BYTES_EXCLUSIVE = 32L * Constants.MB;
    private static final long LIVE_RANDOM_SEQUENTIAL_APPEND_PAYLOAD_MAX_BYTES_INCLUSIVE = 64L * Constants.MB;

    private static final String MD5_AND_CRC64_EXCLUSIVE_MESSAGE
        = "Only one form of transactional content validation may be used.";

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

        byte[] randomData = getRandomByteArray(FIVE_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) FIVE_MB))
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

        byte[] randomData = getRandomByteArray(FIVE_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) FIVE_MB))
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

        byte[] randomData = getRandomByteArray(FIVE_MB);
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

        byte[] randomData = getRandomByteArray(FIVE_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(data).setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        assertNotNull(client.uploadWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasNoContentValidationHeaders(recorded));
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

        byte[] randomData = getRandomByteArray(FIVE_MB);
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

        byte[] randomData = getRandomByteArray(FIVE_MB);
        BinaryData data = BinaryData.fromBytes(randomData);

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(getBlockID(), data)
            .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        client.stageBlockWithResponse(options, null, Context.NONE);
        assertTrue(hasNoContentValidationHeaders(recorded));
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
            = new AppendBlobAppendBlockOptions(BinaryData.fromStream(data, (long) UNDER_4MB))
                .setContentValidationAlgorithm(algorithm);

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

        byte[] randomData = getRandomByteArray(FIVE_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        AppendBlobAppendBlockOptions options
            = new AppendBlobAppendBlockOptions(BinaryData.fromStream(data, (long) FIVE_MB))
                .setContentValidationAlgorithm(algorithm);

        assertNotNull(client.appendBlockWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasOnlyStructuredMessageHeaders(recorded));
    }

    @Test
    public void appendBlockWithNoContentValidation() {
        List<HttpHeaders> recorded = new CopyOnWriteArrayList<>();
        BlobClient blobClient = createBlobClientWithRequestSniffer(recorded);
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(FIVE_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        AppendBlobAppendBlockOptions options
            = new AppendBlobAppendBlockOptions(BinaryData.fromStream(data, (long) FIVE_MB))
                .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        assertNotNull(client.appendBlockWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasNoContentValidationHeaders(recorded));
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

        PageRange pageRange = new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1);
        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(pageRange, BinaryData.fromStream(data, (long) UNDER_4MB_PAGE_ALIGNED))
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

        PageRange pageRange = new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1);
        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(pageRange, BinaryData.fromStream(data, (long) FOUR_MB_PAGE_ALIGNED))
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

        PageRange pageRange = new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1);
        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(pageRange, BinaryData.fromStream(data, (long) FOUR_MB_PAGE_ALIGNED))
                .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE);

        assertNotNull(client.uploadPagesWithResponse(options, null, Context.NONE).getValue().getETag());
        assertTrue(hasNoContentValidationHeaders(recorded));
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

        File tempFile = getRandomFile(FIVE_MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) FIVE_MB))
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

        File tempFile = getRandomFile(FIVE_MB);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) FIVE_MB))
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

        byte[] randomData = getRandomByteArray(FIVE_MB);

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

        byte[] randomData = getRandomByteArray(FIVE_MB);

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

        byte[] randomData = getRandomByteArray(FIVE_MB);

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) FIVE_MB))
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

        byte[] randomData = getRandomByteArray(FIVE_MB);

        try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) FIVE_MB))
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
        BlobClient client = cc.getBlobClient(generateBlobName());

        byte[] randomData = getRandomByteArray(FIVE_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data).setParallelTransferOptions(
            new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) FIVE_MB).setProgressListener(l -> {
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
        BlobClient client = cc.getBlobClient(generateBlobName());

        File tempFile = getRandomFile(FIVE_MB);

        BlobUploadFromFileOptions options
            = new BlobUploadFromFileOptions(tempFile.getAbsolutePath()).setParallelTransferOptions(
                new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) FIVE_MB).setProgressListener(l -> {
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
        BlobClient client = cc.getBlobClient(generateBlobName());

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
        BlobClient client = cc.getBlobClient(generateBlobName());

        byte[] randomData = getRandomByteArray(FIVE_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) FIVE_MB))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadWithResponse(options, null, Context.NONE);

        byte[] downloaded = client.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded data (structured message path)");
    }

    @LiveOnly // Put Block URLs include random block IDs; not replayable with the test proxy.
    @Test
    public void uploadChunkedWithStructuredMessageRoundTripDataIntegrity() {
        BlobClient client = cc.getBlobClient(generateBlobName());

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
        BlobClient blobClient = cc.getBlobClient(generateBlobName());
        BlockBlobClient client = blobClient.getBlockBlobClient();

        byte[] randomData = getRandomByteArray(FIVE_MB);
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
        BlobClient blobClient = cc.getBlobClient(generateBlobName());
        AppendBlobClient client = blobClient.getAppendBlobClient();
        client.create();

        byte[] randomData = getRandomByteArray(FIVE_MB);
        InputStream data = new ByteArrayInputStream(randomData);

        AppendBlobAppendBlockOptions options
            = new AppendBlobAppendBlockOptions(BinaryData.fromStream(data, (long) FIVE_MB))
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.appendBlockWithResponse(options, null, Context.NONE);

        byte[] downloaded = blobClient.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded data (append block)");
    }

    @Test
    public void uploadPagesRoundTripDataIntegrity() {
        BlobClient blobClient = cc.getBlobClient(generateBlobName());
        PageBlobClient client = blobClient.getPageBlobClient();
        client.create(FOUR_MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);
        InputStream data = new ByteArrayInputStream(randomData);

        PageRange pageRange = new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1);
        PageBlobUploadPagesOptions options
            = new PageBlobUploadPagesOptions(pageRange, BinaryData.fromStream(data, (long) FOUR_MB_PAGE_ALIGNED))
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadPagesWithResponse(options, null, Context.NONE);

        byte[] downloaded = blobClient.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded data (page blob upload pages)");
    }

    @Test
    public void uploadFromFileRoundTripDataIntegrity() throws IOException {
        BlobClient client = cc.getBlobClient(generateBlobName());

        byte[] randomData = getRandomByteArray(FIVE_MB);
        File tempFile = File.createTempFile("blob-cv-roundtrip", ".bin");
        tempFile.deleteOnExit();
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
            fos.write(randomData);
        }

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) FIVE_MB))
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        client.uploadFromFileWithResponse(options, null, Context.NONE);

        byte[] downloaded = client.downloadContent().toBytes();
        assertArrayEquals(randomData, downloaded, "Downloaded data must match uploaded file data");
    }

    // ===========================================================================================
    // Live-only random payload bands.
    //   - 256–500 MiB: parallelUpload, uploadFromFile, stageBlock, block BlobOutputStream, SeekableByteChannel —
    //     parallel staging or single giant block / default transfer options as applicable.
    //   - 32–64 MiB (sequential append blocks only): appendBlobAppendBlocksLiveRandom… — one append REST call per
    //     chunk in order.
    // ===========================================================================================

    @LiveOnly // This test is too large for the test proxy.
    @Test
    public void parallelUploadLiveRandomRoundTripDataIntegrity() throws Exception {
        int chosenPayloadSizeBytes = (int) randomLongFromNamer(LIVE_RANDOM_PARALLEL_PAYLOAD_MIN_BYTES_EXCLUSIVE + 1,
            LIVE_RANDOM_PARALLEL_PAYLOAD_MAX_BYTES_INCLUSIVE + 1);
        try {
            String prefix = "chosenPayloadSizeBytes=" + chosenPayloadSizeBytes + ". ";
            BlobClient client = cc.getBlobClient(generateBlobName());
            File sourceFile = getRandomFile(chosenPayloadSizeBytes);
            File outFile = Files.createTempFile("blob-cv-live-par-dl", ".bin").toFile();
            outFile.deleteOnExit();
            try {
                try (InputStream data = new FileInputStream(sourceFile)) {
                    BlobParallelUploadOptions options
                        = new BlobParallelUploadOptions(data).setRequestConditions(new BlobRequestConditions())
                            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
                    client.uploadWithResponse(options, null, Context.NONE);
                }
                client.downloadToFile(outFile.getPath(), true);
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
            BlobClient blobClient = cc.getBlobClient(generateBlobName());
            BlockBlobClient client = blobClient.getBlockBlobClient();
            String blockId = getBlockID();

            File sourceFile = getRandomFile(chosenPayloadSizeBytes);
            File outFile = Files.createTempFile("blob-cv-live-stage-dl", ".bin").toFile();
            outFile.deleteOnExit();
            try {
                BinaryData binaryData = BinaryData.fromFile(sourceFile.toPath());
                BlockBlobStageBlockOptions stageOptions = new BlockBlobStageBlockOptions(blockId, binaryData)
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
                client.stageBlockWithResponse(stageOptions, null, Context.NONE);
                client.commitBlockList(Collections.singletonList(blockId));
                blobClient.downloadToFile(outFile.getPath(), true);

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
    public void appendBlobAppendBlocksLiveRandomRoundTripDataIntegrity() throws Exception {
        int chosenPayloadSizeBytes
            = (int) randomLongFromNamer(LIVE_RANDOM_SEQUENTIAL_APPEND_PAYLOAD_MIN_BYTES_EXCLUSIVE + 1,
                LIVE_RANDOM_SEQUENTIAL_APPEND_PAYLOAD_MAX_BYTES_INCLUSIVE + 1);
        try {
            String prefix = "chosenPayloadSizeBytes=" + chosenPayloadSizeBytes + ". ";
            BlobClient blobClient = cc.getBlobClient(generateBlobName());
            AppendBlobClient client = blobClient.getAppendBlobClient();
            File sourceFile = getRandomFile(chosenPayloadSizeBytes);
            File outFile = Files.createTempFile("blob-cv-live-append-blocks-dl", ".bin").toFile();
            outFile.deleteOnExit();
            client.create();
            int maxAppendBlockBytes = client.getMaxAppendBlockBytes();
            try {
                try (FileInputStream fis = new FileInputStream(sourceFile)) {
                    long remaining = chosenPayloadSizeBytes;
                    byte[] buf = new byte[maxAppendBlockBytes];
                    while (remaining > 0) {
                        int chunk = (int) Math.min(maxAppendBlockBytes, remaining);
                        int totalRead = 0;
                        while (totalRead < chunk) {
                            int n = fis.read(buf, totalRead, chunk - totalRead);
                            if (n == -1) {
                                throw new EOFException(
                                    prefix + "Unexpected EOF after " + totalRead + " bytes of chunk.");
                            }
                            totalRead += n;
                        }
                        ByteArrayInputStream chunkStream = new ByteArrayInputStream(buf, 0, chunk);
                        AppendBlobAppendBlockOptions appendOptions
                            = new AppendBlobAppendBlockOptions(BinaryData.fromStream(chunkStream, (long) chunk))
                                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
                        client.appendBlockWithResponse(appendOptions, null, Context.NONE);
                        remaining -= chunk;
                    }
                }
                blobClient.downloadToFile(outFile.getPath(), true);
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
            BlobClient client = cc.getBlobClient(generateBlobName());
            File sourceFile = getRandomFile(chosenPayloadSizeBytes);
            File outFile = Files.createTempFile("blob-cv-live-uploadfromfile-dl", ".bin").toFile();
            outFile.deleteOnExit();
            try {
                BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(sourceFile.getAbsolutePath())
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
                client.uploadFromFileWithResponse(options, null, Context.NONE);
                client.downloadToFile(outFile.getPath(), true);
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
    public void blockBlobOutputStreamLiveRandomRoundTripDataIntegrity() throws Exception {
        int chosenPayloadSizeBytes = (int) randomLongFromNamer(LIVE_RANDOM_PARALLEL_PAYLOAD_MIN_BYTES_EXCLUSIVE + 1,
            LIVE_RANDOM_PARALLEL_PAYLOAD_MAX_BYTES_INCLUSIVE + 1);
        try {
            String prefix = "chosenPayloadSizeBytes=" + chosenPayloadSizeBytes + ". ";
            BlobClient blobClient = cc.getBlobClient(generateBlobName());
            BlockBlobClient client = blobClient.getBlockBlobClient();

            // Explicit parallel-transfer tuning (8 MiB blocks × concurrency 8) on the stream ingest path.
            ParallelTransferOptions parallelTransferOptions
                = new ParallelTransferOptions().setBlockSizeLong(8L * Constants.MB)
                    .setMaxSingleUploadSizeLong(8L * Constants.MB)
                    .setMaxConcurrency(8);

            File sourceFile = getRandomFile(chosenPayloadSizeBytes);
            File outFile = Files.createTempFile("blob-cv-live-block-os-dl", ".bin").toFile();
            outFile.deleteOnExit();
            try {
                try (BlobOutputStream outputStream = client.getBlobOutputStream(
                    new BlockBlobOutputStreamOptions().setParallelTransferOptions(parallelTransferOptions)
                        .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64))) {
                    Files.copy(sourceFile.toPath(), outputStream);
                }

                blobClient.downloadToFile(outFile.getPath(), true);
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
    public void seekableByteChannelWriteLiveRandomRoundTripDataIntegrity() throws Exception {
        int chosenPayloadSizeBytes = (int) randomLongFromNamer(LIVE_RANDOM_PARALLEL_PAYLOAD_MIN_BYTES_EXCLUSIVE + 1,
            LIVE_RANDOM_PARALLEL_PAYLOAD_MAX_BYTES_INCLUSIVE + 1);
        try {
            String prefix = "chosenPayloadSizeBytes=" + chosenPayloadSizeBytes + ". ";
            BlobClient blobClient = cc.getBlobClient(generateBlobName());
            BlockBlobClient client = blobClient.getBlockBlobClient();
            File sourceFile = getRandomFile(chosenPayloadSizeBytes);
            File outFile = Files.createTempFile("blob-cv-live-sbc-dl", ".bin").toFile();
            outFile.deleteOnExit();
            try {
                try (java.nio.channels.SeekableByteChannel seekableByteChannel
                    = client.openSeekableByteChannelWrite(new BlockBlobSeekableByteChannelWriteOptions(
                        BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE)
                            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64))) {
                    Files.copy(sourceFile.toPath(), Channels.newOutputStream(seekableByteChannel));
                }

                blobClient.downloadToFile(outFile.getPath(), true);
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

    // ---------- Deterministic parallel upload ----------

    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelUploadPutBlobReplayableCases")
    public void fuzzyParallelUploadPutBlobReplayableRoundTrip(int payloadBytes, long segmentBytes, int maxConcurrency)
        throws IOException {
        assertParallelUploadFuzzyRoundTrip("putBlobReplay", payloadBytes, segmentBytes, maxConcurrency);
    }

    @LiveOnly // Staging-only cases: Put Block URLs include random IDs
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelUploadSmallPayloadStagingCases")
    public void fuzzyParallelUploadSmallPayloadRoundTripRequiresLiveStaging(int payloadBytes, long segmentBytes,
        int maxConcurrency) throws IOException {
        assertParallelUploadFuzzyRoundTrip("smallPayloadStaging", payloadBytes, segmentBytes, maxConcurrency);
    }

    @LiveOnly // payload > segment for every tuple; always staging/Put Block.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelUploadSub4MiBCases")
    public void fuzzyParallelUploadSubFourMiBBlobRoundTrip(int payloadBytes, long segmentBytes, int maxConcurrency)
        throws IOException {
        assertParallelUploadFuzzyRoundTrip("subFourMiB", payloadBytes, segmentBytes, maxConcurrency);
    }

    @LiveOnly // Staging-only cases.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelUploadFourMiBBoundaryStagingCases")
    public void fuzzyParallelUploadFourMiBBoundaryRoundTripRequiresLiveStaging(int payloadBytes, long segmentBytes,
        int maxConcurrency) throws IOException {
        assertParallelUploadFuzzyRoundTrip("fourMiBBoundaryStaging", payloadBytes, segmentBytes, maxConcurrency);
    }

    @LiveOnly // payload > segment throughout; chunked upload.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelUploadMediumMultiPartCases")
    public void fuzzyParallelUploadMediumMultiPartRoundTrip(int payloadBytes, long segmentBytes, int maxConcurrency)
        throws IOException {
        assertParallelUploadFuzzyRoundTrip("mediumMultiPart", payloadBytes, segmentBytes, maxConcurrency);
    }

    @LiveOnly // payload >> segment throughout; chunked upload / large payloads.
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#fuzzyParallelUploadLargeMultiPartCases")
    public void fuzzyParallelUploadLargeMultiPartRoundTrip(int payloadBytes, long segmentBytes, int maxConcurrency)
        throws IOException {
        assertParallelUploadFuzzyRoundTrip("largeMultiPart", payloadBytes, segmentBytes, maxConcurrency);
    }

    private void assertParallelUploadFuzzyRoundTrip(String caseKind, int payloadBytes, long segmentBytes,
        int maxConcurrency) throws IOException {
        BlobClient client = cc.getBlobClient(generateBlobName());

        ParallelTransferOptions parallelOptions = new ParallelTransferOptions().setBlockSizeLong(segmentBytes)
            .setMaxSingleUploadSizeLong(segmentBytes)
            .setMaxConcurrency(maxConcurrency);

        String assertionMessage = "Fuzzy parallel upload [" + caseKind + "] payloadBytes=" + payloadBytes
            + ", segmentBytes=" + segmentBytes + ", maxConcurrency=" + maxConcurrency;

        // above this threshold the fuzzy parallel upload helpers stream from a temp source file
        // to avoid materializing the full payload twice in heap.
        if (payloadBytes >= 96 * Constants.MB) {
            File sourceFile = getRandomFile(payloadBytes);
            File outFile = Files.createTempFile("blob-cv-fuzzy-parallel-dl", ".bin").toFile();
            outFile.deleteOnExit();
            try {
                try (InputStream data = new FileInputStream(sourceFile)) {
                    BlobParallelUploadOptions options
                        = new BlobParallelUploadOptions(data).setParallelTransferOptions(parallelOptions)
                            .setRequestConditions(new BlobRequestConditions())
                            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
                    client.uploadWithResponse(options, null, Context.NONE);
                }
                client.downloadToFile(outFile.getPath(), true);
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
            InputStream data = new ByteArrayInputStream(randomData);
            BlobParallelUploadOptions options
                = new BlobParallelUploadOptions(data).setParallelTransferOptions(parallelOptions)
                    .setRequestConditions(new BlobRequestConditions())
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
            client.uploadWithResponse(options, null, Context.NONE);
            byte[] downloaded = client.downloadContent().toBytes();
            assertArrayEquals(randomData, downloaded, assertionMessage);
        }
    }

    // ===========================================================================================
    // Customer Provided MD5 Byte[] with Content Validation Algorithm
    // ===========================================================================================

    private static final byte[] DEFAULT_MD5 = createDefaultMd5();
    private static final String MESSAGE = "Both x-ms-content-crc64 header and Content-MD5 header are present.";

    private static byte[] createDefaultMd5() {
        try {
            // setContentMd5 expects the raw 16-byte MD5 digest; the SDK Base64-encodes it into the Content-MD5
            // header. Pre-encoding here would produce an invalid digest and let conflict tests pass for the wrong
            // reason, so the fixture must stay raw.
            return MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes());
        } catch (NoSuchAlgorithmException ex) {
            throw LOGGER.logExceptionAsError(new RuntimeException("MD5 algorithm unavailable.", ex));
        }
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void blockBlobUploadWithCustomerProvidedMd5AndCrc64Header(ContentValidationAlgorithm algorithm) {
        BlockBlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>()).getBlockBlobClient();

        BlockBlobSimpleUploadOptions options
            = new BlockBlobSimpleUploadOptions(DATA.getDefaultBinaryData()).setContentValidationAlgorithm(algorithm)
                .setContentMd5(DEFAULT_MD5);

        BlobStorageException e
            = assertThrows(BlobStorageException.class, () -> client.uploadWithResponse(options, null, Context.NONE));
        assertEquals(400, e.getStatusCode());
        assertTrue(e.getMessage().contains(MESSAGE));
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void stageBlockWithCustomerProvidedMd5AndCrc64Header(ContentValidationAlgorithm algorithm) {
        BlockBlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>()).getBlockBlobClient();

        BlockBlobStageBlockOptions options = new BlockBlobStageBlockOptions(getBlockID(), DATA.getDefaultBinaryData())
            .setContentValidationAlgorithm(algorithm)
            .setContentMd5(DEFAULT_MD5);

        BlobStorageException e = assertThrows(BlobStorageException.class,
            () -> client.stageBlockWithResponse(options, null, Context.NONE));
        assertEquals(400, e.getStatusCode());
        assertTrue(e.getMessage().contains(MESSAGE));
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void appendBlockWithCustomerProvidedMd5AndCrc64Header(ContentValidationAlgorithm algorithm) {
        AppendBlobClient client
            = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>()).getAppendBlobClient();
        client.create();

        byte[] randomData = DATA.getDefaultBytes();
        AppendBlobAppendBlockOptions options = new AppendBlobAppendBlockOptions(
            BinaryData.fromStream(new ByteArrayInputStream(randomData), (long) randomData.length))
                .setContentValidationAlgorithm(algorithm)
                .setContentMd5(DEFAULT_MD5);

        BlobStorageException e = assertThrows(BlobStorageException.class,
            () -> client.appendBlockWithResponse(options, null, Context.NONE));
        assertEquals(400, e.getStatusCode());
        assertTrue(e.getMessage().contains(MESSAGE));
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void uploadPagesWithCustomerProvidedMd5AndCrc64Header(ContentValidationAlgorithm algorithm)
        throws NoSuchAlgorithmException {
        PageBlobClient client = createBlobClientWithRequestSniffer(new CopyOnWriteArrayList<>()).getPageBlobClient();
        client.create(UNDER_4MB_PAGE_ALIGNED);

        byte[] randomData = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);
        byte[] md5 = MessageDigest.getInstance("MD5").digest(randomData);
        PageRange pageRange = new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1);
        PageBlobUploadPagesOptions options = new PageBlobUploadPagesOptions(pageRange,
            BinaryData.fromStream(new ByteArrayInputStream(randomData), (long) UNDER_4MB_PAGE_ALIGNED))
                .setContentValidationAlgorithm(algorithm)
                .setContentMd5(md5);

        BlobStorageException e = assertThrows(BlobStorageException.class,
            () -> client.uploadPagesWithResponse(options, null, Context.NONE));
        assertEquals(400, e.getStatusCode());
        assertTrue(e.getMessage().contains(MESSAGE));
    }

    // ===========================================================================================
    // Request-shape verification: exact header mode, checksum value, and per-request content length.
    //
    // Requests are captured with a pass-through sniffer (createBlobClientWithFullRequestSniffer) and sent to the
    // service. Upload requests are selected by method/URL so a request missing validation headers fails the check
    // instead of being silently ignored.
    // ===========================================================================================

    private static final int JUST_UNDER_4MB = 4 * Constants.MB - 1;

    private static final HttpHeaderName CV_LEASE_ID = HttpHeaderName.fromString("x-ms-lease-id");
    private static final HttpHeaderName CV_IF_MATCH = HttpHeaderName.fromString("If-Match");
    private static final HttpHeaderName CV_APPEND_POS = HttpHeaderName.fromString("x-ms-blob-condition-appendpos");
    private static final HttpHeaderName CV_MAX_SIZE = HttpHeaderName.fromString("x-ms-blob-condition-maxsize");
    private static final HttpHeaderName CV_SEQ_EQ = HttpHeaderName.fromString("x-ms-if-sequence-number-eq");
    private static final HttpHeaderName CV_RANGE = HttpHeaderName.fromString("x-ms-range");

    private BlockBlobClient recordingBlockClient(List<RecordedRequest> recorded) {
        return createBlobClientWithFullRequestSniffer(recorded).getBlockBlobClient();
    }

    private static byte[] md5Digest(byte[] data) {
        try {
            return MessageDigest.getInstance("MD5").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void putBlobBelow4MbUsesCrc64HeaderWithExactValue(ContentValidationAlgorithm algorithm) {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        byte[] data = getRandomByteArray(UNDER_4MB);

        recordingBlockClient(recorded).uploadWithResponse(
            new BlockBlobSimpleUploadOptions(BinaryData.fromBytes(data)).setContentValidationAlgorithm(algorithm), null,
            Context.NONE);

        List<RecordedRequest> uploads = contentBearingUploadRequests(recorded);
        assertEquals(1, uploads.size());
        assertTrue(allUploadsUseCrc64Header(recorded));
        assertCrc64HeaderMatches(uploads.get(0).getHeaders(), data);
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void putBlobImmediatelyBelow4MbUsesCrc64Header(ContentValidationAlgorithm algorithm) {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        byte[] data = getRandomByteArray(JUST_UNDER_4MB);

        recordingBlockClient(recorded).uploadWithResponse(
            new BlockBlobSimpleUploadOptions(BinaryData.fromBytes(data)).setContentValidationAlgorithm(algorithm), null,
            Context.NONE);

        assertTrue(allUploadsUseCrc64Header(recorded),
            "A payload one byte below 4 MiB must still use the CRC64 header");
        assertCrc64HeaderMatches(contentBearingUploadRequests(recorded).get(0).getHeaders(), data);
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void putBlobExactly4MbUsesStructuredMessageWithExactLengths(ContentValidationAlgorithm algorithm) {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        byte[] data = getRandomByteArray(EXACTLY_4MB);

        recordingBlockClient(recorded).uploadWithResponse(
            new BlockBlobSimpleUploadOptions(BinaryData.fromBytes(data)).setContentValidationAlgorithm(algorithm), null,
            Context.NONE);

        assertTrue(allUploadsUseStructuredMessage(recorded), "Exactly 4 MiB must use a structured message");
        assertStructuredMessageLengths(contentBearingUploadRequests(recorded).get(0).getHeaders(), EXACTLY_4MB);
    }

    @ParameterizedTest
    @EnumSource(value = ContentValidationAlgorithm.class, names = { "CRC64", "AUTO" })
    public void putBlobAbove4MbUsesStructuredMessageWithExactLengths(ContentValidationAlgorithm algorithm) {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        byte[] data = getRandomByteArray(FIVE_MB);

        recordingBlockClient(recorded).uploadWithResponse(
            new BlockBlobSimpleUploadOptions(BinaryData.fromBytes(data)).setContentValidationAlgorithm(algorithm), null,
            Context.NONE);

        assertTrue(allUploadsUseStructuredMessage(recorded));
        assertStructuredMessageLengths(contentBearingUploadRequests(recorded).get(0).getHeaders(), FIVE_MB);
    }

    @Test
    public void putBlobWithNoneAlgorithmHasNoValidation() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        recordingBlockClient(recorded)
            .uploadWithResponse(new BlockBlobSimpleUploadOptions(BinaryData.fromBytes(getRandomByteArray(FIVE_MB)))
                .setContentValidationAlgorithm(ContentValidationAlgorithm.NONE), null, Context.NONE);
        assertTrue(noUploadUsesContentValidation(recorded));
    }

    @Test
    public void putBlobWithOmittedAlgorithmHasNoValidation() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        // The content-validation algorithm setter is intentionally never called (default/null): no headers expected.
        recordingBlockClient(recorded).uploadWithResponse(
            new BlockBlobSimpleUploadOptions(BinaryData.fromBytes(getRandomByteArray(FIVE_MB))), null, Context.NONE);
        assertTrue(noUploadUsesContentValidation(recorded));
    }

    @Test
    public void stageBlockBelow4MbUsesCrc64HeaderWithExactValue() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        byte[] data = getRandomByteArray(UNDER_4MB);
        recordingBlockClient(recorded)
            .stageBlockWithResponse(new BlockBlobStageBlockOptions(getBlockID(), BinaryData.fromBytes(data))
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64), null, Context.NONE);
        assertTrue(allUploadsUseCrc64Header(recorded));
        assertCrc64HeaderMatches(contentBearingUploadRequests(recorded).get(0).getHeaders(), data);
    }

    @Test
    public void stageBlockAbove4MbUsesStructuredMessageWithExactLengths() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        recordingBlockClient(recorded).stageBlockWithResponse(
            new BlockBlobStageBlockOptions(getBlockID(), BinaryData.fromBytes(getRandomByteArray(FIVE_MB)))
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64),
            null, Context.NONE);
        assertTrue(allUploadsUseStructuredMessage(recorded));
        assertStructuredMessageLengths(contentBearingUploadRequests(recorded).get(0).getHeaders(), FIVE_MB);
    }

    @Test
    public void appendBlockBelow4MbUsesCrc64HeaderWithExactValue() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        AppendBlobClient client = createBlobClientWithFullRequestSniffer(recorded).getAppendBlobClient();
        client.create();
        byte[] data = getRandomByteArray(UNDER_4MB);
        client.appendBlockWithResponse(new AppendBlobAppendBlockOptions(BinaryData.fromBytes(data))
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64), null, Context.NONE);
        assertTrue(allUploadsUseCrc64Header(recorded));
        assertCrc64HeaderMatches(contentBearingUploadRequests(recorded).get(0).getHeaders(), data);
    }

    @Test
    public void uploadPagesBelow4MbUsesCrc64HeaderWithExactValueAtZeroOffset() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        PageBlobClient client = createBlobClientWithFullRequestSniffer(recorded).getPageBlobClient();
        client.create(UNDER_4MB_PAGE_ALIGNED);
        byte[] data = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);
        PageRange range = new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1);
        client.uploadPagesWithResponse(new PageBlobUploadPagesOptions(range, BinaryData.fromBytes(data))
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64), null, Context.NONE);
        assertTrue(allUploadsUseCrc64Header(recorded));
        assertCrc64HeaderMatches(contentBearingUploadRequests(recorded).get(0).getHeaders(), data);
    }

    @Test
    public void uploadPagesAtNonZeroOffsetIsValidatedAndForwardsRange() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        PageBlobClient client = createBlobClientWithFullRequestSniffer(recorded).getPageBlobClient();
        int start = 4 * PAGE_BYTES;
        client.create(start + UNDER_4MB_PAGE_ALIGNED);
        byte[] data = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);
        PageRange range = new PageRange().setStart(start).setEnd(start + UNDER_4MB_PAGE_ALIGNED - 1);
        client.uploadPagesWithResponse(new PageBlobUploadPagesOptions(range, BinaryData.fromBytes(data))
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64), null, Context.NONE);

        RecordedRequest upload = contentBearingUploadRequests(recorded).get(0);
        assertTrue(allUploadsUseCrc64Header(recorded));
        assertCrc64HeaderMatches(upload.getHeaders(), data);
        assertEquals("bytes=" + start + "-" + (start + UNDER_4MB_PAGE_ALIGNED - 1),
            upload.getHeaders().getValue(CV_RANGE),
            "A non-zero page offset must be forwarded alongside content validation");
    }

    @Test
    public void uploadPagesAbove4MbUsesStructuredMessageWithExactLengths() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        PageBlobClient client = createBlobClientWithFullRequestSniffer(recorded).getPageBlobClient();
        client.create(FOUR_MB_PAGE_ALIGNED);
        byte[] data = getRandomByteArray(FOUR_MB_PAGE_ALIGNED);
        PageRange range = new PageRange().setStart(0).setEnd(FOUR_MB_PAGE_ALIGNED - 1);
        client.uploadPagesWithResponse(new PageBlobUploadPagesOptions(range, BinaryData.fromBytes(data))
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64), null, Context.NONE);
        assertTrue(allUploadsUseStructuredMessage(recorded));
        assertStructuredMessageLengths(contentBearingUploadRequests(recorded).get(0).getHeaders(),
            FOUR_MB_PAGE_ALIGNED);
    }

    /**
     * Multi-block: every staged block is validated, the request count is asserted (not assumed), and the final
     * partial block is validated with its true length.
     */
    @LiveOnly // Parallel staging uses SDK-generated (random) block IDs in the Put Block URLs; not replayable.
    @Test
    public void chunkedParallelUploadValidatesEveryBlockIncludingFinalPartialBlock() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        BlobClient client = createBlobClientWithFullRequestSniffer(recorded);

        int total = FIVE_MB; // 2 MiB + 2 MiB + 1 MiB -> three Put Block requests, last one partial
        long blockSize = 2L * Constants.MB;
        byte[] data = getRandomByteArray(total);

        client.uploadWithResponse(new BlobParallelUploadOptions(new ByteArrayInputStream(data))
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(blockSize)
                .setMaxSingleUploadSizeLong(blockSize)
                .setMaxConcurrency(1))
            .setRequestConditions(new BlobRequestConditions())
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64), null, Context.NONE);

        List<RecordedRequest> uploads = contentBearingUploadRequests(recorded);
        assertEquals(3, uploads.size(), "A 5 MiB payload with a 2 MiB block size must stage exactly three blocks");
        assertTrue(allUploadsUseStructuredMessage(recorded), "Every staged block must carry a structured message");

        List<Long> perBlockLengths = uploads.stream()
            .map(r -> Long
                .parseLong(r.getHeaders().getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME)))
            .sorted()
            .collect(Collectors.toList());
        assertEquals(Arrays.asList((long) Constants.MB, 2L * Constants.MB, 2L * Constants.MB), perBlockLengths,
            "The final partial (1 MiB) block must be validated with its true length, not padded or skipped");
    }

    @Test
    public void multipleAppendBlocksAreEachValidatedWithExactValues() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        AppendBlobClient client = createBlobClientWithFullRequestSniffer(recorded).getAppendBlobClient();
        client.create();

        byte[][] blocks
            = { getRandomByteArray(UNDER_4MB), getRandomByteArray(UNDER_4MB), getRandomByteArray(Constants.MB) };
        for (byte[] block : blocks) {
            client.appendBlockWithResponse(new AppendBlobAppendBlockOptions(BinaryData.fromBytes(block))
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64), null, Context.NONE);
        }

        List<RecordedRequest> uploads = contentBearingUploadRequests(recorded);
        assertEquals(blocks.length, uploads.size(), "Each append must produce exactly one Append Block request");
        assertTrue(allUploadsUseCrc64Header(recorded), "Every appended block must be validated");
        for (int i = 0; i < blocks.length; i++) {
            assertCrc64HeaderMatches(uploads.get(i).getHeaders(), blocks[i]);
        }
    }

    /**
     * MD5-only control for the MD5+CRC64 conflict tests: a valid raw MD5 digest (no content-validation algorithm)
     * is forwarded Base64-encoded with no conflict, proving the conflict tests introduce only the intended conflict.
     */
    @Test
    public void putBlobMd5OnlyControlIsForwardedWithoutContentValidation() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        byte[] data = getRandomByteArray(UNDER_4MB);
        byte[] rawDigest = md5Digest(data);
        assertEquals(16, rawDigest.length, "MD5 fixture must be the raw 16-byte digest, not a Base64-encoded value");

        recordingBlockClient(recorded).uploadWithResponse(
            new BlockBlobSimpleUploadOptions(BinaryData.fromBytes(data)).setContentMd5(rawDigest), null, Context.NONE);

        RecordedRequest upload = contentBearingUploadRequests(recorded).get(0);
        assertEquals(Base64.getEncoder().encodeToString(rawDigest),
            upload.getHeaders().getValue(HttpHeaderName.CONTENT_MD5),
            "Content-MD5 must be the Base64 of the raw digest");
        assertTrue(noUploadUsesContentValidation(recorded), "MD5-only must not add any content-validation header");
    }

    // ===========================================================================================
    // Condition forwarding together with content validation.
    //
    // The conditions are deliberately unsatisfiable so no server state setup is needed; the request is still sent
    // and recorded with the condition headers (which is what we assert), then rejected server-side.
    // ===========================================================================================

    @Test
    public void appendBlockForwardsLeaseEtagAppendPositionAndSizeWithValidation() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        AppendBlobClient client = createBlobClientWithFullRequestSniffer(recorded).getAppendBlobClient();
        client.create();

        byte[] data = getRandomByteArray(UNDER_4MB);
        AppendBlobRequestConditions conditions
            = new AppendBlobRequestConditions().setLeaseId(testResourceNamer.randomUuid())
                .setIfMatch("\"0xNONMATCHINGETAG\"")
                .setAppendPosition(0L)
                .setMaxSize(1024L * 1024L * 1024L);
        try {
            client.appendBlockWithResponse(
                new AppendBlobAppendBlockOptions(BinaryData.fromBytes(data)).setRequestConditions(conditions)
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64),
                null, Context.NONE);
        } catch (BlobStorageException expectedPreconditionFailure) {
            // The unsatisfiable lease/ETag causes a server rejection; we only assert the outgoing request shape.
        }

        HttpHeaders headers = contentBearingUploadRequests(recorded).get(0).getHeaders();
        assertTrue(ContentValidationTestUtils.isCrc64HeaderRequest(headers));
        assertEquals(conditions.getLeaseId(), headers.getValue(CV_LEASE_ID));
        assertEquals("\"0xNONMATCHINGETAG\"", headers.getValue(CV_IF_MATCH));
        assertEquals("0", headers.getValue(CV_APPEND_POS));
        assertEquals(String.valueOf(1024L * 1024L * 1024L), headers.getValue(CV_MAX_SIZE));
    }

    @Test
    public void uploadPagesForwardsLeaseEtagAndSequenceNumberWithValidation() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        PageBlobClient client = createBlobClientWithFullRequestSniffer(recorded).getPageBlobClient();
        client.create(UNDER_4MB_PAGE_ALIGNED);

        byte[] data = getRandomByteArray(UNDER_4MB_PAGE_ALIGNED);
        PageRange range = new PageRange().setStart(0).setEnd(UNDER_4MB_PAGE_ALIGNED - 1);
        PageBlobRequestConditions conditions
            = new PageBlobRequestConditions().setLeaseId(testResourceNamer.randomUuid())
                .setIfMatch("\"0xNONMATCHINGETAG\"")
                .setIfSequenceNumberEqualTo(7L);
        try {
            client.uploadPagesWithResponse(
                new PageBlobUploadPagesOptions(range, BinaryData.fromBytes(data)).setRequestConditions(conditions)
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64),
                null, Context.NONE);
        } catch (BlobStorageException expectedPreconditionFailure) {
            // The unsatisfiable lease/ETag/sequence number causes a server rejection; assert request shape only.
        }

        HttpHeaders headers = contentBearingUploadRequests(recorded).get(0).getHeaders();
        assertTrue(ContentValidationTestUtils.isCrc64HeaderRequest(headers));
        assertEquals(conditions.getLeaseId(), headers.getValue(CV_LEASE_ID));
        assertEquals("\"0xNONMATCHINGETAG\"", headers.getValue(CV_IF_MATCH));
        assertEquals("7", headers.getValue(CV_SEQ_EQ));
    }

    // ===========================================================================================
    // Failures after work has started: replay-after-consumption, error propagation, and no commit on failure.
    //
    // Failures are injected with a client-side fault policy (the pattern the download tests use), so these are
    // LiveOnly.
    // ===========================================================================================

    private static boolean isUploadAttempt(HttpRequest request) {
        return request.getHeaders().getValue(Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME) != null
            || request.getHeaders().getValue(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME) != null;
    }

    private static reactor.core.publisher.Mono<com.azure.core.http.HttpResponse> injectedError(HttpRequest request,
        int status) {
        byte[] body = ("<?xml version=\"1.0\"?><Error><Code>InjectedFailure</Code>"
            + "<Message>Injected failure</Message></Error>").getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml");
        return reactor.core.publisher.Mono.just(new MockHttpResponse(request, status, headers, body));
    }

    @Test
    public void uploadRetryReplaysBodyAndRevalidatesAfterConsumption() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        AtomicBoolean failedOnce = new AtomicBoolean(false);
        // Fail the first upload attempt once with a retryable 500, but only AFTER draining the request body so the
        // transport has actually consumed the stream. The retry must then replay the body and the content-validation
        // policy must recompute the checksum on the retried request.
        HttpPipelinePolicy fault = (context, next) -> {
            if (isUploadAttempt(context.getHttpRequest()) && failedOnce.compareAndSet(false, true)) {
                return FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody())
                    .then(injectedError(context.getHttpRequest(), 500));
            }
            return next.process();
        };
        BlockBlobClient client = createBlobClientWithFullRequestSniffer(recorded, fault).getBlockBlobClient();

        byte[] data = getRandomByteArray(UNDER_4MB);
        client.uploadWithResponse(new BlockBlobSimpleUploadOptions(BinaryData.fromBytes(data))
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64), null, Context.NONE);

        List<RecordedRequest> uploads = contentBearingUploadRequests(recorded);
        assertTrue(uploads.size() >= 2, "Expected at least one failed attempt followed by a successful retry");
        assertTrue(allUploadsUseCrc64Header(recorded), "Both the failed attempt and the retry must be validated");
        for (RecordedRequest upload : uploads) {
            assertCrc64HeaderMatches(upload.getHeaders(), data);
        }
    }

    @Test
    public void uploadErrorIsPropagated() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        HttpPipelinePolicy fault = (context, next) -> isUploadAttempt(context.getHttpRequest())
            ? injectedError(context.getHttpRequest(), 403)
            : next.process();
        BlockBlobClient client = createBlobClientWithFullRequestSniffer(recorded, fault).getBlockBlobClient();

        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> client.uploadWithResponse(
                new BlockBlobSimpleUploadOptions(BinaryData.fromBytes(getRandomByteArray(UNDER_4MB)))
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64),
                null, Context.NONE));
        assertEquals(403, ex.getStatusCode());
    }

    @Test
    public void failedStageBlockDoesNotCommitIncompleteData() {
        List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
        // Every Put Block fails terminally; the chunked upload must surface the error and never issue Put Block List.
        HttpPipelinePolicy fault = (context, next) -> {
            String url = context.getHttpRequest().getUrl().toString();
            return url.contains("comp=block") && !url.contains("comp=blocklist")
                ? injectedError(context.getHttpRequest(), 403)
                : next.process();
        };
        BlobClient client = createBlobClientWithFullRequestSniffer(recorded, fault);

        long blockSize = 2L * Constants.MB;
        assertThrows(BlobStorageException.class,
            () -> client
                .uploadWithResponse(new BlobParallelUploadOptions(new ByteArrayInputStream(getRandomByteArray(FIVE_MB)))
                    .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(blockSize)
                        .setMaxSingleUploadSizeLong(blockSize)
                        .setMaxConcurrency(1))
                    .setRequestConditions(new BlobRequestConditions())
                    .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64), null, Context.NONE));

        assertFalse(hasCommitBlockListRequest(recorded),
            "A failed multipart upload must not commit a block list of incomplete data");
    }
}
