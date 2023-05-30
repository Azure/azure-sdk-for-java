package com.azure.storage.stress;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class RandomInputStream extends InputStream {
    private final Random random = new Random();
    private final long length;
    private long position;

    public RandomInputStream(long length) {
        this.length = length;
    }

    @Override
    public int read() throws IOException {
        if (position >= length) {
            return -1;
        }
        position += 1;
        return random.nextInt(256);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
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
        return toRead;
    }
}
