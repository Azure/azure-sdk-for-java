// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

public class ByteCountingOutputStream extends OutputStream {
    private final OutputStream target;
    private final AtomicLong bytesWritten = new AtomicLong();

    public ByteCountingOutputStream(OutputStream target) {
        this.target = target;
    }

    public long getBytesWritten() {
        return bytesWritten.get();
    }

    @Override
    public void write(int b) throws IOException {
        target.write(b);
        bytesWritten.incrementAndGet();
    }

    @Override
    public void write(byte[] b) throws IOException {
        target.write(b);
        bytesWritten.addAndGet(b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        target.write(b, off, len);
        bytesWritten.addAndGet(len);
    }

    @Override
    public void flush() throws IOException {
        target.flush();
    }

    @Override
    public void close() throws IOException {
        target.close();
    }
}
