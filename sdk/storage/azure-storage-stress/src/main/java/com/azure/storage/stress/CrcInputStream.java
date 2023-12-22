// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.stress;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.perf.test.core.RepeatingInputStream;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class CrcInputStream extends InputStream {
    private final static ClientLogger LOGGER = new ClientLogger(CrcInputStream.class);
    private final Sinks.One<ContentInfo> sink = Sinks.one();
    private final InputStream inputStream;
    private final CRC32 crc = new CRC32();
    private final ByteBuffer head = ByteBuffer.allocate(1024);
    private long length = 0;

    public CrcInputStream(BinaryData source, long size) {
        this.inputStream = new RepeatingInputStream(source, size);
    }

    public CrcInputStream(InputStream source) {
        this.inputStream = source;
    }

    @Override
    public synchronized int read() throws IOException {
        try {
            int b = inputStream.read();
            if (b >= 0) {
                crc.update(b);
                if (head.hasRemaining()) {
                    head.put((byte) b);
                }
                length++;
            } else {
                sink.emitValue(new ContentInfo(crc.getValue(), length, head), Sinks.EmitFailureHandler.FAIL_FAST);
            }
            return b;
        } catch (IOException e) {
            sink.emitError(e, Sinks.EmitFailureHandler.FAIL_FAST);
            throw LOGGER.logThrowableAsError(e);
        }
    }

    @Override
    public synchronized int read(byte buf[], int off, int len) throws IOException {
        try {
            int read = inputStream.read(buf, off, len);
            if (read > 0) {
                length += read;
                crc.update(buf, off, read);
                if (head.hasRemaining()) {
                    head.put(buf, off, Math.min(read, head.remaining()));
                }
            } else {
                sink.emitValue(new ContentInfo(crc.getValue(), length, head), Sinks.EmitFailureHandler.FAIL_FAST);
            }
            return read;
        } catch (IOException e) {
            sink.emitError(e, Sinks.EmitFailureHandler.FAIL_FAST);
            throw LOGGER.logThrowableAsError(e);
        }
    }

    public Mono<ContentInfo> getContentInfo() {
        return sink.asMono();
    }

    @Override
    public void close() {
        try {
            inputStream.close();
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }
}
