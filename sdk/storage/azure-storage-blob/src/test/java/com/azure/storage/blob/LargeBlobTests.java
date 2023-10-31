// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.parallel.ResourceLock;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ResourceLock("LargeBlobTests")
@EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
public class LargeBlobTests extends BlobTestBase {

    private static BlobServiceClient blobServiceClient;
    private static BlobServiceAsyncClient blobServiceAsyncClient;

    private static final long LARGE_BLOCK_SIZE = 2500L * Constants.MB; // exceed integer limit

    private BlobClient blobClient;
    private BlobAsyncClient blobAsyncClient;

    @BeforeAll
    public static void setupSpec() {
        BlobServiceClientBuilder blobServiceClientBuilder = new BlobServiceClientBuilder()
            .connectionString("UseDevelopmentStorage=true");
        blobServiceClient = blobServiceClientBuilder.buildClient();
        blobServiceAsyncClient = blobServiceClientBuilder.buildAsyncClient();
    }

    @BeforeEach
    public void setup() {
        String containerName = UUID.randomUUID().toString();
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobContainerAsyncClient blobContainerAsyncClient =
            blobServiceAsyncClient.getBlobContainerAsyncClient(containerName);
        blobContainerClient.create();
        String blobName = UUID.randomUUID().toString();
        blobClient = blobContainerClient.getBlobClient(blobName);
        blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(blobName);
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void stageRealLargeBlob() {
        InputStream stream = createLargeInputStream(LARGE_BLOCK_SIZE);
        String blockId = Base64.getEncoder().encodeToString(UUID.randomUUID().toString()
            .getBytes(StandardCharsets.UTF_8));

        blobClient.getBlockBlobClient().stageBlock(blockId, stream, LARGE_BLOCK_SIZE);
        blobClient.getBlockBlobClient().commitBlockList(Collections.singletonList((blockId)));
        BlockList blockList = blobClient.getBlockBlobClient().listBlocks(BlockListType.COMMITTED);

        assertEquals(1, blockList.getCommittedBlocks().size());
        assertEquals(LARGE_BLOCK_SIZE, blockList.getCommittedBlocks().get(0).getSizeLong());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void uploadRealLargeBlobInSingleUpload() {
        long size = LARGE_BLOCK_SIZE;
        InputStream stream = createLargeInputStream(size);
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setMaxSingleUploadSizeLong(BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES_LONG);

        blobClient.uploadWithResponse(stream, size, parallelTransferOptions, null, null, null, null, null,
            Context.NONE);
        BlobProperties properties = blobClient.getProperties();
        assertEquals(size, properties.getBlobSize());
        assertNull(properties.getCommittedBlockCount());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void uploadRealLargeBlobInSingleUploadAsync() {
        long size = LARGE_BLOCK_SIZE;
        Flux<ByteBuffer> flux = createLargeBuffer(size);
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setMaxSingleUploadSizeLong(BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES_LONG);

        blobAsyncClient.upload(flux, parallelTransferOptions).block();
        BlobProperties properties = blobClient.getProperties();
        assertEquals(size, properties.getBlobSize());
        assertNull(properties.getCommittedBlockCount());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void uploadLargeInput() {
        long tailSize = Constants.MB;
        long length = LARGE_BLOCK_SIZE + tailSize;
        Flux<ByteBuffer> flux = createLargeBuffer(length);
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(LARGE_BLOCK_SIZE);

        blobAsyncClient.upload(flux, parallelTransferOptions).block();
        BlockList blockList = blobAsyncClient.getBlockBlobAsyncClient().listBlocks(BlockListType.COMMITTED).block();
        assertNotNull(blockList);
        assertEquals(2, blockList.getCommittedBlocks().size());
        assertEquals(LARGE_BLOCK_SIZE, blockList.getCommittedBlocks().get(0).getSizeLong());
        assertEquals(tailSize, blockList.getCommittedBlocks().get(1).getSizeLong());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void uploadLargeInputSync() {
        long tailSize = Constants.MB;
        long length = LARGE_BLOCK_SIZE + tailSize;
        InputStream stream = createLargeInputStream(length, Constants.MB);
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(LARGE_BLOCK_SIZE);

        blobClient.uploadWithResponse(stream, length, parallelTransferOptions, null, null, null, null, null,
            Context.NONE);
        BlockList blockList = blobClient.getBlockBlobClient().listBlocks(BlockListType.COMMITTED);

        assertEquals(2, blockList.getCommittedBlocks().size());
        assertEquals(LARGE_BLOCK_SIZE, blockList.getCommittedBlocks().get(0).getSizeLong());
        assertEquals(tailSize, blockList.getCommittedBlocks().get(1).getSizeLong());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void uploadLargeInputSyncNoLengthGiven() {
        long tailSize = Constants.MB;
        long length = LARGE_BLOCK_SIZE + tailSize;
        InputStream stream = createLargeInputStream(length, Constants.MB);
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(LARGE_BLOCK_SIZE);

        blobClient.uploadWithResponse(new BlobParallelUploadOptions(stream)
            .setParallelTransferOptions(parallelTransferOptions), null, Context.NONE);
        BlockList blockList = blobClient.getBlockBlobClient().listBlocks(BlockListType.COMMITTED);

        assertEquals(2, blockList.getCommittedBlocks().size());
        assertEquals(LARGE_BLOCK_SIZE, blockList.getCommittedBlocks().get(0).getSizeLong());
        assertEquals(tailSize, blockList.getCommittedBlocks().get(1).getSizeLong());
    }

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
    @Test
    public void uploadLargeFile() throws IOException {
        long tailSize = Constants.MB;
        File file = getRandomLargeFile(LARGE_BLOCK_SIZE + tailSize);
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(LARGE_BLOCK_SIZE);

        blobClient.uploadFromFile(file.toPath().toString(), parallelTransferOptions, null, null, null, null, null);
        BlockList blockList = blobClient.getBlockBlobClient().listBlocks(BlockListType.COMMITTED);

        assertEquals(2, blockList.getCommittedBlocks().size());
        assertEquals(LARGE_BLOCK_SIZE, blockList.getCommittedBlocks().get(0).getSizeLong());
        assertEquals(tailSize, blockList.getCommittedBlocks().get(1).getSizeLong());
    }

    private InputStream createLargeInputStream(long size) {
        return createLargeInputStream(size, Constants.MB);
    }

    private InputStream createLargeInputStream(long size, int chunkSize) {
        long numberOfSubStreams = size / chunkSize;
        Vector<ByteArrayInputStream> subStreams = new Vector<>();
        byte[] bytes = getRandomByteArray(chunkSize);
        for (long i = 0; i < numberOfSubStreams; i++) {
            subStreams.add(new ByteArrayInputStream(bytes));
        }
        return new SequenceInputStream(subStreams.elements()) {
            @Override
            public void reset() {
                // no-op
            }
        };
    }

    private Flux<ByteBuffer> createLargeBuffer(long size) {
        return createLargeBuffer(size, Constants.MB);
    }

    private Flux<ByteBuffer> createLargeBuffer(long size, int bufferSize) {
        byte[] bytes = getRandomByteArray(bufferSize);
        long numberOfSubBuffers = size / bufferSize;
        int remainder = (int) (size - numberOfSubBuffers * bufferSize);
        Flux<ByteBuffer> result =  Flux.just(ByteBuffer.wrap(bytes))
            .map(ByteBuffer::duplicate)
            .repeat(numberOfSubBuffers - 1);
        if (remainder > 0) {
            byte[] extraBytes = getRandomByteArray(remainder);
            result = Flux.concat(result, Flux.just(ByteBuffer.wrap(extraBytes)));
        }
        return result;
    }

    File getRandomLargeFile(long size) throws IOException {
        File file = File.createTempFile(UUID.randomUUID().toString(), ".txt");
        file.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(file);

        if (size > Constants.MB) {
            for (int i = 0; i < size / Constants.MB; i++) {
                int dataSize = (int) Math.min(Constants.MB, size - (long) i * Constants.MB);
                fos.write(getRandomByteArray(dataSize));
            }
        } else {
            fos.write(getRandomByteArray((int) size));
        }

        fos.close();
        return file;
    }
}
