// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.testmodels;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Implementation of {@link AsynchronousFileChannel} used for mocking without Mockito.
 */
public class MockAsynchronousFileChannel extends AsynchronousFileChannel {
    private final byte[] mockData;
    private final int dataLength;
    private final long fileLength;

    public MockAsynchronousFileChannel(byte[] mockData, long fileLength) {
        this.mockData = mockData;
        this.dataLength = mockData.length;
        this.fileLength = fileLength;
    }

    @Override
    public long size() {
        return fileLength;
    }

    @Override
    public AsynchronousFileChannel truncate(long size) {
        return null; // no-op
    }

    @Override
    public void force(boolean metaData) {
        // no-op
    }

    @Override
    public <A> void lock(long position, long size, boolean shared, A attachment,
        CompletionHandler<FileLock, ? super A> handler) {
        // no-op
    }

    @Override
    public Future<FileLock> lock(long position, long size, boolean shared) {
        return null; // no-op
    }

    @Override
    public FileLock tryLock(long position, long size, boolean shared) {
        return null; // no-op
    }

    @Override
    public <A> void read(ByteBuffer dst, long position, A attachment,
        CompletionHandler<Integer, ? super A> handler) {
        if (position >= fileLength) {
            handler.completed(-1, attachment);
            return;
        }

        handler.completed(readInternal(dst, position), attachment);
    }

    @Override
    public Future<Integer> read(ByteBuffer dst, long position) {
        if (position >= fileLength) {
            return CompletableFuture.completedFuture(-1);
        }

        return CompletableFuture.completedFuture(readInternal(dst, position));
    }

    private int readInternal(ByteBuffer dst, long position) {
        int remaining = dst.remaining();
        int actualLength = (int) Math.min(remaining, fileLength - position);
        int bytesOffset = (int) (position % dataLength);

        // Check if we need to wrap back around to the beginning of the bytes.
        if (bytesOffset + actualLength > dataLength) {
            int initial = dataLength - bytesOffset;
            int count = (actualLength - initial) / dataLength;
            int remainder = (actualLength - initial) % dataLength;

            dst.put(mockData, bytesOffset, initial);

            for (int i = 0; i < count; i++) {
                dst.put(mockData);
            }

            if (remainder > 0) {
                dst.put(mockData, 0, remainder);
            }
        } else {
            dst.put(mockData, bytesOffset, actualLength);
        }

        return actualLength;
    }

    @Override
    public <A> void write(ByteBuffer src, long position, A attachment,
        CompletionHandler<Integer, ? super A> handler) {
        // no-op
    }

    @Override
    public Future<Integer> write(ByteBuffer src, long position) {
        return null; // no-op
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() {
        // no-op
    }
}
