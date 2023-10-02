// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.mocking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link AsynchronousFileChannel} used for mocking.
 */
public class MockAsynchronousFileChannel extends AsynchronousFileChannel {
    private static final ScheduledExecutorService WRITER;

    static {
        WRITER = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        Thread hook = new Thread(() -> {
            try {
                WRITER.shutdown();
                if (!WRITER.awaitTermination(2500, TimeUnit.MILLISECONDS)) {
                    WRITER.shutdownNow();
                    WRITER.awaitTermination(2500, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                WRITER.shutdown();
            }
        });
        Runtime.getRuntime().addShutdownHook(hook);
    }

    private final byte[] mockData;
    private final int dataLength;
    private final long fileLength;
    private final Mode mode;

    public MockAsynchronousFileChannel() {
        this.mockData = null;
        this.dataLength = -1;
        this.fileLength = -1;
        this.mode = Mode.NOOP;
    }

    public MockAsynchronousFileChannel(MockFile mockFile) {
        this(mockFile.getData(), mockFile.length());
    }

    public MockAsynchronousFileChannel(byte[] buffer, long fileLength) {
        this.mockData = buffer;
        this.dataLength = mockData.length;
        this.fileLength = fileLength;
        this.mode = Mode.READ;
    }

    public MockAsynchronousFileChannel(byte[] buffer) {
        this.mockData = buffer;
        this.dataLength = 0;
        this.fileLength = 0;
        this.mode = Mode.WRITE;
    }

    @Override
    public long size() throws IOException {
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
        if (mode != Mode.READ) {
            return;
        }

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
        if (mode == Mode.WRITE) {
            WRITER.schedule(() -> handler.completed(writeInternal(src, position), attachment), 1,
                TimeUnit.MICROSECONDS);
        }
    }

    @Override
    public Future<Integer> write(ByteBuffer src, long position) {
        return CompletableFuture.completedFuture(writeInternal(src, position));
    }

    private int writeInternal(ByteBuffer dst, long position) {
        int remaining = dst.remaining();

        dst.get(mockData, (int) position, remaining);

        return remaining;
    }

    @Override
    public boolean isOpen() {
        return mode != Mode.NOOP;
    }

    @Override
    public void close() throws IOException {
        // no-op
    }

    private enum Mode {
        NOOP,
        READ,
        WRITE
    }
}
