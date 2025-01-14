// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.implementation;

import com.azure.core.v2.util.ProgressReporter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Count bytes written to the target channel.
 */
public class ByteCountingWritableByteChannel implements WritableByteChannel {

    private final WritableByteChannel channel;
    private final ProgressReporter progressReporter;

    private static final AtomicLongFieldUpdater<ByteCountingWritableByteChannel> BYTES_WRITTEN_ATOMIC_UPDATER
        = AtomicLongFieldUpdater.newUpdater(ByteCountingWritableByteChannel.class, "bytesWritten");
    private volatile long bytesWritten;

    /**
     * Creates an instance of {@link ByteCountingWritableByteChannel} that counts bytes written to the target channel.
     *
     * @param channel The {@link WritableByteChannel} to adapt.
     * @param progressReporter The {@link ProgressReporter} to report progress on write operations.
     */
    public ByteCountingWritableByteChannel(WritableByteChannel channel, ProgressReporter progressReporter) {
        this.channel = Objects.requireNonNull(channel, "'channel' must not be null");
        this.progressReporter = progressReporter;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        // We're implementing channel interface here, i.e. we don't have to consume whole buffer in one shot.
        // Caller is responsible for that.
        int written = channel.write(src);
        BYTES_WRITTEN_ATOMIC_UPDATER.addAndGet(this, written);
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

    /**
     * Get the number of bytes written to the target channel.
     *
     * @return the number of bytes written to the target channel.
     */
    public long getBytesWritten() {
        return BYTES_WRITTEN_ATOMIC_UPDATER.get(this);
    }
}
