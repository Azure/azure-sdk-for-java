// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.stress;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.perf.test.core.RepeatingInputStream;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.zip.CRC32;

public class CrcInputStream extends InputStream {
    private final static ClientLogger LOGGER = new ClientLogger(CrcInputStream.class);
    private final Sinks.One<ContentInfo> sink = Sinks.one();
    private final InputStream inputStream;
    private final CRC32 crc = new CRC32();
    private final byte[] head = new byte[1024];
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
                if (length < head.length) {
                    head[(int) length] = (byte) b;
                }
                length++;
            }
            if (b == -1) {
                sink.emitValue(new ContentInfo(crc.getValue(), length, head), Sinks.EmitFailureHandler.FAIL_FAST);
            }
            return b;
        } catch (IOException e) {
            sink.emitError(e, Sinks.EmitFailureHandler.FAIL_FAST);
            throw LOGGER.logThrowableAsError(e);
        }
    }

    @Override
    public synchronized int read(byte b[], int off, int len) throws IOException {
        try {
            int read = inputStream.read(b, off, len);
            if (read > 0) {
                crc.update(b, off, read);
                if (length < head.length) {
                    System.arraycopy(b, off, head, (int)length, Math.min(read, head.length - (int)length));
                }
                length += read;
            }
            if (read == -1) {
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
