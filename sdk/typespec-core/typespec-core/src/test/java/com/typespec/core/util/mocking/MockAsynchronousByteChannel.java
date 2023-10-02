// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.mocking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;

/**
 * An asynchronous byte channel used for mocking in tests.
 */
public class MockAsynchronousByteChannel implements AsynchronousByteChannel {
    @Override
    public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {

    }

    @Override
    public Future<Integer> read(ByteBuffer dst) {
        return null;
    }

    @Override
    public <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {

    }

    @Override
    public Future<Integer> write(ByteBuffer src) {
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
