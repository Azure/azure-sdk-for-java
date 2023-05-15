// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class SyncWrappingAsynchronousByteChannel implements AsynchronousByteChannel {
    private final FileChannel channel;
    private long position;

    public SyncWrappingAsynchronousByteChannel(FileChannel channel, long position) {
        this.channel = channel;
        this.position = position;
    }

    @Override
    public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
        handler.failed(new UnsupportedOperationException("Only writing is supported."), attachment);
    }

    @Override
    public Future<Integer> read(ByteBuffer dst) {
        return CompletableFuture.supplyAsync(() -> {
            throw new UnsupportedOperationException("Only writing is supported.");
        });
    }

    @Override
    public <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
        try {
            handler.completed(sharedWrite(src), attachment);
        } catch (IOException ex) {
            handler.failed(ex, attachment);
        }
    }

    @Override
    public Future<Integer> write(ByteBuffer src) {
        try {
            return CompletableFuture.completedFuture(sharedWrite(src));
        } catch (IOException ex) {
            CompletableFuture<Integer> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(ex);
            return failedFuture;
        }
    }

    private int sharedWrite(ByteBuffer src) throws IOException {
        int totalWritten = 0;
        while (src.hasRemaining()) {
            int written = channel.write(src, position);
            totalWritten += written;
            position += written;
        }

        return totalWritten;
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
