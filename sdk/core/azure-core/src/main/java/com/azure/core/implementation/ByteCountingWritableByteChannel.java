// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.ProgressReporter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Count bytes written to the target channel.
 */
public class ByteCountingWritableByteChannel implements WritableByteChannel {

    private final WritableByteChannel channel;
    private final AtomicLong bytesWritten = new AtomicLong();
    private final ProgressReporter progressReporter;

    public ByteCountingWritableByteChannel(WritableByteChannel channel, ProgressReporter progressReporter) {
        this.channel = Objects.requireNonNull(channel, "'channel' must not be null");
        this.progressReporter = progressReporter;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int written = channel.write(src);
        bytesWritten.addAndGet(written);
        if (progressReporter != null) {
            progressReporter.reportProgress(written);
        }
        return written;
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
}
