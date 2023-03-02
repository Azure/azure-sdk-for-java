package com.azure.storage.blob.specialized

import com.azure.core.util.BinaryData
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.ConsistentReadControl
import com.azure.storage.blob.options.BlobSeekableByteChannelReadOptions
import com.azure.storage.common.StorageSeekableByteChannel
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.TestUtility

class BlobSeekableByteChannelTests extends APISpec {
    BlobClient bc

    def setup() {
        bc = cc.getBlobClient(generateBlobName())
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

    def "Client creates appropriate channel readmode"() {
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
}
