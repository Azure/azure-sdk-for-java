// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.apache;

import com.azure.core.serializer.AvroSerializer;
import org.apache.avro.util.Utf8;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApacheAvroSerializerTests {
    private static final AvroSerializer AVRO_SERIALIZER = new ApacheAvroSerializerBuilder().build();

    /*
     * This Avro schema specifies the Java string type that should be used to deserialize STRING. Without specifying
     * 'String' the default is 'CharSequence' which ends up being wrapped in Apache's 'Utf8' class. Additionally, this
     * can be set as a compile configuration.
     */
    private static final String SPECIFIED_STRING_SCHEMA = "{\"type\": \"string\",\"avro.java.string\":\"String\"}";

    private static final String SPECIFIED_CHAR_SEQUENCE_SCHEMA = "{\"type\": \"string\","
        + "\"avro.java.string\":\"CharSequence\"}";

    private static final String ENUM_SCHEMA = "{\"type\":\"enum\","
        + "\"name\":\"com.azure.core.serializer.avro.apache.PlayingCardSuits\","
        + "\"symbols\":[\"SPADES\",\"HEARTS\",\"DIAMONDS\",\"CLUBS\"]}";

    private static final String INT_ARRAY_SCHEMA = "{\"type\":\"array\",\"items\":\"int\"}";

    private static final String INT_MAP_SCHEMA = "{\"type\":\"map\",\"values\":\"int\"}";

    @ParameterizedTest
    @MethodSource("deserializePrimitiveTypesSupplier")
    public void deserializePrimitiveTypes(String schema, byte[] avro, Object expected) {
        Object actual = AVRO_SERIALIZER.read(avro, schema);

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> deserializePrimitiveTypesSupplier() {
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

            // STRING has an additional property 'avro.java.string' which indicates the deserialization type.
            // Using Java's String class.
            Arguments.of(SPECIFIED_STRING_SCHEMA, new byte[] { 0 }, ""),
            Arguments.of(SPECIFIED_STRING_SCHEMA, new byte[] { 0x06, 0x66, 0x6F, 0x6F }, "foo"),

            // Using Java's CharSequence class that gets wrapped in Apache's Utf8.
            Arguments.of(SPECIFIED_CHAR_SEQUENCE_SCHEMA, new byte[] { 0 }, new Utf8("")),
            Arguments.of(SPECIFIED_CHAR_SEQUENCE_SCHEMA, new byte[] { 0x06, 0x66, 0x6F, 0x6F }, new Utf8("foo")),

            // BYTES deserializes into ByteBuffers.
            Arguments.of(schemaCreator("bytes"), new byte[] { 0 }, ByteBuffer.wrap(new byte[0])),
            Arguments.of(schemaCreator("bytes"), new byte[] { 4, 42, 42 }, ByteBuffer.wrap(new byte[] { 42, 42 }))
        );
    }

    @ParameterizedTest
    @MethodSource("deserializeComplexTypesSupplier")
    public <T> void deserializeComplexTypes(String schema, byte[] avro, T expected) {
        T actual = AVRO_SERIALIZER.read(avro, schema);

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> deserializeComplexTypesSupplier() {
        return Stream.of(
            Arguments.of(ENUM_SCHEMA, new byte[] { 0 }, PlayingCardSuits.SPADES),
            Arguments.of(ENUM_SCHEMA, new byte[] { 2 }, PlayingCardSuits.HEARTS),
            Arguments.of(INT_ARRAY_SCHEMA, new byte[] { 0 }, Collections.emptyList()),
            Arguments.of(INT_ARRAY_SCHEMA, new byte[] { 6, 20, 40, 60, 0 }, Arrays.asList(10, 20, 30)),
            Arguments.of(INT_MAP_SCHEMA, new byte[] { 0 }, Collections.emptyMap()),
            Arguments.of(INT_MAP_SCHEMA, new byte[] { 2, 0x06, 0x66, 0x6F, 0x6F, 2, 0 },
                Collections.singletonMap("foo", 1))
        );
    }

    @Test
    public void deserializeNullReturnsNull() {
        assertNull(AVRO_SERIALIZER.read(null, "ignored"));
    }

    @Test
    public void deserializeNullSchemaThrows() {
        assertThrows(NullPointerException.class, () -> AVRO_SERIALIZER.read(null, null));
    }

    @Test
    public void deserializeMaps() {

    }

    @Test
    public void deserializeUnion() {

    }

    @Test
    public void deserializeFixed() {

    }

    @Test
    public void deserializeRecord() {

    }

    @ParameterizedTest
    @MethodSource("simpleSerializationSupplier")
    public void simpleSerialization(String schema, Object value, byte[] expected) {
        byte[] actual = AVRO_SERIALIZER.write(value, schema);

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
            Arguments.of(SPECIFIED_STRING_SCHEMA, "", new byte[] { 0 }),
            Arguments.of(SPECIFIED_STRING_SCHEMA, "foo", new byte[] { 0x06, 0x66, 0x6F, 0x6F }),
            Arguments.of(schemaCreator("bytes"), ByteBuffer.wrap(new byte[0]), new byte[] { 0 }),
            Arguments.of(schemaCreator("bytes"), ByteBuffer.wrap(new byte[] { 42, 42 }), new byte[] { 4, 42, 42 })
        );
    }

    @Test
    public void serializeNullSchemaThrows() {
        assertThrows(NullPointerException.class, () -> AVRO_SERIALIZER.write(null, null));
    }

    private static String schemaCreator(String type) {
        return String.format("{\"type\" : \"%s\"}", type);
    }
}
