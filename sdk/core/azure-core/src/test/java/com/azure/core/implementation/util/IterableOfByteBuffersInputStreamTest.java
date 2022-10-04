// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IterableOfByteBuffersInputStreamTest {

    private static final Random RANDOM = new Random();

    @Test
    public void throwsOnNull() {
        assertThrows(NullPointerException.class, () -> new IterableOfByteBuffersInputStream(null));
    }

    @Test
    public void testAvailable() throws Exception {
        byte[] data1 = new byte[1024];
        byte[] data2 = new byte[512];

        IterableOfByteBuffersInputStream stream = new IterableOfByteBuffersInputStream(
            Arrays.asList(ByteBuffer.wrap(data1), ByteBuffer.wrap(data2))
        );

        assertEquals(data1.length, stream.available());

        stream.read();
        assertEquals(data1.length - 1, stream.available());

        stream.read(new byte[data1.length - 1]);
        assertEquals(data2.length, stream.available());

        stream.read();
        assertEquals(data2.length - 1, stream.available());

        readStream(stream);

        assertEquals(0, stream.available());
    }

    @Test
    public void testDoesNotConsumeBuffers() throws Exception {
        byte[] data1 = new byte[1024];
        byte[] data2 = new byte[512];

        ByteBuffer buffer1 = ByteBuffer.wrap(data1);
        ByteBuffer buffer2 = ByteBuffer.wrap(data2);
        IterableOfByteBuffersInputStream stream = new IterableOfByteBuffersInputStream(
            Arrays.asList(buffer1, buffer2)
        );

        readStream(stream);

        assertEquals(data1.length, buffer1.remaining());
        assertEquals(data2.length, buffer2.remaining());
    }

    @ParameterizedTest
    @MethodSource("provideReadingArguments")
    public void testBufferedReading(List<ByteBuffer> buffers, byte[] expected) throws Exception {
        IterableOfByteBuffersInputStream stream = new IterableOfByteBuffersInputStream(buffers);

        byte[] bytes = readStream(stream);

        assertArrayEquals(expected, bytes);
    }

    @ParameterizedTest
    @MethodSource("provideReadingArguments")
    public void testReadingByteByByte(List<ByteBuffer> buffers, byte[] expected) throws Exception {
        IterableOfByteBuffersInputStream stream = new IterableOfByteBuffersInputStream(buffers);

        byte[] bytes = readStreamByteByByte(stream);

        assertArrayEquals(expected, bytes);
    }

    public static Stream<Arguments> provideReadingArguments() {
        byte[] tinyData = new byte[1];
        byte[] smallData = new byte[1024 + 113];
        byte[] mediumData = new byte[8 * 1024 + 113];
        byte[] largeData = new byte[10 * 8 * 1024 + 113];
        RANDOM.nextBytes(tinyData);
        RANDOM.nextBytes(smallData);
        RANDOM.nextBytes(mediumData);
        RANDOM.nextBytes(largeData);
        return Stream.concat(
            Stream.of(
                Arguments.of(
                    Collections.emptyList(),
                    new byte[0]
                ),
                Arguments.of(
                    Collections.singletonList(ByteBuffer.allocate(0)),
                    new byte[0]
                ),
                Arguments.of(
                    Arrays.asList(ByteBuffer.allocate(0), ByteBuffer.allocate(0)),
                    new byte[0]
                )
            ),
            Stream.of(tinyData, smallData, mediumData, largeData)
                .flatMap(bytes -> {

                    ByteBuffer overSizedBuffer1 = ByteBuffer.allocate(bytes.length + 11);
                    overSizedBuffer1.put(bytes);
                    overSizedBuffer1.flip();

                    ByteBuffer overSizedBuffer2 = ByteBuffer.allocate(bytes.length + 11);
                    overSizedBuffer2.position(11);
                    overSizedBuffer2.put(bytes);
                    overSizedBuffer2.position(11);

                    // create mixture of buffers.
                    List<ByteBuffer> mixedBuffers = new LinkedList<>();
                    int buffSize = 0;
                    int remaining = bytes.length;
                    int offset = 0;
                    while (remaining > 0) {
                        buffSize = Math.min(buffSize, remaining);
                        ByteBuffer buf = ByteBuffer.wrap(bytes, offset, buffSize);
                        mixedBuffers.add(buf);
                        offset += buffSize;
                        remaining -= buffSize;
                        buffSize++;
                    }

                    return Stream.of(
                        Arguments.of(
                            Collections.singletonList(ByteBuffer.wrap(bytes)),
                            bytes
                        ),
                        Arguments.of(
                            Collections.singletonList(overSizedBuffer1),
                            bytes
                        ),
                        Arguments.of(
                            Collections.singletonList(overSizedBuffer2),
                            bytes
                        ),
                        Arguments.of(
                            mixedBuffers,
                            bytes
                        )
                    );
                })
        );
    }

    private byte[] readStream(InputStream stream) throws Exception {
        return readStream(stream, 8 * 1024);
    }

    private byte[] readStream(InputStream stream, int bufferSize) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[bufferSize];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }

    private byte[] readStreamByteByByte(InputStream stream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int tmp;
        while ((tmp = stream.read()) != -1) {
            outputStream.write(tmp);
        }
        return outputStream.toByteArray();
    }
}
