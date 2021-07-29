// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Utility class to help with data creation for perf testing.
 */
public class TestDataCreationHelper {
    private static final int RANDOM_BYTES_LENGTH = Integer.parseInt(
        System.getProperty("azure.core.perf.test.data.buffer.size", "1048576")); // 1MB default;
    private static final byte[] RANDOM_BYTES;

    static {
        Random random = new Random(0);
        RANDOM_BYTES = new byte[RANDOM_BYTES_LENGTH];
        random.nextBytes(RANDOM_BYTES);
    }

    /**
     * Creates a {@link Flux} of {@code size} with repeated values of {@code array}.
     *
     * @param array the array to create Flux from.
     * @param size the size of the flux to create.
     * @return The created {@link Flux}
     */
    private static Flux<ByteBuffer> createCircularByteBufferFlux(byte[] array, long size) {
        long quotient = size / array.length;
        int remainder = (int) (size % array.length);

        return Flux.just(Boolean.TRUE).repeat(quotient - 1)
            .map(i -> allocateByteBuffer(array, array.length))
            .concatWithValues(allocateByteBuffer(array, remainder));
    }

    private static ByteBuffer allocateByteBuffer(byte[] array, int size) {
        // ByteBuffer.allocate() should be used instead of ByteBuffer.wrap().  The former ensures each
        // ByteBuffer holds its own copy of the data, which more closely simulates real-world usage.
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.put(array, 0, size);
        byteBuffer.rewind();
        return byteBuffer;
    }

    /**
     * Creates a random flux of specified size.
     *
     * @param size the size of the stream
     * @return the {@link Flux} of {@code size}
     */
    public static Flux<ByteBuffer> createRandomByteBufferFlux(long size) {
        return createCircularByteBufferFlux(RANDOM_BYTES, size);
    }

    /**
     * Creates a random stream of specified size.
     *
     * @param size the size of the stream
     * @return the {@link InputStream} of {@code size}
     */
    public static InputStream createRandomInputStream(long size) {
        return new RepeatingInputStream(size);
    }

    /**
     * Writes the size of bytes into the OutputStream.
     *
     * @param outputStream Stream to write into.
     * @param size Number of bytes to write.
     * @throws IOException If an IO error occurs.
     */
    public static void writeBytesToOutputStream(OutputStream outputStream, long size) throws IOException {
        long quotient = size / RANDOM_BYTES.length;
        int remainder = (int) (size % RANDOM_BYTES.length);

        for (long i = 0; i < quotient; i++) {
            outputStream.write(RANDOM_BYTES);
        }

        outputStream.write(RANDOM_BYTES, 0, remainder);
    }

    /**
     * Writes the data from InputStream into the OutputStream.
     *
     * @param inputStream stream to read from.
     * @param outputStream stream to write into.
     * @param bufferSize number of bytes to read in a single read.
     * @throws IOException If an IO error occurs.
     * @return the number of bytes transferred.
     */
    public static long copyStream(InputStream inputStream, OutputStream outputStream, int bufferSize) throws IOException {
        long transferred = 0;
        byte[] buffer = new byte[bufferSize];
        int read;
        while ((read = inputStream.read(buffer, 0, bufferSize)) >= 0) {
            outputStream.write(buffer, 0, read);
            transferred += read;

        }
        return transferred;
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

    /**
     * Writes contents of the specified size to the specified file path.
     *
     * @param filePath the path of the file to write to contents to
     * @param size the size of the contents to write to the file.
     * @param bufferSize the size of the buffer to use to write to the file.
     * @throws IOException when an error occurs when writing to the file.
     */
    public static void writeToFile(String filePath, long size, int bufferSize) throws IOException {
        InputStream inputStream = createRandomInputStream(size);
        OutputStream outputStream = new FileOutputStream(filePath);
        copyStream(inputStream, outputStream, bufferSize);
    }
}
