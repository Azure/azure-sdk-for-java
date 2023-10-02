// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation;

import com.typespec.core.util.ProgressReporter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Count bytes written and read to the target channel.
 */
public class ByteCountingAsynchronousByteChannel implements AsynchronousByteChannel {

    private final AsynchronousByteChannel channel;
    private final ProgressReporter readProgressReporter;
    private final ProgressReporter writeProgressReporter;

    private static final AtomicLongFieldUpdater<ByteCountingAsynchronousByteChannel> BYTES_WRITTEN_ATOMIC_UPDATER =
        AtomicLongFieldUpdater.newUpdater(ByteCountingAsynchronousByteChannel.class, "bytesWritten");
    private volatile long bytesWritten;
    private static final AtomicLongFieldUpdater<ByteCountingAsynchronousByteChannel> BYTES_READ_ATOMIC_UPDATER =
        AtomicLongFieldUpdater.newUpdater(ByteCountingAsynchronousByteChannel.class, "bytesRead");
    private volatile long bytesRead;

    public ByteCountingAsynchronousByteChannel(
        AsynchronousByteChannel channel,
        ProgressReporter readProgressReporter,
        ProgressReporter writeProgressReporter) {
        this.channel = Objects.requireNonNull(channel, "'channel' must not be null");
        this.readProgressReporter = readProgressReporter;
        this.writeProgressReporter = writeProgressReporter;
    }

    @Override
    public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
        this.channel.read(dst, attachment,
            new DelegatingCompletionHandler<A>(handler, BYTES_READ_ATOMIC_UPDATER, readProgressReporter));
    }

    @Override
    public Future<Integer> read(ByteBuffer dst) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        channel.read(dst, dst,
            new DelegatingCompletionHandler<>(future, BYTES_READ_ATOMIC_UPDATER, readProgressReporter));
        return future;
    }

    @Override
    public <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
        // We're implementing channel interface here, i.e. we don't have to consume whole buffer in one shot.
        // Caller is responsible for that.
        this.channel.write(src, attachment,
            new DelegatingCompletionHandler<A>(handler, BYTES_WRITTEN_ATOMIC_UPDATER, writeProgressReporter));
    }

    @Override
    public Future<Integer> write(ByteBuffer src) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        // We're implementing channel interface here, i.e. we don't have to consume whole buffer in one shot.
        // Caller is responsible for that.
        channel.write(src, src,
            new DelegatingCompletionHandler<>(future, BYTES_WRITTEN_ATOMIC_UPDATER, writeProgressReporter));
        return future;
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    public long getBytesWritten() {
        return BYTES_WRITTEN_ATOMIC_UPDATER.get(this);
    }

    public long getBytesRead() {
        return BYTES_READ_ATOMIC_UPDATER.get(this);
    }

    private final class DelegatingCompletionHandler<T> implements CompletionHandler<Integer, T> {
        private final CompletionHandler<Integer, ? super T> handler;
        private final CompletableFuture<Integer> future;
        private final AtomicLongFieldUpdater<ByteCountingAsynchronousByteChannel> atomicLongFieldUpdater;
        private final ProgressReporter progressReporter;

        private DelegatingCompletionHandler(
            CompletionHandler<Integer, ? super T> handler,
            AtomicLongFieldUpdater<ByteCountingAsynchronousByteChannel> atomicLongFieldUpdater,
            ProgressReporter progressReporter) {
            this.handler = handler;
            this.future = null;
            this.atomicLongFieldUpdater = atomicLongFieldUpdater;
            this.progressReporter = progressReporter;
        }

        private DelegatingCompletionHandler(
            CompletableFuture<Integer> future,
            AtomicLongFieldUpdater<ByteCountingAsynchronousByteChannel> atomicLongFieldUpdater,
            ProgressReporter progressReporter) {
            this.handler = null;
            this.future = future;
            this.atomicLongFieldUpdater = atomicLongFieldUpdater;
            this.progressReporter = progressReporter;
        }

        @Override
        public void completed(Integer result, T attachment) {
            if (result > 0) {
                atomicLongFieldUpdater.addAndGet(ByteCountingAsynchronousByteChannel.this, result);
                if (progressReporter != null) {
                    progressReporter.reportProgress(result);
                }
            }
            if (handler != null) {
                handler.completed(result, attachment);
            } else if (future != null) {
                future.complete(result);
            }
        }

        @Override
        public void failed(Throwable exc, T attachment) {
            if (handler != null) {
                handler.failed(exc, attachment);
            } else if (future != null) {
                future.completeExceptionally(exc);
            }
        }
    }
}
