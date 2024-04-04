// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.stress.utils;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.stress.ContentInfo;
import com.azure.storage.stress.ContentMismatchException;
import com.azure.storage.stress.CrcInputStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Base64;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.toFluxByteBuffer;

public class OriginalContent {
    private final static ClientLogger LOGGER = new ClientLogger(OriginalContent.class);
    private final static Tracer TRACER = TracerProvider.getDefaultProvider().createTracer("unused", null, null, null);
    private static final String FILE_CONTENT_HEAD_STRING = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, "
        + "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. "
        + "Pellentesque elit ullamcorper dignissim cras tincidunt lobortis feugiat vivamus. Massa sapien faucibus et molestie ac feugiat sed lectus. "
        + "Sed pulvinar proin gravida hendrerit.";

    private static final BinaryData FILE_CONTENT_HEAD = BinaryData.fromString(FILE_CONTENT_HEAD_STRING);
    private long dataChecksum = -1;
    private long fileSize = 0;

    public Mono<Void> setupFile(DataLakeFileAsyncClient fileAsyncClient, long fileSize) {
        if (dataChecksum != -1) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("setupFile can't be called again"));
        }
        this.fileSize = fileSize;
        return Mono.using(
                () -> new CrcInputStream(FILE_CONTENT_HEAD, fileSize),
                data -> fileAsyncClient
                    .upload(toFluxByteBuffer(data, 8192),
                        new ParallelTransferOptions().setMaxSingleUploadSizeLong(4 * 1024 * 1024L).setMaxConcurrency(1))
                    .then(data.getContentInfo()),
                CrcInputStream::close)
            .map(info -> dataChecksum = info.getCrc())
            .then();

    }

    public Mono<Void> checkMatch(BinaryData data, Context span) {
        return checkMatch(data.toFluxByteBuffer(), span);
    }

    public Mono<Void> checkMatch(Flux<ByteBuffer> data, Context span) {
        return checkMatch(ContentInfo.fromFluxByteBuffer(data), span);
    }

    public Mono<Void> checkMatch(Mono<ContentInfo> contentInfo, Context span) {
        if (dataChecksum == -1) {
            return monoError(LOGGER, new IllegalStateException("setupFile must complete first"));
        }
        return contentInfo
            .flatMap(info -> {
                if (info.getCrc() != dataChecksum) {
                    logMismatch(info.getCrc(), info.getLength(), info.getHead(), span);
                    return Mono.error(new ContentMismatchException());
                }

                return Mono.empty();
            });
    }

    @SuppressWarnings("try")
    private void logMismatch(long actualCrc, long actualLength, ByteBuffer actualContentHead, Context span) {
        try(AutoCloseable scope = TRACER.makeSpanCurrent(span)) {
            // future: if mismatch, compare against original file
            LOGGER.atError()
                .addKeyValue("expectedCrc", dataChecksum)
                .addKeyValue("actualCrc", actualCrc)
                .addKeyValue("expectedLength", fileSize)
                .addKeyValue("actualLength", actualLength)
                .addKeyValue("actualContentHead", Base64.getEncoder().encode(actualContentHead))
                .log("mismatched crc");
        } catch (Throwable e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    public BinaryData getContentHead() {
        return FILE_CONTENT_HEAD;
    }
}
