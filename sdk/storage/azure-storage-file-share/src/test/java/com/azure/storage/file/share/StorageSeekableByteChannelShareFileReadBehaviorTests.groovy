// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.http.HttpHeaders
import com.azure.storage.common.ParallelTransferOptions
import com.azure.storage.common.implementation.Constants
import com.azure.storage.file.share.models.ShareFileDownloadAsyncResponse
import com.azure.storage.file.share.models.ShareFileDownloadHeaders
import com.azure.storage.file.share.models.ShareFileDownloadResponse
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions
import com.azure.storage.file.share.models.ShareRequestConditions
import com.azure.storage.file.share.options.ShareFileDownloadOptions
import spock.lang.Unroll

import java.nio.ByteBuffer

class StorageSeekableByteChannelShareFileReadBehaviorTests extends APISpec {
    ShareFileClient primaryFileClient
    ShareClient shareClient
    String shareName
    String filePath

    def setup() {
        shareName = namer.getRandomName(60)
        filePath = namer.getRandomName(60)
        shareClient = shareBuilderHelper(shareName).buildClient()
        primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient()
    }

    def cleanup() {
        shareClient.deleteIfExists()
    }

    ShareFileDownloadResponse createMockDownloadResponse(String contentRange) {
        String contentRangeHeader = "Content-Range"
        return new ShareFileDownloadResponse(new ShareFileDownloadAsyncResponse(null, 206,
            new HttpHeaders([(contentRangeHeader): contentRange] as Map<String, String>), null,
            new ShareFileDownloadHeaders().setContentRange(contentRange)))
    }

    @Unroll
    def "Read calls to client correctly"() {
        given:
        ShareFileClient client = Mock()
        def behavior = new StorageSeekableByteChannelShareFileReadBehavior(client, conditions)
        def buffer = ByteBuffer.allocate(Constants.KB)

        when: "ReadBehavior.read() called"
        behavior.read(buffer, offset)

        then: "Expected ShareFileClient download parameters given"
        // if this test throws null pointer exceptions, then the condition isn't matching and so the stubbed response is
        // not being appropriately returned to the write behavior
        1 * client.downloadWithResponse(
            _,
            { ShareFileDownloadOptions options -> options.getRange().getStart() == offset &&
                options.getRange().getEnd() == offset + buffer.remaining() - 1 &&
                options.getRequestConditions() == conditions },
            null, null) >> { createMockDownloadResponse("bytes $offset-${offset + buffer.limit() - 1}/4096") }

        where:
        offset | conditions
        0      | null
        50     | null
        0      | new ShareRequestConditions()
    }

    @Unroll
    def "Read graceful past end of file"() {
        given:
        def data = getRandomByteArray(fileSize)
        shareClient.create()
        primaryFileClient.create(fileSize)
        primaryFileClient.upload(new ByteArrayInputStream(data), fileSize, null as ParallelTransferOptions)

        def behavior = new StorageSeekableByteChannelShareFileReadBehavior(primaryFileClient, null)
        def buffer = ByteBuffer.allocate(readSize)

        when: "ReadBehavior.read() called"
        def read = behavior.read(buffer, offset)

        then: "graceful read"
        notThrown(Throwable)

        and: "correct amount read"
        read == expectedRead
        buffer.position() == Math.max(expectedRead, 0) //
        behavior.getCachedLength().longValue() == fileSize

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
        fileSize     | offset            | readSize         | expectedRead
        Constants.KB | 0                 | 2 * Constants.KB | Constants.KB       // read larger than file
        Constants.KB | 500               | Constants.KB     | Constants.KB - 500 // overlap on end of file
        Constants.KB | Constants.KB      | Constants.KB     | -1                 // starts at end of file
        Constants.KB | Constants.KB + 20 | Constants.KB     | -1                 // completely past file
    }

    def "Read detects file growth"() {
        given: "data"
        int length = 512
        def data = getRandomByteArray(2 * length)

        and: "Storage file at half size"
        shareClient.create()
        primaryFileClient.create(length)
        primaryFileClient.upload(new ByteArrayInputStream(data[0..length-1] as byte[]), length, null as ParallelTransferOptions)

        and: "behavior to read file"
        def behavior = new StorageSeekableByteChannelShareFileReadBehavior(primaryFileClient, null)
        def buffer = ByteBuffer.allocate(length)

        when: "entire file initially read"
        def read = behavior.read(buffer, 0)

        then: "channel state as expected"
        read == length
        behavior.cachedLength.longValue() == length

        and: "buffer correctly filled"
        buffer.position() == buffer.capacity()
        buffer.array() as List<Byte> == data[0..length-1]

        when: "read at end of file"
        buffer.clear()
        read = behavior.read(buffer, length)

        then: "gracefully signal end of file"
        notThrown(Throwable)
        read == -1
        behavior.cachedLength.longValue() == length

        and: "buffer unfilled"
        buffer.position() == 0

        when: "file augmented to full size"
        primaryFileClient.setProperties(2 * length, null, null, null)
        primaryFileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(
            new ByteArrayInputStream(data[length..-1] as byte[]), length).setOffset(length), null, null)

        and: "behavior reads at previous EOF"
        buffer.clear()
        read = behavior.read(buffer, behavior.cachedLength.longValue())

        then: "channel state has updated length"
        read == length
        behavior.cachedLength.longValue() == 2 * length

        and: "buffer correctly filled"
        buffer.position() == buffer.capacity()
        buffer.array() as List<Byte> == data[length..-1]
    }

    def "Cached read is used over client"() {
        given:
        def data = getRandomByteArray(Constants.KB)
        ShareFileClient client = Mock()

        def behavior = new StorageSeekableByteChannelShareFileReadBehavior(client, null)
        behavior.setCachedLength(Constants.MB)
        behavior.setCachedReadValue(ByteBuffer.wrap(data), offset)

        when:
        ByteBuffer bb = ByteBuffer.allocate(data.length)
        behavior.read(bb, offset)

        then:
        bb.array() == data
        0 * client._

        where:
        _ | offset
        _ | 0
        _ | 100
    }
}
