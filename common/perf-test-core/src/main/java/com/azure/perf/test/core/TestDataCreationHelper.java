// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import reactor.core.publisher.Flux;

/**
 * Utility class to help with data creation for perf testing.
 */
public class TestDataCreationHelper {
    private static final byte[] RANDOM_BYTES;
    private static final ByteBuffer RANDOM_BYTE_BUFFER;
    private static final int SIZE = (1024 * 1024 * 1024) + 1;
    private static final byte[] RANDOM_STREAM_BYTES;

    static {
        RANDOM_BYTES = new byte[1024 * 1024];
        (new Random(0)).nextBytes(TestDataCreationHelper.RANDOM_BYTES);
        RANDOM_BYTE_BUFFER = ByteBuffer.wrap(TestDataCreationHelper.RANDOM_BYTES).asReadOnlyBuffer();
        RANDOM_STREAM_BYTES = new byte[SIZE];
        (new Random(0)).nextBytes(RANDOM_STREAM_BYTES);
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

        // Workaround for Azure/azure-sdk-for-java#6020
        // return CircularStream.create(_randomBytes, size);
        return new ByteArrayInputStream(RANDOM_STREAM_BYTES, 0, (int) size);
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
        List<ByteArrayInputStream> list = Flux.range(0, quotient)
            .map(i -> new ByteArrayInputStream(byteArray))
            .concatWithValues(new ByteArrayInputStream(byteArray, 0, remainder))
            .collectList()
            .block();
        return new SequenceInputStream(Collections.enumeration(list));
    }
}
