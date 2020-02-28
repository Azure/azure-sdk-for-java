// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobStorageException;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Objects;

class PipedStreamSubscriber extends BaseSubscriber<ByteBuffer> {

    private final PipedInputStream in;
    private PipedOutputStream out;
    private final ClientLogger logger;

    PipedStreamSubscriber(PipedInputStream in, ClientLogger logger) {
        Objects.requireNonNull(in, "The input stream must not be null");
        this.in = in;
        this.logger = logger;
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        //change if you want to control back-pressure
        super.hookOnSubscribe(subscription);
        try {
            this.out = new PipedOutputStream(in);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    protected void hookOnNext(ByteBuffer payload) {
        try {
            out.write(FluxUtil.byteBufferToArray(payload));
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    protected void hookOnComplete() {
        close();
    }

    @Override
    protected void hookOnError(Throwable error) {
        close();
        if (error instanceof BlobStorageException) {
            throw logger.logExceptionAsError((BlobStorageException) error);
        }
    }

    @Override
    protected void hookOnCancel() {
        close();
    }

    private void close() {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                throw logger.logExceptionAsError(new UncheckedIOException(e));
            }
        }
    }
}
