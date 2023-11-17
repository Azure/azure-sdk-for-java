// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.stress;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.zip.CRC32;

public class RandomInputStream extends InputStream {
    private final Random random = new Random();
    private final long length;
    private long position;
    private final CRC32 crc = new CRC32();
    private final ByteBuffer contentHead;

    public RandomInputStream(long length, int keepFirstBytes) {
        this.length = length;
        this.contentHead = ByteBuffer.allocate(keepFirstBytes);
    }

    @Override
    public int read() {
        if (position >= length) {
            return -1;
        }
        position += 1;
        byte b = (byte)random.nextInt(256);
        crc.update(b);
        if (contentHead.hasRemaining()) {
            contentHead.put(b);
        }
        return b;
    }

    @Override
    public int read(byte b[], int off, int len) {
        if (len <= 0) {
            return 0;
        }
        int toRead = (int) Math.min(len, length - position);
        if (toRead <= 0) {
            return -1;
        }
        byte[] bytes = new byte[toRead];
        random.nextBytes(bytes);
        System.arraycopy(bytes, 0, b, off, toRead);
        position += toRead;

        crc.update(bytes, 0, toRead);
        if (contentHead.hasRemaining()) {
            contentHead.put(bytes, 0, (int)Math.min(contentHead.remaining(), toRead));
        }

        return toRead;
    }

    public CRC32 getCrc() {
        return crc;
    }

    public ByteBuffer getContentHead() {
        return contentHead;
    }
}
