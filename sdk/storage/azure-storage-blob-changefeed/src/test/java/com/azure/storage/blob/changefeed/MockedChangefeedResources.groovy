package com.azure.storage.blob.changefeed

import com.azure.core.util.FluxUtil
import com.azure.storage.blob.changefeed.implementation.models.InternalBlobChangefeedEvent
import com.azure.storage.blob.changefeed.implementation.models.InternalBlobChangefeedEventData
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent
import com.azure.storage.blob.changefeed.models.BlobChangefeedEventData
import com.azure.storage.blob.changefeed.models.BlobChangefeedEventType
import com.azure.storage.blob.models.BlobType
import reactor.core.publisher.Flux

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.OffsetDateTime
import java.time.ZoneOffset

class MockedChangefeedResources {

    static Flux<ByteBuffer> readFile(String fileName, Class aClass) {
        ClassLoader classLoader = aClass.getClassLoader()
        File file = new File(classLoader.getResource(fileName).getFile())
        Path path = Paths.get(file.getAbsolutePath())
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)
        return FluxUtil.readFile(fileChannel)
    }

    static BlobChangefeedEvent getMockBlobChangefeedEvent(int index) {
        return new InternalBlobChangefeedEvent(
            "topic",
            "subject",
            BlobChangefeedEventType.BLOB_CREATED,
            OffsetDateTime.of(2020, 04, 05, 06, 30, 00, 00, ZoneOffset.UTC),
            "id" + index, /* Just to verify later. */
            getMockBlobChangefeedEventData(),
            0,
            "metadataVersion"
        )
    }

    static BlobChangefeedEventData getMockBlobChangefeedEventData() {
        return new InternalBlobChangefeedEventData(
            "PutBlob",
            "clientRequestId",
            "requestId",
            "etag",
            "application/octet-stream",
            100,
            BlobType.BLOCK_BLOB,
            0,
            "destinationUrl",
            "sourceUrl",
            "",
            false,
            "sequencer"
        )
    }
}
