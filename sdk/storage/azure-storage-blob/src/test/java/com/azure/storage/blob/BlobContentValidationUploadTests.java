// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.BinaryData;
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
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.common.StorageChecksumAlgorithm;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobContentValidationUploadTests extends BlobTestBase {
    private static final int TEN_MB = 10 * Constants.MB;
    /* Single-part uploads with length < 4MB use CRC64 header; >= 4MB use structured message. */
    private static final int UNDER_4MB = 2 * Constants.MB;

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

    //  ai silly and doesn't know about max upload sizes in relation to parallelism
    // tomorrow: fix these, fix perf tests, edge case tests, md5 compatibility tests

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

    private static boolean hasOnlyStructuredMessageHeaders(List<HttpHeaders> recordedRequestHeaders) {
        return recordedRequestHeaders.stream().anyMatch(headers -> {
            String bodyType = headers.getValue(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME);
            String contentLength = headers.getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME);
            String contentCrc64 = headers.getValue(Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME);
            return StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE.equals(bodyType)
                && contentLength != null
                && contentCrc64 == null;
        });
    }

    private static boolean hasOnlyCrc64Headers(List<HttpHeaders> recordedRequestHeaders) {
        return recordedRequestHeaders.stream().anyMatch(headers -> {
            String contentCrc64 = headers.getValue(Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME);
            String bodyType = headers.getValue(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME);
            String contentLength = headers.getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME);
            return contentCrc64 != null && bodyType == null && contentLength == null;
        });
    }

    private static boolean hasNoContentValidationHeaders(List<HttpHeaders> recordedRequestHeaders) {
        return recordedRequestHeaders.stream().anyMatch(headers -> {
            String bodyType = headers.getValue(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME);
            String contentLength = headers.getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME);
            String contentCrc64 = headers.getValue(Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME);
            return bodyType == null && contentLength == null && contentCrc64 == null;
        });
    }

    /**
     * Creates a BlobAsyncClient that records all outgoing request headers into the supplied list.
     * Each test should use its own list so tests can run concurrently.
     */
    private BlobAsyncClient createBlobAsyncClientWithRequestSniffer(List<HttpHeaders> recordedRequestHeaders) {
        HttpPipelinePolicy sniffPolicy = (context, next) -> {
            recordedRequestHeaders.add(context.getHttpRequest().getHeaders());
            return next.process();
        };
        BlobServiceAsyncClient serviceClient = getServiceAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(), sniffPolicy);
        return serviceClient.getBlobContainerAsyncClient(containerName).getBlobAsyncClient(generateBlobName());
    }

    private BlobClient createBlobClientWithRequestSniffer(List<HttpHeaders> recordedRequestHeaders) {
        HttpPipelinePolicy sniffPolicy = (context, next) -> {
            recordedRequestHeaders.add(context.getHttpRequest().getHeaders());
            return next.process();
        };
        BlobServiceClient serviceClient = getServiceClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(), sniffPolicy);
        return serviceClient.getBlobContainerClient(containerName).getBlobClient(generateBlobName());
    }

}
