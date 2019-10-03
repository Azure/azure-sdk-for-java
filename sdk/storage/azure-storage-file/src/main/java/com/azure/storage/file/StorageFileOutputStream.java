// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.storage.common.Constants;
import com.azure.storage.common.SR;
import com.azure.storage.common.StorageOutputStream;
import com.azure.storage.file.models.StorageException;
import java.io.IOException;
import java.nio.ByteBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class StorageFileOutputStream extends StorageOutputStream {
    private long offsetPos;

    private final FileAsyncClient client;

    StorageFileOutputStream(final FileAsyncClient client, long offsetPos) {
        super(4 * Constants.MB);
        this.client = client;
        this.offsetPos = offsetPos;
    }

    private Mono<Void> uploadData(Flux<ByteBuffer> inputData, long writeLength, long offset) {
        return client.uploadWithResponse(inputData, writeLength, offset)
            .then()
            .onErrorResume(t -> t instanceof IOException || t instanceof StorageException, e -> {
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
