// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.ProgressReporter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public class ByteCountingOutputStream extends OutputStream {

    private final OutputStream outputStream;
    private final ProgressReporter progressReporter;

    private static final AtomicLongFieldUpdater<ByteCountingOutputStream> BYTES_WRITTEN_ATOMIC_UPDATER =
        AtomicLongFieldUpdater.newUpdater(ByteCountingOutputStream.class, "bytesWritten");
    private volatile long bytesWritten;

    public ByteCountingOutputStream(OutputStream outputStream, ProgressReporter progressReporter) {
        this.outputStream = outputStream;
        this.progressReporter = progressReporter;
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
        BYTES_WRITTEN_ATOMIC_UPDATER.addAndGet(this, 1);
        if (progressReporter != null) {
            progressReporter.reportProgress(1);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
        BYTES_WRITTEN_ATOMIC_UPDATER.addAndGet(this, len - off);
        if (progressReporter != null) {
            progressReporter.reportProgress(len - off);
        }
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    public long getBytesWritten() {
        return BYTES_WRITTEN_ATOMIC_UPDATER.get(this);
    }


}
