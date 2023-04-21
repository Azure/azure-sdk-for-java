// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.stream.Stream;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AvroParserStateTests {
    @Test
    public void constructor() {
        AvroParserState state = new AvroParserState();
        assertEquals(0, state.getSize());
        assertTrue(state.getCache().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 10, 100, 1000 })
    public void write(int size) {
        AvroParserState state = new AvroParserState();
        byte[] b = new byte[size];
        new Random().nextBytes(b);
        state.write(ByteBuffer.wrap(b));

        assertEquals(size, state.getSize());
        assertArraysEqual(b, AvroSchema.getBytes(state.read(size)));
    }

    @ParameterizedTest
    @MethodSource("readSizeSupplier")
    public void readSize(int size, int remaining, byte[] value, int buffersLeft) {
        AvroParserState state = new AvroParserState();
        state.write(ByteBuffer.wrap("Hello ".getBytes(StandardCharsets.UTF_8)));
        state.write(ByteBuffer.wrap("World!".getBytes(StandardCharsets.UTF_8)));

        assertArraysEqual(value, AvroSchema.getBytes(state.read(size)));
        assertEquals(remaining, state.getSize());
        assertEquals(buffersLeft, state.getCache().size());
    }

    private static Stream<Arguments> readSizeSupplier() {
        return Stream.of(
            Arguments.of(0, 12, new byte[0], 2),
            Arguments.of(3, 9, "Hel".getBytes(StandardCharsets.UTF_8), 2),
            Arguments.of(6, 6, "Hello ".getBytes(StandardCharsets.UTF_8), 1),
            Arguments.of(7, 5, "Hello W".getBytes(StandardCharsets.UTF_8), 1),
            Arguments.of(12, 0, "Hello World!".getBytes(StandardCharsets.UTF_8), 0)
        );
    }

    @Test
    public void read() {
        String word = "Hello World!";
        AvroParserState state = new AvroParserState();
        state.write(ByteBuffer.wrap(word.substring(0, 6).getBytes(StandardCharsets.UTF_8)));
        state.write(ByteBuffer.wrap(word.substring(6, 12).getBytes(StandardCharsets.UTF_8)));

        int index = 0;
        while (index < word.length()) {
            assertEquals(word.charAt(index), state.read());
            index++;
            assertEquals(word.length() - index, state.getSize());
        }
    }
}
