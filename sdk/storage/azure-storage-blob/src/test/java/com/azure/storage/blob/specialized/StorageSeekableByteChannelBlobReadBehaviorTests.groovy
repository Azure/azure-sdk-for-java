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
    @Shared BlockBlobClient blockBlobClient
    @Shared PageBlobClient pageBlobClient
    @Shared AppendBlobClient appendBlobClient
    @Shared BlobContainerClient containerClient

    def setup() {
        containerClient = getContainerClientBuilder(namer.getRandomName(60)).buildClient()
        blockBlobClient = containerClient.getBlobClient(namer.getRandomName(60)).getBlockBlobClient()
        pageBlobClient = containerClient.getBlobClient(namer.getRandomName(60)).getPageBlobClient()
        appendBlobClient = containerClient.getBlobClient(namer.getRandomName(60)).getAppendBlobClient()
    }

    def cleanup() {
        containerClient.deleteIfExists()
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
        def behavior = new StorageSeekableByteChannelBlobReadBehavior(client, null, -1, Constants.MB, null)

        when: "ReadBehavior.read() called"
        def buffer = ByteBuffer.allocate(bufferSize)
        behavior.read(buffer, offset)

        then: "Expected ShareFileClient download parameters given"
        // if this test throws null pointer exceptions, then the condition isn't matching and so the stubbed response is
        // not being appropriately returned to the write behavior
        1 * client.downloadStreamWithResponse(_,
            { BlobRange range -> range.getOffset() == offset && range.getCount() as Integer == bufferSize}, null,
            conditions, false, null, null) >> { createMockDownloadResponse("bytes $offset-${offset + buffer.limit() - 1}/$Constants.MB") }

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
                    return createMockDownloadResponse("bytes $offset-${offset + buffer.limit() - 1}/$Constants.MB}")
            }
        0 * client.downloadStreamWithResponse(_, _, _, _, _, _, _)
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
    def "Read graceful past end of blob - block blob"() {
        given: "Initialized blob and behavior to target it"
        def data = getRandomByteArray(fileSize)
        containerClient.create()
        blockBlobClient.upload(BinaryData.fromBytes(data))
        pageBlobClient.create(fileSize)
        pageBlobClient.uploadPages(new PageRange().setStart(0).setEnd(fileSize), new ByteArrayInputStream(data))
        appendBlobClient.create()
        appendBlobClient.appendBlock(new ByteArrayInputStream(data), fileSize)

        def behavior = new StorageSeekableByteChannelBlobReadBehavior(client as BlobClientBase, null, -1, fileSize, null)

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
        client           | fileSize     | offset            | readSize         | expectedRead
        blockBlobClient  | Constants.KB | 0                 | 2 * Constants.KB | Constants.KB       // read larger than file
        blockBlobClient  | Constants.KB | 600               | Constants.KB     | Constants.KB - 600 // overlap on end of file
        blockBlobClient  | Constants.KB | Constants.KB      | Constants.KB     | -1                 // starts at end of file
        blockBlobClient  | Constants.KB | Constants.KB + 20 | Constants.KB     | -1                 // completely past file

        pageBlobClient   | Constants.KB | 0                 | 2 * Constants.KB | Constants.KB
        pageBlobClient   | Constants.KB | 600               | Constants.KB     | Constants.KB - 600
        pageBlobClient   | Constants.KB | Constants.KB      | Constants.KB     | -1
        pageBlobClient   | Constants.KB | Constants.KB + 20 | Constants.KB     | -1

        appendBlobClient | Constants.KB | 0                 | 2 * Constants.KB | Constants.KB
        appendBlobClient | Constants.KB | 600               | Constants.KB     | Constants.KB - 600
        appendBlobClient | Constants.KB | Constants.KB      | Constants.KB     | -1
        appendBlobClient | Constants.KB | Constants.KB + 20 | Constants.KB     | -1
    }

    def "Read detects blob growth"() {
        given: "data"
        int halfLength = 512
        def data = getRandomByteArray(2 * halfLength)

        and: "Blob at half size"
        containerClient.create()
        def blockId1 = "blockId1".bytes.encodeBase64().toString()
        blockBlobClient.stageBlock(blockId1, BinaryData.fromBytes(data[0..halfLength-1] as byte[]))
        blockBlobClient.commitBlockList([blockId1])

        and: "behavior to read blob"
        def behavior = new StorageSeekableByteChannelBlobReadBehavior(blockBlobClient, null, -1, halfLength, null)
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
        blockBlobClient.stageBlock(blockId1, BinaryData.fromBytes(data[halfLength..-1] as byte[]))
        blockBlobClient.commitBlockList([blockId1, blockId2])

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
