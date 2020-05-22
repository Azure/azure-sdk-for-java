// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.serializer.AvroSerializer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacksonAvroSerializerTests {
    private static final AvroSerializer AVRO_SERIALIZER = new JacksonAvroSerializerBuilder().build();

    @ParameterizedTest
    @MethodSource("simpleDeserializationSupplier")
    public void simpleDeserialization(String schema, byte[] avro, Object expected) {
        Object actual = AVRO_SERIALIZER.deserialize(avro, schema);

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> simpleDeserializationSupplier() {
        return Stream.of(
            Arguments.of(schemaCreator("null"), new byte[0], null),
            Arguments.of(schemaCreator("boolean"), new byte[] { 0 }, false),
            Arguments.of(schemaCreator("boolean"), new byte[] { 1 }, true),

            // INT and LONG use zigzag encoding.
            Arguments.of(schemaCreator("int"), new byte[] { 42 }, 21),
            Arguments.of(schemaCreator("long"), new byte[] { 42 }, 21L),

            // FLOAT and DOUBLE use little endian.
            Arguments.of(schemaCreator("float"), new byte[] { 0x00, 0x00, 0x28, 0x42 }, 42F),
            Arguments.of(schemaCreator("double"), new byte[] {  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x45, 0x40 }, 42D),

            // STRING deserializes into Apache's Util.Utf8.
            Arguments.of(schemaCreator("string"), new byte[] { 0 }, ""),
            Arguments.of(schemaCreator("string"), new byte[] { 0x06, 0x66, 0x6F, 0x6F }, "foo"),

            // BYTES deserializes into ByteBuffers.
            Arguments.of(schemaCreator("bytes"), new byte[] { 0 }, ByteBuffer.wrap(new byte[0])),
            Arguments.of(schemaCreator("bytes"), new byte[] { 4, 42, 42 }, ByteBuffer.wrap(new byte[] { 42, 42 }))
        );
    }

    @ParameterizedTest
    @MethodSource("simpleSerializationSupplier")
    public void simpleSerialization(String schema, Object value, byte[] expected) {
        byte[] actual = AVRO_SERIALIZER.serialize(value, schema);

        assertArrayEquals(expected, actual);
    }

    private static Stream<Arguments> simpleSerializationSupplier() {
        return Stream.of(
            Arguments.of(schemaCreator("null"), null, new byte[0]),
            Arguments.of(schemaCreator("boolean"), false, new byte[] { 0 }),
            Arguments.of(schemaCreator("boolean"), true, new byte[] { 1 }),

            // INT and LONG use zigzag encoding.
            Arguments.of(schemaCreator("int"), 21, new byte[] { 42 }),
            Arguments.of(schemaCreator("long"), 21L, new byte[] { 42 }),

            // FLOAT and DOUBLE use little endian.
            Arguments.of(schemaCreator("float"), 42F, new byte[] { 0x00, 0x00, 0x28, 0x42 }),
            Arguments.of(schemaCreator("double"), 42D, new byte[] {  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x45, 0x40 }),
            Arguments.of(schemaCreator("string"), "", new byte[] { 0 }),
            Arguments.of(schemaCreator("string"), "foo", new byte[] { 0x06, 0x66, 0x6F, 0x6F }),
            Arguments.of(schemaCreator("bytes"), new byte[0], new byte[] { 0 }),
            Arguments.of(schemaCreator("bytes"), new byte[] { 42, 42 }, new byte[] { 4, 42, 42 }),
            Arguments.of(schemaCreator("bytes"), ByteBuffer.wrap(new byte[0]), new byte[] { 0 }),
            Arguments.of(schemaCreator("bytes"), ByteBuffer.wrap(new byte[] { 42, 42 }), new byte[] { 4, 42, 42 })
        );
    }

    private static String schemaCreator(String type) {
        return String.format("{\"type\" : \"%s\"}", type);
    }
}
