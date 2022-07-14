// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.ProgressReporter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Count bytes written and read to the target channel.
 */
public class ByteCountingAsynchronousByteChannel implements AsynchronousByteChannel {

    private final AsynchronousByteChannel channel;
    private final AtomicLong bytesWritten = new AtomicLong();
    private final AtomicLong bytesRead = new AtomicLong();
    private final ProgressReporter progressReporter;

    public ByteCountingAsynchronousByteChannel(AsynchronousByteChannel channel, ProgressReporter progressReporter) {
        this.channel = Objects.requireNonNull(channel, "'channel' must not be null");
        this.progressReporter = progressReporter;
    }

    @Override
    public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
        this.channel.read(dst, attachment, new CompletionHandler<Integer, A>() {
            @Override
            public void completed(Integer result, A attachment) {
                if (result > 0) {
                    bytesRead.addAndGet(result);
                    if (progressReporter != null) {
                        progressReporter.reportProgress(result);
                    }
                }
                handler.completed(result, attachment);
            }

            @Override
            public void failed(Throwable exc, A attachment) {
                handler.failed(exc, attachment);
            }
        });
    }

    @Override
    public Future<Integer> read(ByteBuffer dst) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        channel.read(dst, dst, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result >= 0) {
                    bytesRead.addAndGet(result);
                    if (progressReporter != null) {
                        progressReporter.reportProgress(result);
                    }
                }
                future.complete(result);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                future.completeExceptionally(exc);
            }
        });
        return future;
    }

    @Override
    public <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
        this.channel.write(src, attachment, new CompletionHandler<Integer, A>() {
            @Override
            public void completed(Integer result, A attachment) {
                bytesWritten.addAndGet(result);
                if (progressReporter != null) {
                    progressReporter.reportProgress(result);
                }
                handler.completed(result, attachment);
            }

            @Override
            public void failed(Throwable exc, A attachment) {
                handler.failed(exc, attachment);
            }
        });
    }

    @Override
    public Future<Integer> write(ByteBuffer src) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        channel.write(src, src, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                bytesWritten.addAndGet(result);
                if (progressReporter != null) {
                    progressReporter.reportProgress(result);
                }
                future.complete(result);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                future.completeExceptionally(exc);
            }
        });
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
        return bytesWritten.get();
    }

    public long getBytesRead() {
        return bytesRead.get();
    }
}
