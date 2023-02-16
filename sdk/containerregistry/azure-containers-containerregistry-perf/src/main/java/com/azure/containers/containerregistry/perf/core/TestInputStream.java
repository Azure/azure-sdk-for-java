// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.perf.core;

import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.CHUNK_SIZE;

public class TestInputStream extends InputStream {
    private static final Random RANDOM = new Random(42);
    private static final byte [] CHUNK = new byte[CHUNK_SIZE];
    static {
        RANDOM.nextBytes(CHUNK);
    }

    private final long size;
    private long position = 0;

    public TestInputStream(long size) {
        this.size = size;
    }

    @Override
    public int read() {
        position++;
        return position == size ? -1 : CHUNK[(int) (position % (long) CHUNK.length)];
    }

    @Override
    public int read(byte b[], int off, int len) {
        if (len == 0) {
            return 0;
        }
        if (off >= size || position >= size) {
            return -1;
        }

        int offAfter = off + len;
        long startPos = position;
        for (; off < offAfter && position < size; position++, off++) {
            b[off] = CHUNK[(int) (position % (long) CHUNK.length)];
        }

        return (int) (position - startPos);
    }


    @Override
    public int available() {
        return (int) Math.min(Integer.MAX_VALUE, size - position);
    }

    public static Flux<ByteBuffer> generateAsyncStream(long size) {
        return Flux.generate(() -> 0L, (pos, sink) -> {
            long remaining = size - pos;
            if (remaining <= 0) {
                sink.complete();
                return size;
            }

            ByteBuffer buffer = ByteBuffer.wrap(CHUNK);
            if (remaining < CHUNK.length) {
                buffer.limit((int)remaining);
            }
            sink.next(buffer);

            return pos + CHUNK.length;
        });
    }
}
