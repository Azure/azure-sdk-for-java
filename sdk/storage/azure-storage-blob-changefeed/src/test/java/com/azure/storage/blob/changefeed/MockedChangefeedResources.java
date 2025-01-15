// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.changefeed;

import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.changefeed.implementation.models.InternalBlobChangefeedEvent;
import com.azure.storage.blob.changefeed.implementation.models.InternalBlobChangefeedEventData;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEventData;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEventType;
import com.azure.storage.blob.models.BlobType;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

final class MockedChangefeedResources {
    static Flux<ByteBuffer> readFile(String fileName, Class<?> aClass) {
        try {
            ClassLoader classLoader = aClass.getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            Path path = Paths.get(file.getAbsolutePath());
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            return FluxUtil.readFile(fileChannel);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    static BlobChangefeedEvent getMockBlobChangefeedEvent(int index) {
        return new InternalBlobChangefeedEvent("topic", "subject", BlobChangefeedEventType.BLOB_CREATED,
            OffsetDateTime.of(2020, 4, 5, 6, 30, 0, 0, ZoneOffset.UTC), "id" + index, /* Just to verify later. */
            getMockBlobChangefeedEventData(), 0L, "metadataVersion");
    }

    static BlobChangefeedEventData getMockBlobChangefeedEventData() {
        return new InternalBlobChangefeedEventData("PutBlob", "clientRequestId", "requestId", "etag",
            "application/octet-stream", 100L, BlobType.BLOCK_BLOB, 0L, "destinationUrl", "sourceUrl", "", false,
            "sequencer");
    }

    private MockedChangefeedResources() {
    }
}
