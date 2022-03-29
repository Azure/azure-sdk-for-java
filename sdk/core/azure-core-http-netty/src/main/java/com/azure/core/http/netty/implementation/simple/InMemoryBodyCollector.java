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
    public void collect(ByteBuf buffer, boolean isLast) {
        if (buffer.isReadable()) {
            ByteArrayOutputStream result = ensureStream();
            try {
                buffer.readBytes(result, buffer.readableBytes());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private ByteArrayOutputStream ensureStream() {
        ByteArrayOutputStream result = outputStream;
        if (result == null) {
            synchronized (this) {
                result = outputStream;
                if (result == null) {
                    result = new ByteArrayOutputStream();
                    outputStream = result;
                }
            }
        }
        return result;
    }

    @Override
    public BinaryData toBinaryData() {
        if (outputStream == null || outputStream.size() == 0) {
            return null;
        }
        return BinaryData.fromBytes(outputStream.toByteArray());
    }
}
