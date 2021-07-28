// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.util.Context
import com.azure.storage.blob.models.BlockListType
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.options.BlobParallelUploadOptions
import com.azure.storage.blob.specialized.BlockBlobAsyncClient
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.extensions.LiveOnly
import reactor.core.publisher.Flux
import spock.lang.ResourceLock
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

/**
 * This test requires Azurite running in background.
 * See https://github.com/Azure/Azurite
 */
@ResourceLock("LargeBlobTest")
@LiveOnly
class LargeBlobTest extends Specification {

    @Shared
    BlobServiceClient blobServiceClient
    @Shared
    BlobServiceAsyncClient blobServiceAsyncClient

    long largeBlockSize = 2500L * Constants.MB // exceed integer limit

    BlobClient blobClient
    BlobAsyncClient blobAsyncClient
    String blobName

    def setupSpec() {
        def blobServiceClientBuilder = new BlobServiceClientBuilder()
            .connectionString("UseDevelopmentStorage=true")
        blobServiceClient = blobServiceClientBuilder.buildClient()
        blobServiceAsyncClient = blobServiceClientBuilder.buildAsyncClient()
    }

    def setup() {
        def containerName = UUID.randomUUID().toString()
        def blobContainerClient = blobServiceClient.getBlobContainerClient(containerName)
        def blobContainerAsyncClient = blobServiceAsyncClient.getBlobContainerAsyncClient(containerName)
        blobContainerClient.create()
        blobName = UUID.randomUUID().toString()
        blobClient = blobContainerClient.getBlobClient(blobName)
        blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(blobName)
    }

    def "Stage Real Large Blob"() {
        given:
        def stream = createLargeInputStream(largeBlockSize)
        def blockId = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8))

        when:
        blobClient.getBlockBlobClient().stageBlock(blockId, stream, largeBlockSize)
        blobClient.getBlockBlobClient().commitBlockList([blockId])
        def blockList = blobClient.getBlockBlobClient().listBlocks(BlockListType.COMMITTED)

        then:
        blockList.committedBlocks.size() == 1
        blockList.committedBlocks.get(0).getSizeLong() == largeBlockSize
    }

    def "Upload Real Large Blob in Single Upload"() {
        given:
        long size = largeBlockSize
        def stream = createLargeInputStream(size)
        def parallelTransferOptions = new ParallelTransferOptions().setMaxSingleUploadSizeLong(BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES_LONG)

        when:
        blobClient.uploadWithResponse(
            stream, size, parallelTransferOptions,
            null, null, null, null, null, Context.NONE)
        def properties = blobClient.getProperties()

        then:
        notThrown(Exception)
        properties.blobSize == size
        properties.committedBlockCount == null
    }

    def "Upload Real Large Blob in Single Upload Async"() {
        given:
        long size = largeBlockSize
        def flux = createLargeBuffer(size)
        def parallelTransferOptions = new ParallelTransferOptions().setMaxSingleUploadSizeLong(BlockBlobAsyncClient.MAX_UPLOAD_BLOB_BYTES_LONG)

        when:
        blobAsyncClient.upload(flux, parallelTransferOptions).block()
        def properties = blobAsyncClient.getProperties().block()

        then:
        notThrown(Exception)
        properties.blobSize == size
        properties.committedBlockCount == null
    }

    def "Upload Large Input"() {
        given:
        def tailSize = 1L * Constants.MB
        def length = largeBlockSize + tailSize
        def flux = createLargeBuffer(length)
        def parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(largeBlockSize)

        when:
        blobAsyncClient.upload(flux, parallelTransferOptions)
        .block()
        def blockList = blobAsyncClient.getBlockBlobAsyncClient().listBlocks(BlockListType.COMMITTED).block()

        then:
        blockList.committedBlocks.size() == 2
        blockList.committedBlocks[0].sizeLong == largeBlockSize
        blockList.committedBlocks[1].sizeLong == tailSize
    }

    def "Upload Large Input Sync"() {
        given:
        def tailSize = 1L * Constants.MB
        def length = largeBlockSize + tailSize
        def stream = createLargeInputStream(length, Constants.MB)
        def parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(largeBlockSize)

        when:
        blobClient.uploadWithResponse(
            stream, length, parallelTransferOptions,
            null, null, null, null, null, Context.NONE)
        def blockList = blobClient.getBlockBlobClient().listBlocks(BlockListType.COMMITTED)

        then:
        blockList.committedBlocks.size() == 2
        blockList.committedBlocks[0].sizeLong == largeBlockSize
        blockList.committedBlocks[1].sizeLong == tailSize
    }

    def "Upload Large Input Sync No Length Given"() {
        given:
        def tailSize = 1L * Constants.MB
        def length = largeBlockSize + tailSize
        def stream = createLargeInputStream(length, Constants.MB)
        def parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(largeBlockSize)

        when:
        blobClient.uploadWithResponse(
            new BlobParallelUploadOptions(stream).setParallelTransferOptions(parallelTransferOptions),
            null, Context.NONE)
        def blockList = blobClient.getBlockBlobClient().listBlocks(BlockListType.COMMITTED)

        then:
        blockList.committedBlocks.size() == 2
        blockList.committedBlocks[0].sizeLong == largeBlockSize
        blockList.committedBlocks[1].sizeLong == tailSize
    }

    def "Upload Large File"() {
        given:
        def tailSize = 1L * Constants.MB
        def file = getRandomLargeFile(largeBlockSize + tailSize)
        def parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(largeBlockSize)

        when:
        blobClient.uploadFromFile(file.toPath().toString(), parallelTransferOptions,
            null, null, null, null, null)
        def blockList = blobClient.getBlockBlobClient().listBlocks(BlockListType.COMMITTED)

        then:
        blockList.committedBlocks.size() == 2
        blockList.committedBlocks[0].sizeLong == largeBlockSize
        blockList.committedBlocks[1].sizeLong == tailSize
    }

    private InputStream createLargeInputStream(long size) {
        return createLargeInputStream(size, Constants.MB)
    }

    private InputStream createLargeInputStream(long size, int chunkSize) {
        long numberOfSubStreams = (long) (size / chunkSize)
        def subStreams = new Vector()
        def bytes = getRandomByteArray(chunkSize)
        for (long i = 0; i < numberOfSubStreams; i++) {
            subStreams.add(new ByteArrayInputStream(bytes))
        }
        return new SequenceInputStream(subStreams.elements()) {
            @Override
            void reset() throws IOException {
                // no-op
            }
        }
    }

    private Flux<ByteBuffer> createLargeBuffer(long size) {
        return createLargeBuffer(size, Constants.MB)
    }

    private byte[] getRandomByteArray(int size) {
        long seed = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE
        Random rand = new Random(seed)
        byte[] data = new byte[size]
        rand.nextBytes(data)
        return data
    }

    private Flux<ByteBuffer> createLargeBuffer(long size, int bufferSize) {
        def bytes = getRandomByteArray(bufferSize)
        long numberOfSubBuffers = (long) (size / bufferSize)
        int remainder = (int) (size - numberOfSubBuffers * bufferSize)
        Flux<ByteBuffer> result =  Flux.just(ByteBuffer.wrap(bytes))
            .map{buffer -> buffer.duplicate()}
            .repeat(numberOfSubBuffers - 1)
        if (remainder > 0) {
            def extraBytes = getRandomByteArray(remainder)
            result = Flux.concat(result, Flux.just(ByteBuffer.wrap(extraBytes)))
        }
        return result
    }

    File getRandomLargeFile(long size) {
        File file = File.createTempFile(UUID.randomUUID().toString(), ".txt")
        file.deleteOnExit()
        FileOutputStream fos = new FileOutputStream(file)

        if (size > Constants.MB) {
            for (def i = 0; i < size / Constants.MB; i++) {
                def dataSize = (int) Math.min(Constants.MB, size - i * Constants.MB)
                fos.write(getRandomByteArray(dataSize))
            }
        } else {
            fos.write(getRandomByteArray((int) size))
        }

        fos.close()
        return file
    }
}
