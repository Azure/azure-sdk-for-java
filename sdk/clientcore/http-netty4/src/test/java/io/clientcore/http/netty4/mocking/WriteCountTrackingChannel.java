// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.mocking;

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

        outputStream.write(byteBufferToArray(src));

        return remaining;
    }

    private static byte[] byteBufferToArray(ByteBuffer byteBuffer) {
        int length = byteBuffer.remaining();
        byte[] byteArray = new byte[length];
        byteBuffer.get(byteArray);
        return byteArray;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() {

    }
}
