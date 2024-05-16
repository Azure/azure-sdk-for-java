// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.http.HttpMethod;
import com.azure.core.serializer.avro.jackson.generatedtestsources.HandOfCards;
import com.azure.core.serializer.avro.jackson.generatedtestsources.LongLinkedList;
import com.azure.core.serializer.avro.jackson.generatedtestsources.PlayingCard;
import org.apache.avro.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link JacksonAvroSerializerProvider}.
 */
public class JacksonAvroSerializerProviderTests {
    @ParameterizedTest
    @MethodSource("getPrimitiveSchemaSupplier")
    public void getPrimitiveSchema(Object primitiveValue, String expected) {
        assertEquals(expected, new JacksonAvroSerializerProvider().getSchema(primitiveValue));
    }

    @ParameterizedTest
    @MethodSource("getPrimitiveSchemaNameSupplier")
    public void getPrimitiveSchemaName(Object primitiveValue, String expected) {
        assertEquals(expected, new JacksonAvroSerializerProvider().getSchemaName(primitiveValue));
    }

    private static Stream<Arguments> getPrimitiveSchemaSupplier() {
        return getPrimitiveSchemaHelper(Schema::toString);
    }

    private static Stream<Arguments> getPrimitiveSchemaNameSupplier() {
        return getPrimitiveSchemaHelper(Schema::getFullName);
    }

    private static Stream<Arguments> getPrimitiveSchemaHelper(Function<Schema, String> func) {
        return Stream.of(
            Arguments.of(null, func.apply(Schema.create(Schema.Type.NULL))),
            Arguments.of(true, func.apply(Schema.create(Schema.Type.BOOLEAN))),
            Arguments.of(Boolean.TRUE, func.apply(Schema.create(Schema.Type.BOOLEAN))),
            Arguments.of(0, func.apply(Schema.create(Schema.Type.INT))),
            Arguments.of(Integer.valueOf(0), func.apply(Schema.create(Schema.Type.INT))),
            Arguments.of(0L, func.apply(Schema.create(Schema.Type.LONG))),
            Arguments.of(Long.valueOf(0L), func.apply(Schema.create(Schema.Type.LONG))),
            Arguments.of(0F, func.apply(Schema.create(Schema.Type.FLOAT))),
            Arguments.of(Float.valueOf(0F), func.apply(Schema.create(Schema.Type.FLOAT))),
            Arguments.of(0D, func.apply(Schema.create(Schema.Type.DOUBLE))),
            Arguments.of(Double.valueOf(0D), func.apply(Schema.create(Schema.Type.DOUBLE))),
            Arguments.of("true", func.apply(Schema.create(Schema.Type.STRING))),
            Arguments.of("true".subSequence(0, 1), func.apply(Schema.create(Schema.Type.STRING))),
            Arguments.of(new byte[0], func.apply(Schema.create(Schema.Type.BYTES))),
            Arguments.of(ByteBuffer.allocate(0), func.apply(Schema.create(Schema.Type.BYTES)))
        );
    }

    @ParameterizedTest
    @MethodSource("getComplexSchemaSupplier")
    public void getComplexSchema(Object complexType, String expected) {
        assertEquals(expected, new JacksonAvroSerializerProvider().getSchema(complexType));
    }

    @ParameterizedTest
    @MethodSource("getComplexSchemaNameSupplier")
    public void getComplexSchemaName(Object complexType, String expected) {
        assertEquals(expected, new JacksonAvroSerializerProvider().getSchemaName(complexType));
    }

    private static Stream<Arguments> getComplexSchemaSupplier() {
        return getComplexSchemaHelper(Schema::toString);
    }

    private static Stream<Arguments> getComplexSchemaNameSupplier() {
        return getComplexSchemaHelper(Schema::getFullName);
    }

    private static Stream<Arguments> getComplexSchemaHelper(Function<Schema, String> func) {
        return Stream.of(
            Arguments.of(new HandOfCards(), func.apply(HandOfCards.SCHEMA$)),
            Arguments.of(new LongLinkedList(), func.apply(LongLinkedList.SCHEMA$)),
            Arguments.of(new PlayingCard(), func.apply(PlayingCard.SCHEMA$))
        );
    }

    @Test
    public void nonPrimitiveOrRecordTypeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new JacksonAvroSerializerProvider()
            .getSchema(HttpMethod.GET));

        assertThrows(IllegalArgumentException.class, () -> new JacksonAvroSerializerProvider()
            .getSchemaName(HttpMethod.GET));
    }
}
