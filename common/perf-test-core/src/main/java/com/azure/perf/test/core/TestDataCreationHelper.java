// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Utility class to help with data creation for perf testing.
 */
public class TestDataCreationHelper {
    private static final int RANDOM_BYTES_LENGTH = 1024 * 1024; // 1MB
    private static final byte[] RANDOM_BYTES;
    private static final ByteBuffer RANDOM_BYTE_BUFFER;
    private static final int SIZE = (1024 * 1024 * 1024) + 1;

    static {
        Random random = new Random(0);
        RANDOM_BYTES = new byte[RANDOM_BYTES_LENGTH];
        random.nextBytes(RANDOM_BYTES);
        RANDOM_BYTE_BUFFER = ByteBuffer.wrap(TestDataCreationHelper.RANDOM_BYTES).asReadOnlyBuffer();
    }

    /**
     * Creates a {@link Flux} of {@code size} with repeated values of {@code byteBuffer}.
     *
     * @param byteBuffer the byteBuffer to create Flux from.
     * @param size the size of the flux to create.
     * @return The created {@link Flux}
     */
    @SuppressWarnings("cast")
    public static Flux<ByteBuffer> createCircularByteBufferFlux(ByteBuffer byteBuffer, long size) {
        int remaining = byteBuffer.remaining();

        int quotient = (int) size / remaining;
        int remainder = (int) size % remaining;

        return Flux.range(0, quotient)
            .map(i -> byteBuffer.duplicate())
            .concatWithValues((ByteBuffer) byteBuffer.duplicate().limit(remainder));
    }

    /**
     * Creates a random flux of specified size.
     *
     * @param size the size of the stream
     * @return the {@link Flux} of {@code size}
     */
    public static Flux<ByteBuffer> createRandomByteBufferFlux(long size) {
        return createCircularByteBufferFlux(RANDOM_BYTE_BUFFER, size);
    }

    /**
     * Creates a random stream of specified size.
     *
     * @param size the size of the stream
     * @return the {@link InputStream} of {@code size}
     * @throws IllegalArgumentException if {@code size} is more than {@link #SIZE}
     */
    public static InputStream createRandomInputStream(long size) {
        if (size > SIZE) {
            throw new IllegalArgumentException("size must be <= " + SIZE);
        }

        return new RepeatingInputStream((int) size);
    }

    /**
     * Writes the size of bytes into the OutputStream.
     *
     * @param outputStream Stream to write into.
     * @param size Number of bytes to write.
     * @throws IOException If an IO error occurs.
     */
    public static void writeBytesToOutputStream(OutputStream outputStream, long size) throws IOException {
        int quotient = (int) size / RANDOM_BYTES.length;
        int remainder = (int) size % RANDOM_BYTES.length;

        for (int i = 0; i < quotient; i++) {
            outputStream.write(RANDOM_BYTES);
        }

        outputStream.write(RANDOM_BYTES, 0, remainder);
    }

    /**
     * Generate random string of given {@code targetLength length}. The string will only have lower case alphabets.
     *
     * @param targetLength of the string to be generated.
     * @return the generated string.
     */
    public static String generateRandomString(int targetLength) {
        int leftLimit = 97;
        int rightLimit = 122;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
            .limit(targetLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
        return generatedString;
    }

    private static final class RepeatingInputStream extends InputStream {
        private final int size;

        private int mark = 0;
        private int pos = 0;

        private RepeatingInputStream(int size) {
            this.size = size;
        }

        @Override
        public synchronized int read() {
            return (pos < size) ? (RANDOM_BYTES[pos++ % RANDOM_BYTES_LENGTH] & 0xFF) : -1;
        }

        @Override
        public synchronized int read(byte[] b) {
            return read(b, 0, b.length);
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) {
            if (pos >= size) {
                return -1;
            }

            int readCount = Math.min(len, RANDOM_BYTES_LENGTH);
            System.arraycopy(RANDOM_BYTES, 0, b, off, len);
            pos += readCount;

            return readCount;
        }

        @Override
        public synchronized void reset() {
            this.pos = this.mark;
        }

        @Override
        public synchronized void mark(int readlimit) {
            this.mark = readlimit;
        }

        @Override
        public boolean markSupported() {
            return true;
        }
    }
}
