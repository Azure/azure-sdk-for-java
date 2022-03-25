// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation.simple;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;

public class PipedStreamBodyCollector implements SimpleBodyCollector {

    private static final ClientLogger LOGGER = new ClientLogger(PipedStreamBodyCollector.class);

    private final PipedInputStream inputStream;
    private final PipedOutputStream outputStream;

    public PipedStreamBodyCollector() {
        outputStream = new PipedOutputStream();
        try {
            inputStream = new PipedInputStream(outputStream);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public void collect(ByteBuf buffer, boolean isLast) {
        if (buffer.isReadable()) {
            try {
                buffer.readBytes(outputStream, buffer.readableBytes());
                if (isLast) {
                    outputStream.close();
                }
            } catch (IOException e) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
                }
                throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
            }
        } else {
            if (isLast) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
                }
            }
        }
    }

    @Override
    public BinaryData toBinaryData() {
        return BinaryData.fromStream(inputStream);
    }
}
