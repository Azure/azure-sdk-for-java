// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation.simple;

import com.azure.core.util.BinaryData;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class InMemoryBodyCollector implements SimpleBodyCollector {

    private volatile ByteArrayOutputStream outputStream;

    @Override
    public void collect(ByteBuf buffer) {
        if (buffer.isReadable()) {
            ensureStream();
            try {
                buffer.readBytes(outputStream, buffer.readableBytes());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void ensureStream() {
        if (outputStream == null) {
            synchronized (this) {
                if (outputStream == null) {
                    outputStream = new ByteArrayOutputStream();
                }
            }
        }
    }

    @Override
    public BinaryData toBinaryData() {
        if (outputStream == null || outputStream.size() == 0) {
            return null;
        }
        return BinaryData.fromBytes(outputStream.toByteArray());
    }
}
