// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ByteBufferOutputStream extends OutputStream {

    private final AppInsightsByteBufferPool byteBufferPool;

    private final List<ByteBuffer> byteBuffers = new ArrayList<>();

    private ByteBuffer current;

    public ByteBufferOutputStream(AppInsightsByteBufferPool byteBufferPool) {
        this.byteBufferPool = byteBufferPool;
        current = byteBufferPool.remove();
        byteBuffers.add(current);
    }

    @Override
    public void write(int b) {
        ensureSomeCapacity();
        current.put((byte) b);
    }

    @Override
    public void write(byte[] bytes, int off, int len) {
        ensureSomeCapacity();
        int numBytesWritten = Math.min(current.remaining(), len);
        current.put(bytes, off, numBytesWritten);
        if (numBytesWritten < len) {
            write(bytes, off + numBytesWritten, len - numBytesWritten);
        }
    }

    void ensureSomeCapacity() {
        if (current.remaining() > 0) {
            return;
        }
        current = byteBufferPool.remove();
        byteBuffers.add(current);
    }

    public List<ByteBuffer> getByteBuffers() {
        return byteBuffers;
    }
}
