// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress.utils;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.stress.ContentInfo;
import com.azure.storage.stress.CrcInputStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

import static com.azure.core.util.FluxUtil.monoError;

public class OriginalContent {
    private final static ClientLogger LOGGER = new ClientLogger(OriginalContent.class);
    private final static Tracer TRACER = TracerProvider.getDefaultProvider().createTracer("unused", null, null, null);
    private static final String BLOB_CONTENT_HEAD_STRING = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, "
        + "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. "
        + "Pellentesque elit ullamcorper dignissim cras tincidunt lobortis feugiat vivamus. Massa sapien faucibus et molestie ac feugiat sed lectus. "
        + "Sed pulvinar proin gravida hendrerit.";

    private static final BinaryData BLOB_CONTENT_HEAD = BinaryData.fromString(BLOB_CONTENT_HEAD_STRING);
    private long dataChecksum = -1;
    private long blobSize = 0;

    public OriginalContent() {
    }

    public Mono<Void> setupBlob(BlobAsyncClient blobClient, long blobSize) {
        if (dataChecksum != -1) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("setupBlob can't be called again"));
        }

        this.blobSize = blobSize;
        return Mono.using(
                () -> new CrcInputStream(BLOB_CONTENT_HEAD, blobSize),
                data -> blobClient
                        .upload(BinaryData.fromStream(data, blobSize))
                        .then(data.getContentInfo())
                        .doOnSuccess(info -> dataChecksum = info.getCrc()),
                CrcInputStream::close)
            .then();
    }

    public Mono<Boolean> checkMatch(BinaryData data, Context span) {
        return checkMatch(data.toFluxByteBuffer(), span);
    }

    public Mono<Boolean> checkMatch(Flux<ByteBuffer> data, Context span) {
        return checkMatch(ContentInfo.fromFluxByteBuffer(data), span);
    }

    public Mono<Boolean> checkMatch(Mono<ContentInfo> contentInfo, Context span) {
        if (dataChecksum == -1) {
            return monoError(LOGGER, new IllegalStateException("setupBlob must complete first"));
        }
        return contentInfo
                .map(info -> {
                    if (info.getCrc() != dataChecksum) {
                        logMismatch(info.getCrc(), info.getLength(), info.getHead(), span);
                        return false;
                    }

                    return true;
                });
    }

    private void logMismatch(long actualCrc, long actualLength, byte[] actualContentHead, Context span) {
        try(AutoCloseable scope = TRACER.makeSpanCurrent(span)) {
            // future: if mismatch, compare against original file
            LOGGER.atError()
                    .addKeyValue("expectedCrc", dataChecksum)
                    .addKeyValue("actualCrc", actualCrc)
                    .addKeyValue("expectedLength", blobSize)
                    .addKeyValue("actualLength", actualLength)
                    .addKeyValue("actualContentHead", new String(actualContentHead, 0, (int)Math.min(1024, actualLength), StandardCharsets.UTF_8))
                    .log("mismatched crc");
        } catch (Throwable e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}
