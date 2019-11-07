// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.StorageOutputStream;
import com.azure.storage.file.share.models.ShareStorageException;
import java.io.IOException;
import java.nio.ByteBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Provides an output stream to write a given storage file resource.
 */
public class StorageFileOutputStream extends StorageOutputStream {
    private long offsetPos;

    private final ShareFileAsyncClient client;

    StorageFileOutputStream(final ShareFileAsyncClient client, long offsetPos) {
        super(4 * Constants.MB);
        this.client = client;
        this.offsetPos = offsetPos;
    }

    private Mono<Void> uploadData(Flux<ByteBuffer> inputData, long writeLength, long offset) {
        return client.uploadWithResponse(inputData, writeLength, offset)
            .then()
            .onErrorResume(t -> t instanceof IOException || t instanceof ShareStorageException, e -> {
                this.lastError = new IOException(e);
                return null;
            });
    }

    @Override
    protected Mono<Void> dispatchWrite(byte[] data, int writeLength, long offset) {
        if (writeLength == 0) {
            return Mono.empty();
        }

        Flux<ByteBuffer> fbb = Flux.range(0, 1)
            .concatMap(pos -> Mono.fromCallable(() -> ByteBuffer.wrap(data, (int) offset, writeLength)));

        long fileOffset = this.offsetPos;
        this.offsetPos = this.offsetPos + writeLength;

        return this.uploadData(fbb.subscribeOn(Schedulers.elastic()), writeLength, fileOffset);
    }
}
