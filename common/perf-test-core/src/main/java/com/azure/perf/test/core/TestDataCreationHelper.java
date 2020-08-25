// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Utility class to help with data creation for perf testing.
 */
public class TestDataCreationHelper {
    private static final byte[] RANDOM_BYTES;
    private static final ByteBuffer RANDOM_BYTE_BUFFER;
    private static final int SIZE = (1024 * 1024 * 1024) + 1;

    static {
        Random random = new Random();
        RANDOM_BYTES = new byte[1024 * 1024];
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
     * @param size the size of the stream
     * @return the {@link Flux} of {@code size}
     */
    public static Flux<ByteBuffer> createRandomByteBufferFlux(long size) {
        return createCircularByteBufferFlux(RANDOM_BYTE_BUFFER, size);
    }

    /**
     * Creates a random stream of specified size.
     * @param size the size of the stream
     *
     * @throws IllegalArgumentException if {@code size} is more than {@link #SIZE}
     * @return the {@link InputStream} of {@code size}
     */
    public static InputStream createRandomInputStream(long size) {
        if (size > SIZE) {
            throw new IllegalArgumentException("size must be <= " + SIZE);
        }

        return createCircularInputStream(RANDOM_BYTES, size);
    }

    /**
     * Creates a stream of {@code size}with repeated values of {@code byteArray}.
     * @param byteArray the array to create stream from.
     * @param size the size of the stream to create.
     * @return The created {@link InputStream}
     */
    public static InputStream createCircularInputStream(byte[] byteArray, long size) {
        int remaining = byteArray.length;
        int quotient = (int) size / remaining;
        int remainder = (int) size % remaining;

        List<InputStream> list = Stream.concat(IntStream.range(0, quotient)
                .mapToObj(i -> new ByteArrayInputStream(byteArray)),
            Stream.of(new ByteArrayInputStream(byteArray, 0, remainder)))
            .collect(Collectors.toList());

        return new SequenceInputStream(Collections.enumeration(list));
    }

    public static void writeBytesToOutputStream(OutputStream outputStream, long size) throws IOException {
        int quotient = (int) size / RANDOM_BYTES.length;
        int remainder = (int) size % RANDOM_BYTES.length;

        for (int i = 0; i < quotient; i++) {
            outputStream.write(RANDOM_BYTES);
        }

        outputStream.write(RANDOM_BYTES, 0, remainder);
    }
}
