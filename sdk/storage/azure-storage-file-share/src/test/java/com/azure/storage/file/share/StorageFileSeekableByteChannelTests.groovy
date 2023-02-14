package com.azure.storage.file.share

import com.azure.core.http.HttpHeaders
import com.azure.storage.common.StorageChannelMode
import com.azure.storage.common.StorageSeekableByteChannel
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.TestUtility
import com.azure.storage.file.share.models.FileLastWrittenMode
import com.azure.storage.file.share.models.ShareFileDownloadAsyncResponse
import com.azure.storage.file.share.models.ShareFileDownloadHeaders
import com.azure.storage.file.share.models.ShareFileDownloadResponse
import com.azure.storage.file.share.models.ShareRequestConditions
import com.azure.storage.file.share.options.ShareFileDownloadOptions
import com.azure.storage.file.share.options.ShareFileSeekableByteChannelOptions
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream
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

    ShareFileDownloadResponse createMockDownloadResponse(String contentRange) {
        String contentRangeHeader = "Content-Range"
        return new ShareFileDownloadResponse(new ShareFileDownloadAsyncResponse(null, 206,
            new HttpHeaders([(contentRangeHeader): contentRange] as Map<String, String>), null,
            new ShareFileDownloadHeaders().setContentRange(contentRange)))
    }

    @Unroll
    def "WriteBehavior write calls to client correctly"() {
        given:
        ShareFileClient client = Mock()
        def behavior = new StorageSeekableByteChannelShareFileWriteBehavior(client, conditions, lastWrittenMode)

        when: "WriteBehavior.write() called"
        behavior.write(getData().getDefaultData(), offset)

        then: "Expected ShareFileClient upload parameters given"
        1 * client.uploadRangeWithResponse(
            { it.getOffset() == offset && it.getRequestConditions() == conditions && it.getLastWrittenMode() == lastWrittenMode
              && (it.getDataStream() as ByteBufferBackedInputStream).getBytes() == getData().getDefaultBytes()},
            null, null)

        where:
        offset | conditions                   | lastWrittenMode
        0      | null                         | null
        50     | null                         | null
        0      | new ShareRequestConditions() | null
        0      | null                         | FileLastWrittenMode.PRESERVE
    }

    @Unroll
    def "ReadBehavior read calls to client correctly"() {
        given:
        ShareFileClient client = Mock()
        def behavior = new StorageSeekableBytechannelShareFileReadBehavior(client, conditions)
        def buffer = ByteBuffer.allocate(Constants.KB);
        def expectedResponseContentRange = "bytes $offset-${offset + buffer.limit() - 1}/4096"

        when: "ReadBehavior.read() called"
        behavior.read(buffer, offset)

        then: "Expected ShareFileClient download parameters given"
        1 * client.downloadWithResponse(
            _,
            { ShareFileDownloadOptions options -> options.getRange().getStart() == offset &&
                options.getRange().getEnd() == offset + buffer.remaining() - 1 &&
                options.getRequestConditions() == conditions },
            null, null) >> { createMockDownloadResponse(expectedResponseContentRange) }

        where:
        offset | conditions
        0      | null
        50     | null
        0      | new ShareRequestConditions()
    }

    def "E2E channel write"() {
        given:
        def data = getRandomByteArray(1024)
        def channel = new StorageSeekableByteChannel(50, StorageChannelMode.WRITE, null,
            new StorageSeekableByteChannelShareFileWriteBehavior(primaryFileClient, null, null))
        primaryFileClient.create(data.length)

        when: "write to channel"
        int copied = TestUtility.copy(new ByteArrayInputStream(data), channel, 50)

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
        def readBehavior = channel.getReadBehavior() as StorageSeekableBytechannelShareFileReadBehavior
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
        def readBehavior = channel.getReadBehavior() as StorageSeekableBytechannelShareFileReadBehavior
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
