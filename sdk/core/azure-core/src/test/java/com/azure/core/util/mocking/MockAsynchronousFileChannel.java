// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.mocking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.util.concurrent.Future;

/**
 * An asynchronous file channel used for mocking in tests.
 */
public class MockAsynchronousFileChannel extends AsynchronousFileChannel {

    @Override
    public long size() throws IOException {
        return 0;
    }

    @Override
    public AsynchronousFileChannel truncate(long size) {
        return null;
    }

    @Override
    public void force(boolean metaData) {

    }

    @Override
    public <A> void lock(long position, long size, boolean shared, A attachment,
        CompletionHandler<FileLock, ? super A> handler) {

    }

    @Override
    public Future<FileLock> lock(long position, long size, boolean shared) {
        return null;
    }

    @Override
    public FileLock tryLock(long position, long size, boolean shared) {
        return null;
    }

    @Override
    public <A> void read(ByteBuffer dst, long position, A attachment,
        CompletionHandler<Integer, ? super A> handler) {

    }

    @Override
    public Future<Integer> read(ByteBuffer dst, long position) {
        return null;
    }

    @Override
    public <A> void write(ByteBuffer src, long position, A attachment,
        CompletionHandler<Integer, ? super A> handler) {

    }

    @Override
    public Future<Integer> write(ByteBuffer src, long position) {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void close() throws IOException {

    }
}
