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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Adapts {@link AsynchronousFileChannel} to {@link AsynchronousByteChannel}
 */
public class AsynchronousFileChannelAdapter implements AsynchronousByteChannel {

    private static final ClientLogger LOGGER = new ClientLogger(AsynchronousFileChannelAdapter.class);

    private final AsynchronousFileChannel fileChannel;
    private final AtomicLong position;

    // AsynchronousByteChannel implementation may disallow concurrent reads and writes.
    private AtomicReference<Operation> pendingOperation = new AtomicReference<>();

    public AsynchronousFileChannelAdapter(AsynchronousFileChannel fileChannel, long position) {
        this.fileChannel = Objects.requireNonNull(fileChannel);
        this.position = new AtomicLong(position);
    }

    @Override
    public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
        beginOperation(Operation.READ);
        fileChannel.read(dst, position.get(), attachment, new CompletionHandler<Integer, A>() {
            @Override
            public void completed(Integer result, A attachment) {
                position.addAndGet(result);
                endOperation(Operation.READ);
                handler.completed(result, attachment);
            }

            @Override
            public void failed(Throwable exc, A attachment) {
                endOperation(Operation.READ);
                handler.failed(exc, attachment);
            }
        });
    }

    @Override
    public Future<Integer> read(ByteBuffer dst) {
        beginOperation(Operation.READ);
        CompletableFuture<Integer> future = new CompletableFuture<>();
        fileChannel.read(dst, position.get(), dst, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                position.addAndGet(result);
                endOperation(Operation.READ);
                future.complete(result);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                endOperation(Operation.READ);
                future.completeExceptionally(exc);
            }
        });
        return future;
    }

    @Override
    public <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
        beginOperation(Operation.WRITE);
        fileChannel.write(src, position.get(), attachment, new CompletionHandler<Integer, A>() {
            @Override
            public void completed(Integer result, A attachment) {
                position.addAndGet(result);
                endOperation(Operation.WRITE);
                handler.completed(result, attachment);
            }

            @Override
            public void failed(Throwable exc, A attachment) {
                endOperation(Operation.WRITE);
                handler.failed(exc, attachment);
            }
        });
    }

    @Override
    public Future<Integer> write(ByteBuffer src) {
        beginOperation(Operation.WRITE);
        CompletableFuture<Integer> future = new CompletableFuture<>();
        fileChannel.write(src, position.get(), src, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                position.addAndGet(result);
                endOperation(Operation.WRITE);
                future.complete(result);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                endOperation(Operation.WRITE);
                future.completeExceptionally(exc);
            }
        });
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
        if (!pendingOperation.compareAndSet(null, operation)) {
            switch (pendingOperation.get()) {
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
        if (!pendingOperation.compareAndSet(operation, null)) {
            throw new IllegalStateException("There's no pending " + operation);
        }
    }

    private enum Operation {
        READ, WRITE
    }
}
