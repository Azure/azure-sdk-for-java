// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized

import com.azure.core.http.HttpHeaders
import com.azure.core.util.BinaryData
import com.azure.core.util.Context
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.models.BlobDownloadAsyncResponse
import com.azure.storage.blob.models.BlobDownloadHeaders
import com.azure.storage.blob.models.BlobDownloadResponse
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.DownloadRetryOptions
import com.azure.storage.blob.models.PageRange
import com.azure.storage.common.implementation.Constants
import spock.lang.Shared
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.time.Duration

class StorageSeekableByteChannelBlobReadBehaviorTests extends APISpec {
    BlockBlobClient blockBlobClient
    PageBlobClient pageBlobClient
    AppendBlobClient appendBlobClient

    def setup() {
        blockBlobClient = cc.getBlobClient(namer.getRandomName(60)).getBlockBlobClient()
        pageBlobClient = cc.getBlobClient(namer.getRandomName(60)).getPageBlobClient()
        appendBlobClient = cc.getBlobClient(namer.getRandomName(60)).getAppendBlobClient()
    }

    def cleanup() {
        cc.deleteIfExists()
    }

    BlobDownloadResponse createMockDownloadResponse(String contentRange) {
        String contentRangeHeader = "Content-Range"
        return new BlobDownloadResponse(new BlobDownloadAsyncResponse(null, 206,
            new HttpHeaders([(contentRangeHeader): contentRange] as Map<String, String>), null,
            new BlobDownloadHeaders().setContentRange(contentRange)))
    }

    @Unroll
    def "Read calls to client correctly"() {
        given:
        BlobClientBase client = Mock()
        def behavior = new StorageSeekableByteChannelBlobReadBehavior(client, ByteBuffer.allocate(0), -1, Constants.MB,
            conditions)

        when: "ReadBehavior.read() called"
        behavior.read(ByteBuffer.allocate(bufferSize), offset)

        then: "Expected ShareFileClient download parameters given"
        1 * client.downloadStreamWithResponse(_,
            { BlobRange range -> range.getOffset() == offset && range.getCount() as Integer == bufferSize}, null,
            conditions, false, null, null) >> { createMockDownloadResponse("bytes $offset-${offset + bufferSize - 1}/$Constants.MB") }
        // ensure call that fails above condition still returns a value to avoid null pointer
        0 * client.downloadStreamWithResponse(_, _, _, _, _, _, _) >> { createMockDownloadResponse("bytes $offset-${offset + bufferSize - 1}/$Constants.MB") }

        where:
        offset | bufferSize   | conditions
        0      | Constants.KB | null
        50     | Constants.KB | null
        0      | 2000         | null
        0      | Constants.KB | new BlobRequestConditions()
    }

    @Unroll
    def "Read uses cache correctly"() {
        given: "Behavior with a starting cached response"
        BlobClientBase client = Mock()
        def initialCache = getRandomData(cacheSize)
        def behavior = new StorageSeekableByteChannelBlobReadBehavior(client, initialCache, offset, Constants.MB, null)

        when: "ReadBehavior.read() called at offset of cache"
        def buffer = ByteBuffer.allocate(bufferSize)
        int read1 = behavior.read(buffer, offset)

        then: "Cache used"
        0 * client.downloadStreamWithResponse(_, _, _, _, _, _, _)
        read1 == Math.min(bufferSize, cacheSize)
        buffer.array()[0..read1-1] == initialCache.array()[0..read1-1]

        when: "Read again at same offset"
        buffer.clear()
        int read2 = behavior.read(buffer, offset)

        then: "Client read because cache was cleared after use"
        // if this test throws null pointer exceptions, then the condition isn't matching and so the stubbed response is
        // not being appropriately returned to the write behavior
        1 * client.downloadStreamWithResponse(_,
            { BlobRange range -> range.getOffset() == offset && range.getCount() as Integer == bufferSize}, null,
            null, false, null, null) >> {
                OutputStream os, BlobRange range, DownloadRetryOptions retryoptions, BlobRequestConditions conditions,
                boolean md5, Duration d, Context c ->
                    os.write(getRandomData(range.getCount() as int).array())
                    return createMockDownloadResponse("bytes $offset-${offset + bufferSize - 1}/$Constants.MB")
            }
        // ensure call that fails above condition still returns a value to avoid null pointer
        0 * client.downloadStreamWithResponse(_, _, _, _, _, _, _) >> { createMockDownloadResponse("bytes $offset-${offset + bufferSize - 1}/$Constants.MB") }
        read2 == bufferSize
        buffer.position() == read2

        where:
        offset | bufferSize       | cacheSize
        0      | Constants.KB     | Constants.KB
        50     | Constants.KB     | Constants.KB
        0      | 2 * Constants.KB | Constants.KB
        0      | Constants.KB     | 2 * Constants.KB
    }

    @Unroll
    def "Read graceful past end of blob"() {
        given: "Selected client type initialized"
        def data = getRandomByteArray(fileSize)
        BlobClientBase client
        switch (type) {
            case "block":
                blockBlobClient.upload(BinaryData.fromBytes(data))
                client = blockBlobClient
                break;
            case "page":
                pageBlobClient.create(fileSize)
                pageBlobClient.uploadPages(new PageRange().setStart(0).setEnd(fileSize-1), new ByteArrayInputStream(data))
                client = pageBlobClient
                break;
            case "append":
                appendBlobClient.create()
                appendBlobClient.appendBlock(new ByteArrayInputStream(data), fileSize)
                client = appendBlobClient
                break;
            default:
                throw new RuntimeException("Bad test input")
        }

        and: "behavior to target it"
        def behavior = new StorageSeekableByteChannelBlobReadBehavior(client as BlobClientBase, ByteBuffer.allocate(0),
            -1, fileSize, null)

        when: "ReadBehavior.read() called"
        def buffer = ByteBuffer.allocate(readSize)
        def read = behavior.read(buffer, offset)

        then: "graceful read"
        notThrown(Throwable)

        and: "correct amount read"
        read == expectedRead
        buffer.position() == Math.max(expectedRead, 0)
        behavior.getResourceLength().longValue() == fileSize

        and: "if applicable, correct data read"
        if (offset < fileSize) {
            assert buffer.position() == expectedRead
            buffer.limit(buffer.position())
            buffer.rewind()
            def validBufferContent = new byte[buffer.limit()]
            buffer.get(validBufferContent)
            assert validBufferContent as List<Byte> == data[offset..-1]
        }

        where:
        type     | fileSize     | offset            | readSize         | expectedRead
        "block"  | Constants.KB | 0                 | 2 * Constants.KB | Constants.KB       // read larger than file
        "block"  | Constants.KB | 600               | Constants.KB     | Constants.KB - 600 // overlap on end of file
        "block"  | Constants.KB | Constants.KB      | Constants.KB     | -1                 // starts at end of file
        "block"  | Constants.KB | Constants.KB + 20 | Constants.KB     | -1                 // completely past file

        "page"   | Constants.KB | 0                 | 2 * Constants.KB | Constants.KB
        "page"   | Constants.KB | 512               | Constants.KB     | Constants.KB - 512
        "page"   | Constants.KB | Constants.KB      | Constants.KB     | -1
        "page"   | Constants.KB | Constants.KB + 512 | Constants.KB    | -1

        "append" | Constants.KB | 0                 | 2 * Constants.KB | Constants.KB
        "append" | Constants.KB | 600               | Constants.KB     | Constants.KB - 600
        "append" | Constants.KB | Constants.KB      | Constants.KB     | -1
        "append" | Constants.KB | Constants.KB + 20 | Constants.KB     | -1
    }

    def "Read detects blob growth"() {
        given: "data"
        int halfLength = 512
        def data = getRandomByteArray(2 * halfLength)

        and: "Blob at half size"
        def blockId1 = "blockId1".bytes.encodeBase64().toString()
        blockBlobClient.stageBlock(blockId1, BinaryData.fromBytes(data[0..halfLength-1] as byte[]))
        blockBlobClient.commitBlockList([blockId1])

        and: "behavior to read blob"
        def behavior = new StorageSeekableByteChannelBlobReadBehavior(blockBlobClient, ByteBuffer.allocate(0), -1, halfLength, null)
        def buffer = ByteBuffer.allocate(halfLength)

        when: "entire blob initially read"
        def read = behavior.read(buffer, 0)

        then: "behavior state as expected"
        read == halfLength
        behavior.resourceLength.longValue() == halfLength

        and: "buffer correctly filled"
        buffer.position() == buffer.capacity()
        buffer.array() as List<Byte> == data[0..halfLength-1]

        when: "read at end of blob"
        buffer.clear()
        read = behavior.read(buffer, halfLength)

        then: "gracefully signal end of blob"
        notThrown(Throwable)
        read == -1
        behavior.resourceLength.longValue() == halfLength

        and: "buffer unfilled"
        buffer.position() == 0

        when: "blob augmented to full size"
        def blockId2 = "blockId2".bytes.encodeBase64().toString()
        blockBlobClient.stageBlock(blockId2, BinaryData.fromBytes(data[halfLength..-1] as byte[]))
        blockBlobClient.commitBlockList([blockId1, blockId2], true)

        and: "behavior reads at previous EOF"
        buffer.clear()
        read = behavior.read(buffer, behavior.resourceLength.longValue())

        then: "channel state has updated length"
        read == halfLength
        behavior.resourceLength.longValue() == 2 * halfLength

        and: "buffer correctly filled"
        buffer.position() == buffer.capacity()
        buffer.array() as List<Byte> == data[halfLength..-1]
    }
}
