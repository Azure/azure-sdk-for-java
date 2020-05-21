// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.apache;

import com.azure.core.serializer.AvroSerializer;
import org.apache.avro.Conversion;
import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.util.Utf8;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        + "\"logicalType\":\"com.azure.core.serializer.avro.apache.PlayingCardSuits\","
        + "\"symbols\":[\"SPADES\",\"HEARTS\",\"DIAMONDS\",\"CLUBS\"]}";

    private static final String ENUM_LOGICAL_TYPE = "com.azure.core.serializer.avro.apache.PlayingCardSuits";

    private static final String INT_ARRAY_SCHEMA = "{\"type\":\"array\",\"items\":\"int\"}";

    private static final String INT_MAP_SCHEMA = "{\"type\":\"map\",\"values\":\"int\","
        + "\"avro.java.string\":\"String\"}";

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

    @Test
    public void deserializeEnum() {
        LogicalTypes.register(ENUM_LOGICAL_TYPE, schema -> new LogicalType(ENUM_LOGICAL_TYPE));

        GenericData genericData = GenericData.get();
        genericData.addLogicalTypeConversion(new PlayingCardSuitConversion());

        AvroSerializer avroSerializer = new ApacheAvroSerializerBuilder().genericData(genericData).build();

        assertEquals(PlayingCardSuits.SPADES, avroSerializer.read(new byte[] { 0 }, ENUM_SCHEMA));
        assertEquals(PlayingCardSuits.HEARTS, avroSerializer.read(new byte[] { 2 }, ENUM_SCHEMA));
    }

    @Test
    public void deserializeList() {
        byte[] emptyListAvro = new byte[] { 0 };
        byte[] simpleListAvro = new byte[] { 6, 20, 40, 60, 0 }; // 3 elements, 10, 20, 30.

        List<Integer> expectedEmptyList = Collections.emptyList();
        List<Integer> expectedSimpleList = Arrays.asList(10, 20, 30);

        assertEquals(expectedEmptyList, AVRO_SERIALIZER.read(emptyListAvro, INT_ARRAY_SCHEMA));
        assertEquals(expectedSimpleList, AVRO_SERIALIZER.read(simpleListAvro, INT_ARRAY_SCHEMA));
    }

    @Test
    public void deserializeMap() {
        byte[] emptyMapAvro = new byte[] { 0 };
        byte[] simpleMapAvro = new byte[] { 2, 0x06, 0x66, 0x6F, 0x6F, 2, 0 }; // Map of "foo":1.
        byte[] multiBlockMapAvro = new byte[] {
            2, 0x06, 0x66, 0x6F, 0x6F, 2, // "foo":1
            2, 0x06, 0x62, 0x61, 0x72, 4, 0 // "bar":2, then end of map
        };

        Map<String, Integer> expectedEmptyMap = Collections.emptyMap();
        Map<String, Integer> expectedSimpleMap = Collections.singletonMap("foo", 1);
        Map<String, Integer> expectedMultiBlockMap = new HashMap<>();
        expectedMultiBlockMap.put("foo", 1);
        expectedMultiBlockMap.put("bar", 2);

        assertEquals(expectedEmptyMap, AVRO_SERIALIZER.read(emptyMapAvro, INT_MAP_SCHEMA));
        assertEquals(expectedSimpleMap, AVRO_SERIALIZER.read(simpleMapAvro, INT_MAP_SCHEMA));
        assertEquals(expectedMultiBlockMap, AVRO_SERIALIZER.read(multiBlockMapAvro, INT_MAP_SCHEMA));
    }

    @Test
    public void deserializeFixed() {

    }

    @Test
    public void deserializeRecord() {

    }

    @Test
    public void deserializeRecordWithUnion() {

    }

    @Test
    public void deserializeNullReturnsNull() {
        assertNull(AVRO_SERIALIZER.read(null, "ignored"));
    }

    @Test
    public void deserializeNullSchemaThrows() {
        assertThrows(NullPointerException.class, () -> AVRO_SERIALIZER.read(null, null));
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
    public void serializeEnum() {
        LogicalTypes.register(ENUM_LOGICAL_TYPE, schema -> new LogicalType(ENUM_LOGICAL_TYPE));

        GenericData genericData = GenericData.get();
        genericData.addLogicalTypeConversion(new PlayingCardSuitConversion());

        AvroSerializer avroSerializer = new ApacheAvroSerializerBuilder().genericData(genericData).build();

        assertArrayEquals(new byte[] { 0 }, avroSerializer.write(PlayingCardSuits.SPADES, ENUM_SCHEMA));
        assertArrayEquals(new byte[] { 2 }, avroSerializer.write(PlayingCardSuits.HEARTS, ENUM_SCHEMA));
    }

    @Test
    public void serializeList() {
        List<Integer> emptyList = Collections.emptyList();
        List<Integer> simpleList = Arrays.asList(10, 20, 30);


        byte[] expectedEmptyListAvro = new byte[] { 0 };
        byte[] expectedSimpleListAvro = new byte[] { 6, 20, 40, 60, 0 }; // 3 elements, 10, 20, 30.

        assertArrayEquals(expectedEmptyListAvro, AVRO_SERIALIZER.write(emptyList, INT_ARRAY_SCHEMA));
        assertArrayEquals(expectedSimpleListAvro, AVRO_SERIALIZER.write(simpleList, INT_ARRAY_SCHEMA));
    }

    @Test
    public void serializeMap() {
        Map<String, Integer> emptyMap = Collections.emptyMap();
        Map<String, Integer> simpleMap = Collections.singletonMap("foo", 1);
        Map<String, Integer> multiBlockMap = new HashMap<>();
        multiBlockMap.put("foo", 1);
        multiBlockMap.put("bar", 2);

        byte[] expectedEmptyMapAvro = new byte[] { 0 };
        byte[] expectedSimpleMapAvro = new byte[] { 2, 0x06, 0x66, 0x6F, 0x6F, 2, 0 }; // Map of "foo":1.
        byte[] expectedMultiBlockMapAvro = new byte[] {
            2, 0x06, 0x66, 0x6F, 0x6F, 2, // "foo":1
            2, 0x06, 0x62, 0x61, 0x72, 4, 0 // "bar":2, then end of map
        };

        assertArrayEquals(expectedEmptyMapAvro, AVRO_SERIALIZER.write(emptyMap, INT_MAP_SCHEMA));
        assertEquals(expectedSimpleMapAvro, AVRO_SERIALIZER.write(simpleMap, INT_MAP_SCHEMA));
        assertEquals(expectedMultiBlockMapAvro, AVRO_SERIALIZER.write(multiBlockMap, INT_MAP_SCHEMA));
    }

    @Test
    public void serializeNullSchemaThrows() {
        assertThrows(NullPointerException.class, () -> AVRO_SERIALIZER.write(null, null));
    }

    private static String schemaCreator(String type) {
        return String.format("{\"type\" : \"%s\"}", type);
    }

    private static final class PlayingCardSuitConversion extends Conversion<PlayingCardSuits> {
        @Override
        public Class<PlayingCardSuits> getConvertedType() {
            return PlayingCardSuits.class;
        }

        @Override
        public String getLogicalTypeName() {
            return ENUM_LOGICAL_TYPE;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public PlayingCardSuits fromEnumSymbol(GenericEnumSymbol value, Schema schema, LogicalType type) {
            return PlayingCardSuits.valueOf(value.toString());
        }

        @Override
        @SuppressWarnings("rawtypes")
        public GenericEnumSymbol toEnumSymbol(PlayingCardSuits value, Schema schema, LogicalType type) {
            return new GenericEnumSymbol() {
                @Override
                public Schema getSchema() {
                    return schema;
                }

                @Override
                public int compareTo(Object o) {
                    if (!(o instanceof GenericEnumSymbol)) {
                        return 0;
                    }

                    GenericEnumSymbol otherSymbol = (GenericEnumSymbol) o;
                    return (toString().equals(otherSymbol.toString())) ? 1 : 0;
                }

                @Override
                public String toString() {
                    return value.toString();
                }
            };
        }
    }
}
