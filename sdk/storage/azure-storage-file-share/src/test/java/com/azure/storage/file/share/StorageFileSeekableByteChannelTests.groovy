// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.storage.common.ParallelTransferOptions

import com.azure.storage.common.implementation.StorageSeekableByteChannel
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.TestUtility
import com.azure.storage.file.share.models.FileLastWrittenMode
import com.azure.storage.file.share.models.ShareRequestConditions
import com.azure.storage.file.share.options.ShareFileSeekableByteChannelReadOptions
import com.azure.storage.file.share.options.ShareFileSeekableByteChannelWriteOptions

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

    def cleanup() {
        shareClient.deleteIfExists()
    }

    def "E2E channel write"() {
        given:
        def streamBufferSize = 50
        def copyBufferSize = 40
        def data = getRandomByteArray(1024)

        when: "Channel initialized"
        def channel = primaryFileClient.getFileSeekableByteChannelWrite(
            new ShareFileSeekableByteChannelWriteOptions(true)
                .setFileSize(data.length).setChunkSizeInBytes(streamBufferSize))

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
        def channel = primaryFileClient.getFileSeekableByteChannelRead(
            new ShareFileSeekableByteChannelReadOptions().setChunkSizeInBytes(streamBufferSize))

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
        def channel = primaryFileClient.getFileSeekableByteChannelWrite(
            new ShareFileSeekableByteChannelWriteOptions(true)
                .setRequestConditions(conditions).setFileLastWrittenMode(lastWrittenMode).setFileSize(Constants.KB))
            as StorageSeekableByteChannel

        then: "channel WriteBehavior has appropriate values"
        def writeBehavior = channel.getWriteBehavior() as StorageSeekableByteChannelShareFileWriteBehavior
        writeBehavior.client == primaryFileClient
        writeBehavior.requestConditions == conditions
        writeBehavior.lastWrittenMode == lastWrittenMode

        and: "channel ReadBehavior is null"
        channel.getReadBehavior() == null

        and: "channel configured correctly"


        where:
        conditions                   | lastWrittenMode
        null                         | null
        new ShareRequestConditions() | null
        null                         | FileLastWrittenMode.PRESERVE
    }

    def "Client creates appropriate channel readmode"() {
        when: "make channel in read mode"
        def channel = primaryFileClient.getFileSeekableByteChannelRead(new ShareFileSeekableByteChannelReadOptions()
            .setRequestConditions(conditions)) as StorageSeekableByteChannel

        then: "channel WriteBehavior is null"
        channel.getWriteBehavior() == null

        and: "channel ReadBehavior has appropriate values"
        def readBehavior = channel.getReadBehavior() as StorageSeekableByteChannelShareFileReadBehavior
        readBehavior.client == primaryFileClient
        readBehavior.requestConditions == conditions

        where:
        _ | conditions
        _ | null
        _ | new ShareRequestConditions()
    }
}
