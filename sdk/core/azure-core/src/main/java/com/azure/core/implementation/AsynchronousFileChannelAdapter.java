// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadPendingException;
import java.nio.channels.WritePendingException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Adapts {@link AsynchronousFileChannel} to {@link AsynchronousByteChannel}
 */
public class AsynchronousFileChannelAdapter implements AsynchronousByteChannel {

    private static final ClientLogger LOGGER = new ClientLogger(AsynchronousFileChannelAdapter.class);

    private final AsynchronousFileChannel fileChannel;

    private static final AtomicLongFieldUpdater<AsynchronousFileChannelAdapter> POSITION_ATOMIC_UPDATER =
        AtomicLongFieldUpdater.newUpdater(AsynchronousFileChannelAdapter.class, "position");
    private volatile long position;

    // AsynchronousByteChannel implementation may disallow concurrent reads and writes.
    private static final AtomicReferenceFieldUpdater<AsynchronousFileChannelAdapter, Operation> PENDING_OPERATION_ATOMIC_UPDATER =
        AtomicReferenceFieldUpdater.newUpdater(
            AsynchronousFileChannelAdapter.class, Operation.class, "pendingOperation");
    private volatile Operation pendingOperation = null;

    public AsynchronousFileChannelAdapter(AsynchronousFileChannel fileChannel, long position) {
        this.fileChannel = Objects.requireNonNull(fileChannel);
        this.position = position;
    }

    @Override
    public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
        beginOperation(Operation.READ);
        fileChannel.read(dst, POSITION_ATOMIC_UPDATER.get(this), attachment,
            new DelegatingCompletionHandler<>(handler, Operation.READ));
    }

    @Override
    public Future<Integer> read(ByteBuffer dst) {
        beginOperation(Operation.READ);
        CompletableFuture<Integer> future = new CompletableFuture<>();
        fileChannel.read(dst, POSITION_ATOMIC_UPDATER.get(this), dst,
            new DelegatingCompletionHandler<>(future, Operation.READ));
        return future;
    }

    @Override
    public <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
        beginOperation(Operation.WRITE);
        fileChannel.write(src, POSITION_ATOMIC_UPDATER.get(this), attachment,
            new DelegatingCompletionHandler<>(handler, Operation.WRITE));
    }

    @Override
    public Future<Integer> write(ByteBuffer src) {
        beginOperation(Operation.WRITE);
        CompletableFuture<Integer> future = new CompletableFuture<>();
        fileChannel.write(src, POSITION_ATOMIC_UPDATER.get(this), src,
            new DelegatingCompletionHandler<>(future, Operation.WRITE));
        return future;
    }

    @Override
    public boolean isOpen() {
        return fileChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        fileChannel.close();
    }

    private void beginOperation(Operation operation) {
        if (!PENDING_OPERATION_ATOMIC_UPDATER.compareAndSet(this, null, operation)) {
            switch (PENDING_OPERATION_ATOMIC_UPDATER.get(this)) {
                case READ:
                    throw LOGGER.logExceptionAsError(new ReadPendingException());
                case WRITE:
                    throw LOGGER.logExceptionAsError(new WritePendingException());
                default:
                    throw LOGGER.logExceptionAsError(new IllegalStateException("Unknown channel operation"));
            }
        }
    }

    private void endOperation(Operation operation) {
        if (!PENDING_OPERATION_ATOMIC_UPDATER.compareAndSet(this, operation, null)) {
            throw new IllegalStateException("There's no pending " + operation);
        }
    }

    private enum Operation {
        READ, WRITE
    }

    private final class DelegatingCompletionHandler<T> implements CompletionHandler<Integer, T> {
        private final CompletionHandler<Integer, ? super T> handler;
        private final CompletableFuture<Integer> future;
        private final Operation operation;

        private DelegatingCompletionHandler(CompletionHandler<Integer, ? super T> handler, Operation operation) {
            this.handler = handler;
            this.future = null;
            this.operation = operation;
        }

        private DelegatingCompletionHandler(CompletableFuture<Integer> future, Operation operation) {
            this.handler = null;
            this.future = future;
            this.operation = operation;
        }

        @Override
        public void completed(Integer result, T attachment) {
            if (result > 0) {
                POSITION_ATOMIC_UPDATER.addAndGet(AsynchronousFileChannelAdapter.this, result);
            }
            endOperation(this.operation);
            if (handler != null) {
                handler.completed(result, attachment);
            } else if (future != null) {
                future.complete(result);
            }
        }

        @Override
        public void failed(Throwable exc, T attachment) {
            endOperation(this.operation);
            if (handler != null) {
                handler.failed(exc, attachment);
            } else if (future != null) {
                future.completeExceptionally(exc);
            }
        }
    }
}
