// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.AppendBlobAppendBlockOptions;
import com.azure.storage.blob.options.AppendBlobOutputStreamOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Memory usage (perf) tests for content validation during upload.
 * Run manually when measuring memory during upload with content validation. Skipped if heap too small.
 *
 * Peak memory during upload is influenced by the full path from the upload API through
 * StorageContentValidationPolicy. When structured message encoding is used, the policy
 * encodes lazily via Flux.defer (no collectList materialization), so peak should be
 * close to the no-validation baseline (source + in-flight blocks overhead).
 */
@Execution(ExecutionMode.SAME_THREAD)
public class BlobContentValidationPerfTests extends BlobTestBase {
    private static final int FIFTY_MB = 50 * Constants.MB;
    private static final int FIVE_HUNDRED_MB = 500 * Constants.MB;
    private static final int UNDER_4MB = 2 * Constants.MB;
    private static final int PAGE_BYTES = PageBlobClient.PAGE_BYTES;
    private static final int UNDER_4MB_PAGE_ALIGNED = (UNDER_4MB / PAGE_BYTES) * PAGE_BYTES;
    private static final int FOUR_MB_PAGE_ALIGNED = (4 * Constants.MB / PAGE_BYTES) * PAGE_BYTES;
    private static final int DELAY_BETWEEN_PERF_TESTS_MS = 2500;

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    /**
     * Runs a perf test: records heap before, samples peak heap during upload, prints results.
     *
     * @return peak heap bytes observed during the upload.
     */
    private long runPerfTest(String label, long dataSize, Long blockSize, ThrowingRunnable upload) {
        xmxToSmall(dataSize);
        delayBetweenPerfTests();
        forceGc();
        long usedBefore = getHeapUsed();

        AtomicLong peakHeap = new AtomicLong(usedBefore);
        Thread sampler = startHeapSampler(peakHeap);
        try {
            upload.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopHeapSampler(sampler);
        }

        forceGc();
        long usedAfterGc = getHeapUsed();

        System.out.println("[ContentValidationUploadPerf] " + label + ":");
        System.out.println("  Data size: " + dataSize + " bytes (" + (dataSize / (1024 * 1024)) + " MB)");
        if (blockSize != null) {
            System.out.println("  Block size: " + (blockSize / (1024 * 1024)) + " MB");
        }
        System.out.println("  Heap before: " + (usedBefore / (1024 * 1024)) + " MB");
        System.out.println("  Peak heap during upload: " + (peakHeap.get() / (1024 * 1024)) + " MB");
        System.out.println("  Heap after (post-GC): " + (usedAfterGc / (1024 * 1024)) + " MB");
        System.out.println();
        return peakHeap.get();
    }

    private void xmxToSmall(long size) {
        if (Runtime.getRuntime().maxMemory() < 700 * 1024 * 1024) {
            return; // forceOomDuringUpload mode
        }
        long maxMemory = Runtime.getRuntime().maxMemory();
        if (maxMemory < size * 5) {
            System.out.println("[ContentValidationUploadPerf] Skipping " + size + " bytes: -Xmx too small for " + size
                + " bytes (need at least " + (size * 5) + ", have " + maxMemory + ").");
        }
    }

    private Thread startHeapSampler(AtomicLong peakHeap) {
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                peakHeap.updateAndGet(prev -> Math.max(prev, getHeapUsed()));
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "heap-sampler");
        t.setDaemon(true);
        t.start();
        return t;
    }

    private void stopHeapSampler(Thread sampler) {
        if (sampler != null && sampler.isAlive()) {
            sampler.interrupt();
            try {
                sampler.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void delayBetweenPerfTests() {
        try {
            Thread.sleep(DELAY_BETWEEN_PERF_TESTS_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private long getHeapUsed() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    private void forceGc() {
        System.gc();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private File createTempFile(byte[] data) throws IOException {
        File tempFile = File.createTempFile("blob-content-validation-perf-", ".tmp");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(data);
        }
        return tempFile;
    }

    // ===========================================================================================
    // BlobAsyncClient.uploadWithResponse
    // ===========================================================================================

    @Test
    public void documentMemoryUsageNoValidation() {
        long size = FIVE_HUNDRED_MB;
        runPerfTest("BlobAsyncClient.upload - No validation (baseline)", size, null, () -> {
            BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
            byte[] data = getRandomByteArray((int) size);
            client.uploadWithResponse(new BlobParallelUploadOptions(Flux.just(ByteBuffer.wrap(data)))
                .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong(size))
                .setRequestConditions(new BlobRequestConditions())
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE)).block();
        });
    }

    @Test
    public void documentMemoryUsageCrc64Header() {
        int size = UNDER_4MB;
        runPerfTest("BlobAsyncClient.upload - CRC64 header", size, null, () -> {
            BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
            byte[] data = getRandomByteArray(size);
            client.uploadWithResponse(new BlobParallelUploadOptions(Flux.just(ByteBuffer.wrap(data)))
                .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) size))
                .setRequestConditions(new BlobRequestConditions())
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64)).block();
        });
    }

    @Test
    public void documentMemoryUsageStructuredMessage() {
        int size = FIVE_HUNDRED_MB;
        runPerfTest("BlobAsyncClient.upload - Structured message", size, null, () -> {
            BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
            byte[] data = getRandomByteArray(size);
            client.uploadWithResponse(new BlobParallelUploadOptions(Flux.just(ByteBuffer.wrap(data)))
                .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) size))
                .setRequestConditions(new BlobRequestConditions())
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)).block();
        });
    }

    @Test
    public void documentMemoryUsageChunkedNoValidation() {
        int size = FIVE_HUNDRED_MB;
        long blockSize = 10 * (long) Constants.MB;
        runPerfTest("BlobAsyncClient.upload - Chunked no validation (baseline)", size, blockSize, () -> {
            BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
            byte[] data = getRandomByteArray(size);
            client.uploadWithResponse(new BlobParallelUploadOptions(Flux.just(ByteBuffer.wrap(data)))
                .setParallelTransferOptions(
                    new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
                .setRequestConditions(new BlobRequestConditions())
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE)).block();
        });
    }

    @Test
    public void documentMemoryUsageChunkedStructuredMessage() {
        int size = FIVE_HUNDRED_MB;
        long blockSize = 10 * (long) Constants.MB;
        long peakHeap = runPerfTest("BlobAsyncClient.upload - Chunked structured message", size, blockSize, () -> {
            BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
            byte[] data = getRandomByteArray(size);
            client.uploadWithResponse(new BlobParallelUploadOptions(Flux.just(ByteBuffer.wrap(data)))
                .setParallelTransferOptions(
                    new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
                .setRequestConditions(new BlobRequestConditions())
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64)).block();
        });
        assertTrue(peakHeap < 2L * 1024 * 1024 * 1024,
            "Chunked structured message peak heap must be < 2 GB (sanity check), was " + (peakHeap / (1024 * 1024))
                + " MB");
    }

    // ===========================================================================================
    // BlockBlobAsyncClient.uploadWithResponse (simple upload / Put Blob)
    // ===========================================================================================

    @Test
    public void blockBlobSimpleUploadDocumentMemoryUsageNoValidation() {
        int size = FIVE_HUNDRED_MB;
        runPerfTest("BlockBlobAsyncClient.simpleUpload - No validation (baseline)", size, null, () -> {
            BlockBlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
            byte[] data = getRandomByteArray(size);
            client.uploadWithResponse(new BlockBlobSimpleUploadOptions(BinaryData.fromBytes(data))
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE)).block();
        });
    }

    @Test
    public void blockBlobSimpleUploadDocumentMemoryUsageCrc64Header() {
        int size = UNDER_4MB;
        runPerfTest("BlockBlobAsyncClient.simpleUpload - CRC64 header", size, null, () -> {
            BlockBlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
            byte[] data = getRandomByteArray(size);
            client.uploadWithResponse(new BlockBlobSimpleUploadOptions(BinaryData.fromBytes(data))
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64)).block();
        });
    }

    @Test
    public void blockBlobSimpleUploadDocumentMemoryUsageStructuredMessage() {
        int size = FIVE_HUNDRED_MB;
        runPerfTest("BlockBlobAsyncClient.simpleUpload - Structured message", size, null, () -> {
            BlockBlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
            byte[] data = getRandomByteArray(size);
            client.uploadWithResponse(new BlockBlobSimpleUploadOptions(BinaryData.fromBytes(data))
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)).block();
        });
    }

    // ===========================================================================================
    // BlockBlobAsyncClient.stageBlockWithResponse (Put Block)
    // ===========================================================================================

    @Test
    public void stageBlockDocumentMemoryUsageNoValidation() {
        int size = FIVE_HUNDRED_MB;
        runPerfTest("BlockBlobAsyncClient.stageBlock - No validation (baseline)", size, null, () -> {
            BlockBlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
            byte[] data = getRandomByteArray(size);
            client.stageBlockWithResponse(new BlockBlobStageBlockOptions(getBlockID(), BinaryData.fromBytes(data))
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE)).block();
        });
    }

    @Test
    public void stageBlockDocumentMemoryUsageCrc64Header() {
        int size = UNDER_4MB;
        runPerfTest("BlockBlobAsyncClient.stageBlock - CRC64 header", size, null, () -> {
            BlockBlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
            byte[] data = getRandomByteArray(size);
            client.stageBlockWithResponse(new BlockBlobStageBlockOptions(getBlockID(), BinaryData.fromBytes(data))
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64)).block();
        });
    }

    @Test
    public void stageBlockDocumentMemoryUsageStructuredMessage() {
        int size = FIVE_HUNDRED_MB;
        runPerfTest("BlockBlobAsyncClient.stageBlock - Structured message", size, null, () -> {
            BlockBlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
            byte[] data = getRandomByteArray(size);
            client.stageBlockWithResponse(new BlockBlobStageBlockOptions(getBlockID(), BinaryData.fromBytes(data))
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)).block();
        });
    }

    // ===========================================================================================
    // AppendBlobAsyncClient.appendBlockWithResponse (Append Block)
    // Max append block size is 100MB for service version >= 2022-11-02.
    // ===========================================================================================

    @Test
    public void appendBlockDocumentMemoryUsageNoValidation() {
        int size = FIFTY_MB;
        runPerfTest("AppendBlobAsyncClient.appendBlock - No validation (baseline)", size, null, () -> {
            AppendBlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
            client.create().block();
            byte[] data = getRandomByteArray(size);
            client.appendBlockWithResponse(new AppendBlobAppendBlockOptions(Flux.just(ByteBuffer.wrap(data)), size)
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE)).block();
        });
    }

    @Test
    public void appendBlockDocumentMemoryUsageCrc64Header() {
        int size = UNDER_4MB;
        runPerfTest("AppendBlobAsyncClient.appendBlock - CRC64 header", size, null, () -> {
            AppendBlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
            client.create().block();
            byte[] data = getRandomByteArray(size);
            client.appendBlockWithResponse(new AppendBlobAppendBlockOptions(Flux.just(ByteBuffer.wrap(data)), size)
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64)).block();
        });
    }

    @Test
    public void appendBlockDocumentMemoryUsageStructuredMessage() {
        int size = FIFTY_MB;
        runPerfTest("AppendBlobAsyncClient.appendBlock - Structured message", size, null, () -> {
            AppendBlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName()).getAppendBlobAsyncClient();
            client.create().block();
            byte[] data = getRandomByteArray(size);
            client.appendBlockWithResponse(new AppendBlobAppendBlockOptions(Flux.just(ByteBuffer.wrap(data)), size)
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)).block();
        });
    }

    // ===========================================================================================
    // PageBlobAsyncClient.uploadPagesWithResponse (Put Page)
    // Max page upload size is 4MB.
    // ===========================================================================================

    @Test
    public void uploadPagesDocumentMemoryUsageNoValidation() {
        int size = FOUR_MB_PAGE_ALIGNED;
        runPerfTest("PageBlobAsyncClient.uploadPages - No validation (baseline)", size, null, () -> {
            PageBlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
            client.create(size).block();
            byte[] data = getRandomByteArray(size);
            client
                .uploadPagesWithResponse(new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(size - 1),
                    Flux.just(ByteBuffer.wrap(data))).setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE))
                .block();
        });
    }

    @Test
    public void uploadPagesDocumentMemoryUsageCrc64Header() {
        int size = UNDER_4MB_PAGE_ALIGNED;
        runPerfTest("PageBlobAsyncClient.uploadPages - CRC64 header", size, null, () -> {
            PageBlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
            client.create(size).block();
            byte[] data = getRandomByteArray(size);
            client
                .uploadPagesWithResponse(new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(size - 1),
                    Flux.just(ByteBuffer.wrap(data))).setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))
                .block();
        });
    }

    @Test
    public void uploadPagesDocumentMemoryUsageStructuredMessage() {
        int size = FOUR_MB_PAGE_ALIGNED;
        runPerfTest("PageBlobAsyncClient.uploadPages - Structured message", size, null, () -> {
            PageBlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
            client.create(size).block();
            byte[] data = getRandomByteArray(size);
            client
                .uploadPagesWithResponse(new PageBlobUploadPagesOptions(new PageRange().setStart(0).setEnd(size - 1),
                    Flux.just(ByteBuffer.wrap(data))).setRequestChecksumAlgorithm(StorageChecksumAlgorithm.AUTO))
                .block();
        });
    }

    // ===========================================================================================
    // BlobAsyncClient.uploadFromFileWithResponse
    // ===========================================================================================

    @Test
    public void uploadFromFileDocumentMemoryUsageNoValidation() {
        long size = FIVE_HUNDRED_MB;
        runPerfTest("BlobAsyncClient.uploadFromFile - No validation (baseline)", size, null, () -> {
            BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
            byte[] data = getRandomByteArray((int) size);
            File tempFile = createTempFile(data);
            try {
                client.uploadFromFileWithResponse(new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
                    .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong(size))
                    .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE)).block();
            } finally {
                tempFile.delete();
            }
        });
    }

    @Test
    public void uploadFromFileDocumentMemoryUsageCrc64Header() {
        int size = UNDER_4MB;
        runPerfTest("BlobAsyncClient.uploadFromFile - CRC64 header", size, null, () -> {
            BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
            byte[] data = getRandomByteArray(size);
            File tempFile = createTempFile(data);
            try {
                client.uploadFromFileWithResponse(new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
                    .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) size))
                    .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64)).block();
            } finally {
                tempFile.delete();
            }
        });
    }

    @Test
    public void uploadFromFileDocumentMemoryUsageStructuredMessage() {
        int size = FIVE_HUNDRED_MB;
        runPerfTest("BlobAsyncClient.uploadFromFile - Structured message", size, null, () -> {
            BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
            byte[] data = getRandomByteArray(size);
            File tempFile = createTempFile(data);
            try {
                client.uploadFromFileWithResponse(new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
                    .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) size))
                    .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.AUTO)).block();
            } finally {
                tempFile.delete();
            }
        });
    }

    @Test
    public void uploadFromFileDocumentMemoryUsageChunkedNoValidation() {
        int size = FIVE_HUNDRED_MB;
        long blockSize = 10 * (long) Constants.MB;
        runPerfTest("BlobAsyncClient.uploadFromFile - Chunked no validation (baseline)", size, blockSize, () -> {
            BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
            byte[] data = getRandomByteArray(size);
            File tempFile = createTempFile(data);
            try {
                client.uploadFromFileWithResponse(new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
                    .setParallelTransferOptions(
                        new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
                    .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE)).block();
            } finally {
                tempFile.delete();
            }
        });
    }

    @Test
    public void uploadFromFileDocumentMemoryUsageChunkedStructuredMessage() {
        int size = FIVE_HUNDRED_MB;
        long blockSize = 10 * (long) Constants.MB;
        long peakHeap
            = runPerfTest("BlobAsyncClient.uploadFromFile - Chunked structured message", size, blockSize, () -> {
                BlobAsyncClient client = ccAsync.getBlobAsyncClient(generateBlobName());
                byte[] data = getRandomByteArray(size);
                File tempFile = createTempFile(data);
                try {
                    client.uploadFromFileWithResponse(new BlobUploadFromFileOptions(tempFile.getAbsolutePath())
                        .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(blockSize)
                            .setMaxSingleUploadSizeLong(blockSize))
                        .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64)).block();
                } finally {
                    tempFile.delete();
                }
            });
        assertTrue(peakHeap < 2L * 1024 * 1024 * 1024,
            "Chunked structured message peak heap must be < 2 GB (sanity check), was " + (peakHeap / (1024 * 1024))
                + " MB");
    }

    // ===========================================================================================
    // AppendBlobClient.getBlobOutputStream
    // Max append block size is 100MB for service version >= 2022-11-02.
    // ===========================================================================================

    @Test
    public void appendBlobOutputStreamDocumentMemoryUsageNoValidation() {
        int size = FIFTY_MB;
        runPerfTest("AppendBlobClient.getBlobOutputStream - No validation (baseline)", size, null, () -> {
            AppendBlobClient client = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
            client.create();
            byte[] data = getRandomByteArray(size);
            try (BlobOutputStream os = client.getBlobOutputStream(
                new AppendBlobOutputStreamOptions().setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE))) {
                os.write(data);
            }
        });
    }

    @Test
    public void appendBlobOutputStreamDocumentMemoryUsageCrc64Header() {
        int size = UNDER_4MB;
        runPerfTest("AppendBlobClient.getBlobOutputStream - CRC64 header", size, null, () -> {
            AppendBlobClient client = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
            client.create();
            byte[] data = getRandomByteArray(size);
            try (BlobOutputStream os = client.getBlobOutputStream(
                new AppendBlobOutputStreamOptions().setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
                os.write(data);
            }
        });
    }

    @Test
    public void appendBlobOutputStreamDocumentMemoryUsageStructuredMessage() {
        int size = FIFTY_MB;
        runPerfTest("AppendBlobClient.getBlobOutputStream - Structured message", size, null, () -> {
            AppendBlobClient client = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
            client.create();
            byte[] data = getRandomByteArray(size);
            try (BlobOutputStream os = client.getBlobOutputStream(
                new AppendBlobOutputStreamOptions().setRequestChecksumAlgorithm(StorageChecksumAlgorithm.AUTO))) {
                os.write(data);
            }
        });
    }

    // ===========================================================================================
    // BlockBlobClient.getBlobOutputStream
    // ===========================================================================================

    @Test
    public void blockBlobOutputStreamDocumentMemoryUsageNoValidation() {
        long size = FIVE_HUNDRED_MB;
        runPerfTest("BlockBlobClient.getBlobOutputStream - No validation (baseline)", size, null, () -> {
            BlockBlobClient client = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
            byte[] data = getRandomByteArray((int) size);
            try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
                .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong(size))
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE))) {
                os.write(data);
            }
        });
    }

    @Test
    public void blockBlobOutputStreamDocumentMemoryUsageCrc64Header() {
        int size = UNDER_4MB;
        runPerfTest("BlockBlobClient.getBlobOutputStream - CRC64 header", size, null, () -> {
            BlockBlobClient client = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
            byte[] data = getRandomByteArray(size);
            try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
                .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) size))
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
                os.write(data);
            }
        });
    }

    @Test
    public void blockBlobOutputStreamDocumentMemoryUsageStructuredMessage() {
        int size = FIVE_HUNDRED_MB;
        runPerfTest("BlockBlobClient.getBlobOutputStream - Structured message", size, null, () -> {
            BlockBlobClient client = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
            byte[] data = getRandomByteArray(size);
            try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
                .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) size))
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.AUTO))) {
                os.write(data);
            }
        });
    }

    @Test
    public void blockBlobOutputStreamDocumentMemoryUsageChunkedNoValidation() {
        int size = FIVE_HUNDRED_MB;
        long blockSize = 10 * (long) Constants.MB;
        runPerfTest("BlockBlobClient.getBlobOutputStream - Chunked no validation (baseline)", size, blockSize, () -> {
            BlockBlobClient client = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
            byte[] data = getRandomByteArray(size);
            try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
                .setParallelTransferOptions(
                    new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
                .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE))) {
                os.write(data);
            }
        });
    }

    @Test
    public void blockBlobOutputStreamDocumentMemoryUsageChunkedStructuredMessage() {
        int size = FIVE_HUNDRED_MB;
        long blockSize = 10 * (long) Constants.MB;
        long peakHeap
            = runPerfTest("BlockBlobClient.getBlobOutputStream - Chunked structured message", size, blockSize, () -> {
                BlockBlobClient client = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
                byte[] data = getRandomByteArray(size);
                try (BlobOutputStream os = client.getBlobOutputStream(new BlockBlobOutputStreamOptions()
                    .setParallelTransferOptions(
                        new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
                    .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
                    os.write(data);
                }
            });
        assertTrue(peakHeap < 2L * 1024 * 1024 * 1024,
            "Chunked structured message peak heap must be < 2 GB (sanity check), was " + (peakHeap / (1024 * 1024))
                + " MB");
    }

    // ===========================================================================================
    // PageBlobClient.getBlobOutputStream
    // Max page upload size is 4MB.
    // ===========================================================================================

    @Test
    public void pageBlobOutputStreamDocumentMemoryUsageNoValidation() {
        int size = FOUR_MB_PAGE_ALIGNED;
        runPerfTest("PageBlobClient.getBlobOutputStream - No validation (baseline)", size, null, () -> {
            PageBlobClient client = cc.getBlobClient(generateBlobName()).getPageBlobClient();
            client.create(size);
            byte[] data = getRandomByteArray(size);
            try (BlobOutputStream os = client
                .getBlobOutputStream(new PageBlobOutputStreamOptions(new PageRange().setStart(0).setEnd(size - 1))
                    .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE))) {
                os.write(data);
            }
        });
    }

    @Test
    public void pageBlobOutputStreamDocumentMemoryUsageCrc64Header() {
        int size = UNDER_4MB_PAGE_ALIGNED;
        runPerfTest("PageBlobClient.getBlobOutputStream - CRC64 header", size, null, () -> {
            PageBlobClient client = cc.getBlobClient(generateBlobName()).getPageBlobClient();
            client.create(size);
            byte[] data = getRandomByteArray(size);
            try (BlobOutputStream os = client
                .getBlobOutputStream(new PageBlobOutputStreamOptions(new PageRange().setStart(0).setEnd(size - 1))
                    .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
                os.write(data);
            }
        });
    }

    @Test
    public void pageBlobOutputStreamDocumentMemoryUsageStructuredMessage() {
        int size = FOUR_MB_PAGE_ALIGNED;
        runPerfTest("PageBlobClient.getBlobOutputStream - Structured message", size, null, () -> {
            PageBlobClient client = cc.getBlobClient(generateBlobName()).getPageBlobClient();
            client.create(size);
            byte[] data = getRandomByteArray(size);
            try (BlobOutputStream os = client
                .getBlobOutputStream(new PageBlobOutputStreamOptions(new PageRange().setStart(0).setEnd(size - 1))
                    .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.AUTO))) {
                os.write(data);
            }
        });
    }

    // ===========================================================================================
    // BlockBlobClient.openSeekableByteChannelWrite
    // Each write is a stageBlock call. Block size controls individual write size.
    // ===========================================================================================

    @Test
    public void seekableByteChannelWriteDocumentMemoryUsageNoValidation() {
        int size = FIVE_HUNDRED_MB;
        runPerfTest("BlockBlobClient.openSeekableByteChannelWrite - No validation (baseline)", size, null, () -> {
            BlockBlobClient client = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
            byte[] data = getRandomByteArray(size);
            try (SeekableByteChannel channel
                = client.openSeekableByteChannelWrite(new BlockBlobSeekableByteChannelWriteOptions(
                    BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE).setBlockSizeInBytes((long) size)
                        .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.NONE))) {
                channel.write(ByteBuffer.wrap(data));
            }
        });
    }

    @Test
    public void seekableByteChannelWriteDocumentMemoryUsageCrc64Header() {
        int size = UNDER_4MB;
        runPerfTest("BlockBlobClient.openSeekableByteChannelWrite - CRC64 header", size, null, () -> {
            BlockBlobClient client = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
            byte[] data = getRandomByteArray(size);
            try (SeekableByteChannel channel
                = client.openSeekableByteChannelWrite(new BlockBlobSeekableByteChannelWriteOptions(
                    BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE).setBlockSizeInBytes((long) size)
                        .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.CRC64))) {
                channel.write(ByteBuffer.wrap(data));
            }
        });
    }

    @Test
    public void seekableByteChannelWriteDocumentMemoryUsageStructuredMessage() {
        int size = FIVE_HUNDRED_MB;
        runPerfTest("BlockBlobClient.openSeekableByteChannelWrite - Structured message", size, null, () -> {
            BlockBlobClient client = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
            byte[] data = getRandomByteArray(size);
            try (SeekableByteChannel channel
                = client.openSeekableByteChannelWrite(new BlockBlobSeekableByteChannelWriteOptions(
                    BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE).setBlockSizeInBytes((long) size)
                        .setRequestChecksumAlgorithm(StorageChecksumAlgorithm.AUTO))) {
                channel.write(ByteBuffer.wrap(data));
            }
        });
    }
}
