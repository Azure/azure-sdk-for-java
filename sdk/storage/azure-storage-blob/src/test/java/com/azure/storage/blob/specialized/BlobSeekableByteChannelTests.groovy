// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized

import com.azure.core.http.HttpHeaders
import com.azure.core.util.BinaryData
import com.azure.core.util.Context
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.BlobDownloadAsyncResponse
import com.azure.storage.blob.models.BlobDownloadHeaders
import com.azure.storage.blob.models.BlobDownloadResponse
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.ConsistentReadControl
import com.azure.storage.blob.models.DownloadRetryOptions
import com.azure.storage.blob.options.BlobSeekableByteChannelReadOptions
import com.azure.storage.blob.options.BlockBlobSeekableByteChannelWriteOptions
import com.azure.storage.blob.options.BlockBlobSeekableByteChannelWriteOptions.WriteMode
import com.azure.storage.common.implementation.StorageSeekableByteChannel
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.TestUtility

import java.nio.ByteBuffer
import java.time.Duration

class BlobSeekableByteChannelTests extends APISpec {
    BlobClient bc
    BlockBlobClient blockClient

    def setup() {
        bc = cc.getBlobClient(generateBlobName())
        blockClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
    }

    def "E2E channel read"() {
        given: "uploaded blob"
        def data = getRandomByteArray(dataLength)
        bc.upload(BinaryData.fromBytes(data))

        when: "Channel initialized"
        def channel = bc.openSeekableByteChannelRead(new BlobSeekableByteChannelReadOptions()
            .setBlockSize(streamBufferSize), null)

        then: "Channel initialized to position zero"
        channel.position() == 0

        when: "read from channel"
        def downloadedData = new ByteArrayOutputStream()
        int copied = TestUtility.copy(channel, downloadedData, copyBufferSize)

        then: "channel position updated accordingly"
        copied == dataLength
        channel.position() == dataLength

        and: "expected data downloaded"
        downloadedData.toByteArray() == data

        where:
        streamBufferSize  | copyBufferSize | dataLength
        50                | 40             | Constants.KB
        Constants.KB + 50 | 40             | Constants.KB // initial fetch larger than resource size
    }

    def "E2E channel write - block"() {
        when: "Channel initialized"
        def channel = blockClient.openSeekableByteChannelWrite(
            new BlockBlobSeekableByteChannelWriteOptions(WriteMode.OVERWRITE).setChunkSize(streamBufferSize))

        then: "Channel initialized to position zero"
        channel.position() == 0

        when: "write to channel"
        def data = getRandomByteArray(dataLength)
        int copied = TestUtility.copy(new ByteArrayInputStream(data), channel, copyBufferSize)

        then: "channel position updated accordingly"
        copied == dataLength
        channel.position() == dataLength

        when: "channel flushed"
        channel.close()

        then: "appropriate data uploaded"
        blockClient.downloadContent().toBytes() == data

        where:
        streamBufferSize  | copyBufferSize | dataLength
        50                | 40             | Constants.KB
        Constants.KB + 50 | 40             | Constants.KB // initial fetch larger than resource size
    }

    def "Supports greater than maxint blob size"() {
        given: "data"
        long blobSize = Long.MAX_VALUE
        def data = getRandomData(toRead)

        and: "read behavior to blob where length > maxint"
        BlobClientBase client = Mock()
        def behavior = new StorageSeekableByteChannelBlobReadBehavior(client, ByteBuffer.allocate(0), -1, blobSize, null)

        and: "StorageSeekableByteChannel"
        def channel = new StorageSeekableByteChannel(toRead, behavior, 0)

        when: "seek"
        channel.position(offset)

        then: "position set"
        channel.position() == offset

        when: "read"
        ByteBuffer readBuffer = ByteBuffer.allocate(toRead)
        int read = channel.read(readBuffer)

        then: "appropriate data read"
        read == toRead
        readBuffer.array()[0..read-1] == data.array()[0..read-1]
        1 * client.downloadStreamWithResponse(_,
            { BlobRange r -> r != null && r.getOffset() == offset && r.getCount() == toRead as long },
            _, _, _, _, _) >> {
                OutputStream os, BlobRange range, DownloadRetryOptions options, BlobRequestConditions conditions,
                boolean md5, Duration timeout, Context context ->
                    os.write(data.array())
                    def contentRange = "bytes $offset-${offset + toRead - 1}/$blobSize" as String
                    return new BlobDownloadResponse(new BlobDownloadAsyncResponse(null, 206,
                        new HttpHeaders([("Content-Range"): contentRange] as Map<String, String>), null,
                        new BlobDownloadHeaders().setContentRange(contentRange)))
            }
        0 * client.downloadStreamWithResponse(_, _, _, _, _, _, _) >> { throw new RuntimeException("Incorrect parameters") }

        and: "channel position updated"
        channel.position() == offset + toRead

        where:
        toRead | offset
        1024   | Integer.MAX_VALUE as long
        1024   | Integer.MAX_VALUE as long + 1000
        1024   | Long.MAX_VALUE / 2 as long
    }

    def "Client creates appropriate channel readmode"() {
        setup:
        def versionedCC = versionedBlobServiceClient.getBlobContainerClient(generateContainerName())
        versionedCC.create()
        bc = versionedCC.getBlobClient(generateBlobName())

        when: "make channel in read mode"
        bc.upload(BinaryData.fromBytes(getRandomByteArray(1024)))
        def channel = bc.openSeekableByteChannelRead(new BlobSeekableByteChannelReadOptions()
            .setRequestConditions(conditions).setBlockSize(blockSize).setConsistentReadControl(control)
            .setInitialPosition(position), null) as StorageSeekableByteChannel

        then: "channel WriteBehavior is null"
        channel.getWriteBehavior() == null

        and: "channel ReadBehavior has appropriate values"
        def readBehavior = channel.getReadBehavior() as StorageSeekableByteChannelBlobReadBehavior
        readBehavior.client != null
        if (conditions != null) {
            assert readBehavior.requestConditions == conditions
        }
        if (control == null || control == ConsistentReadControl.ETAG) {
            assert readBehavior.requestConditions != null
            assert readBehavior.requestConditions.getIfMatch() != null
        } else if (control == ConsistentReadControl.VERSION_ID) {
            assert readBehavior.client != bc
            assert readBehavior.client.versionId != null
            assert readBehavior.requestConditions == conditions
        } else {
            assert readBehavior.requestConditions == conditions
        }

        and: "channel has appropriate values"
        channel.chunkSize == (blockSize == null ? 4 * Constants.MB : blockSize)
        channel.position() == (position == null ? 0 : position)

        where:
        conditions                  | blockSize | control                          | position
        null                        | null      | null                             | null
        new BlobRequestConditions() | null      | ConsistentReadControl.NONE       | null
        new BlobRequestConditions() | null      | ConsistentReadControl.ETAG       | null
        new BlobRequestConditions() | null      | ConsistentReadControl.VERSION_ID | null
        null                        | 500       | null                             | null
        null                        | null      | ConsistentReadControl.NONE       | null
        null                        | null      | ConsistentReadControl.ETAG       | null
        null                        | null      | ConsistentReadControl.VERSION_ID | null
        null                        | null      | null                             | 800
    }

    def "Client creates appropriate channel writemode - block"() {
        when: "make channel in write mode"
        def channel = blockClient.openSeekableByteChannelWrite(
            new BlockBlobSeekableByteChannelWriteOptions(writeMode).setChunkSize(blockSize).setHeaders(headers)
                .setMetadata(metadata).setTags(tags).setTier(tier).setRequestConditions(conditions)
        ) as StorageSeekableByteChannel

        then: "channel ReadBehavior is null"
        channel.getReadBehavior() == null

        and: "channel WriteBehavior has appropriate values"
        def writeBehavior = channel.getWriteBehavior() as StorageSeekableByteChannelBlockBlobWriteBehavior
        writeBehavior.getWriteMode() == StorageSeekableByteChannelBlockBlobWriteBehavior.WriteMode.valueOf(writeMode.toString())
        writeBehavior.getHeaders() == headers
        writeBehavior.getMetadata() == metadata
        writeBehavior.getTags() == tags
        writeBehavior.getTier() == tier
        writeBehavior.getRequestConditions() == conditions

        and: "channel has appropriate values"
        channel.chunkSize == (blockSize == null ? 4 * Constants.MB : blockSize)
        channel.position() == 0

        where:
        writeMode           | blockSize | headers               | metadata     | tags         | tier            | conditions
        WriteMode.OVERWRITE | null      | null                  | null         | null         | null            | null
        WriteMode.OVERWRITE | 500       | null                  | null         | null         | null            | null
        WriteMode.OVERWRITE | null      | new BlobHttpHeaders() | null         | null         | null            | null
        WriteMode.OVERWRITE | null      | null                  | [foo: "bar"] | null         | null            | null
        WriteMode.OVERWRITE | null      | null                  | null         | [foo: "bar"] | null            | null
        WriteMode.OVERWRITE | null      | null                  | null         | null         | AccessTier.COOL | null
        WriteMode.OVERWRITE | null      | null                  | null         | null         | null            | new BlobRequestConditions()
    }
}
