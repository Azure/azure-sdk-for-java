package com.azure.storage.file.share

import com.azure.core.http.HttpHeaders
import com.azure.storage.common.ParallelTransferOptions
import com.azure.storage.common.StorageChannelMode
import com.azure.storage.common.StorageSeekableByteChannel
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.TestUtility
import com.azure.storage.file.share.models.FileLastWrittenMode
import com.azure.storage.file.share.models.ShareFileDownloadAsyncResponse
import com.azure.storage.file.share.models.ShareFileDownloadHeaders
import com.azure.storage.file.share.models.ShareFileDownloadResponse
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions
import com.azure.storage.file.share.models.ShareRequestConditions
import com.azure.storage.file.share.options.ShareFileDownloadOptions
import com.azure.storage.file.share.options.ShareFileSeekableByteChannelOptions
import spock.lang.Unroll

import java.nio.ByteBuffer

class StorageFileSeekableByteChannelTests extends APISpec {
    int MAX_PUT_RANGE = 4 * Constants.MB

    ShareFileClient primaryFileClient
    ShareClient shareClient
    String shareName
    String filePath

    def setup() {
        shareName = namer.getRandomName(60)
        filePath = namer.getRandomName(60)
        shareClient = shareBuilderHelper(shareName).buildClient()
        shareClient.create()
        primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient()
    }

    def "E2E channel write"() {
        given:
        def streamBufferSize = 50
        def copyBufferSize = 40
        def data = getRandomByteArray(1024)

        when: "Channel initialized"
        def channel = new StorageSeekableByteChannel(streamBufferSize, StorageChannelMode.WRITE, null,
            new StorageSeekableByteChannelShareFileWriteBehavior(primaryFileClient, null, null))
        primaryFileClient.create(data.length)

        then: "Channel initialized to position zero"
        channel.position() == 0

        when: "write to channel"
        int copied = TestUtility.copy(new ByteArrayInputStream(data), channel, copyBufferSize)

        then: "channel position updated accordingly"
        copied == data.length
        channel.position() == data.length

        when: "data fully flushed to Storage"
        channel.close()

        and: "resource downloaded"
        def downloadedData = new ByteArrayOutputStream()
        primaryFileClient.download(downloadedData)

        then: "expected data downloaded"
        downloadedData.toByteArray() == data
    }

    def "E2E channel read"() {
        given:
        def data = getRandomByteArray(dataLength)
        primaryFileClient.create(dataLength)
        primaryFileClient.upload(new ByteArrayInputStream(data), data.length, null as ParallelTransferOptions)

        when: "Channel initialized"
        def channel = new StorageSeekableByteChannel(streamBufferSize, StorageChannelMode.READ,
            new StorageSeekableByteChannelShareFileReadBehavior(primaryFileClient, null), null)

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
        streamBufferSize | copyBufferSize | dataLength
        50               | 40             | Constants.KB
        2 * Constants.KB | 40             | Constants.KB // initial fetch larger than resource size
    }

    def "Client creates appropriate channel writemode"() {
        when: "make channel in write mode"
        def channel = primaryFileClient.getFileSeekableByteChannel(
            new ShareFileSeekableByteChannelOptions(StorageChannelMode.WRITE)
                .setRequestConditions(conditions).setFileLastWrittenMode(lastWrittenMode)) as StorageSeekableByteChannel

        then: "channel WriteBehavior has appropriate values"
        def writeBehavior = channel.getWriteBehavior() as StorageSeekableByteChannelShareFileWriteBehavior
        writeBehavior.client == primaryFileClient
        writeBehavior.requestConditions == conditions
        writeBehavior.lastWrittenMode == lastWrittenMode

        and: "channel ReadBehavior has appropriate values"
        def readBehavior = channel.getReadBehavior() as StorageSeekableByteChannelShareFileReadBehavior
        readBehavior.client == primaryFileClient
        readBehavior.requestConditions == conditions

        where:
        conditions                   | lastWrittenMode
        null                         | null
        null                         | null
        new ShareRequestConditions() | null
        null                         | FileLastWrittenMode.PRESERVE
    }

    def "Client creates appropriate channel readmode"() {
        when: "make channel in read mode"
        def channel = primaryFileClient.getFileSeekableByteChannel(
            new ShareFileSeekableByteChannelOptions(StorageChannelMode.READ)
                .setRequestConditions(conditions)) as StorageSeekableByteChannel

        then: "channel WriteBehavior has appropriate values"
        def writeBehavior = channel.getWriteBehavior() as StorageSeekableByteChannelShareFileWriteBehavior
        writeBehavior.client == primaryFileClient
        writeBehavior.requestConditions == conditions

        and: "channel ReadBehavior has appropriate values"
        def readBehavior = channel.getReadBehavior() as StorageSeekableByteChannelShareFileReadBehavior
        readBehavior.client == primaryFileClient
        readBehavior.requestConditions == conditions

        where:
        _ | conditions
        _ | null
        _ | new ShareRequestConditions()
    }

    def "Client creates appropriate channel readmode error"() {
        when: "make channel in read mode"
        primaryFileClient.getFileSeekableByteChannel(
            new ShareFileSeekableByteChannelOptions(StorageChannelMode.READ)
                .setFileLastWrittenMode(FileLastWrittenMode.PRESERVE)) as StorageSeekableByteChannel

        then: "invalid parameter"
        thrown(IllegalArgumentException)
    }
}
