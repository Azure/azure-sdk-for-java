// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.mocking;

import com.azure.core.util.FluxUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class WriteCountTrackingChannel implements WritableByteChannel {
    private final ByteArrayOutputStream outputStream;
    private int writeCount = 0;

    public WriteCountTrackingChannel() {
        this.outputStream = new ByteArrayOutputStream();
    }

    public int getWriteCount() {
        return writeCount;
    }

    public byte[] getDataWritten() {
        return outputStream.toByteArray();
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        writeCount++;
        int remaining = src.remaining();

        outputStream.write(FluxUtil.byteBufferToArray(src));

        return remaining;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() {

    }
}
