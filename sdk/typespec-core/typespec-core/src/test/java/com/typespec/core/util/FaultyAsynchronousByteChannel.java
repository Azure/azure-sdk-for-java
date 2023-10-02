// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util;

import com.typespec.core.implementation.ByteCountingAsynchronousByteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class FaultyAsynchronousByteChannel implements AsynchronousByteChannel {
    private final ByteCountingAsynchronousByteChannel delegate;
    private final Supplier<IOException> exceptionSupplier;
    private final int maxErrorCount;
    private final AtomicInteger errorEmitted = new AtomicInteger();
    private final long emitAfterOffset;

    public FaultyAsynchronousByteChannel(
        AsynchronousByteChannel delegate, Supplier<IOException> exceptionSupplier, int maxErrorCount, long emitAfterOffset) {
        this.delegate = new ByteCountingAsynchronousByteChannel(delegate, null, null);
        this.exceptionSupplier = exceptionSupplier;
        this.maxErrorCount = maxErrorCount;
        this.emitAfterOffset = emitAfterOffset;
    }


    @Override
    public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (shouldEmitError()) {
            errorEmitted.incrementAndGet();
            handler.failed(exceptionSupplier.get(), attachment);
        } else {
            delegate.read(dst, attachment, handler);
        }
    }

    @Override
    public Future<Integer> read(ByteBuffer dst) {
        if (shouldEmitError()) {
            errorEmitted.incrementAndGet();
            CompletableFuture<Integer> future = new CompletableFuture<>();
            future.completeExceptionally(exceptionSupplier.get());
            return future;
        } else {
            return delegate.read(dst);
        }
    }

    @Override
    public <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (shouldEmitError()) {
            errorEmitted.incrementAndGet();
            handler.failed(exceptionSupplier.get(), attachment);
        } else {
            delegate.write(src, attachment, handler);
        }
    }

    @Override
    public Future<Integer> write(ByteBuffer src) {
        if (shouldEmitError()) {
            errorEmitted.incrementAndGet();
            CompletableFuture<Integer> future = new CompletableFuture<>();
            future.completeExceptionally(exceptionSupplier.get());
            return future;
        } else {
            return delegate.write(src);
        }
    }

    private boolean shouldEmitError() {
        return errorEmitted.get() < maxErrorCount && (delegate.getBytesWritten() >= emitAfterOffset || delegate.getBytesRead() >= emitAfterOffset);
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
