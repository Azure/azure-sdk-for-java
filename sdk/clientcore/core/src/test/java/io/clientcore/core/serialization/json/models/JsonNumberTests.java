// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.serialization.json.models;

import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.serialization.json.implementation.StringBuilderWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link JsonNumber}.
 */
public class JsonNumberTests {
    @Test
    public void kindCheck() {
        JsonElement element = new JsonNumber(0);
        assertTrue(element.isNumber());
        assertFalse(element.isArray());
        assertFalse(element.isObject());
        assertFalse(element.isString());
        assertFalse(element.isBoolean());
        assertFalse(element.isNull());
    }

    @ParameterizedTest
    @MethodSource("fromJsonSupplier")
    public void fromJson(String json, Number number) throws IOException, ParseException {
        try (JsonReader reader = JsonReader.fromString(json)) {
            JsonNumber jsonNumber = JsonNumber.fromJson(reader);
            if (number instanceof BigInteger) {
                BigInteger jsonBigInteger = assertInstanceOf(BigInteger.class, jsonNumber.getValue());
                assertEquals(0, ((BigInteger) number).compareTo(jsonBigInteger));
            } else if (number instanceof BigDecimal) {
                BigDecimal jsonBigDecimal = assertInstanceOf(BigDecimal.class, jsonNumber.getValue());
                assertEquals(0, ((BigDecimal) number).compareTo(jsonBigDecimal));
            } else {
                assertEquals(number, jsonNumber.getValue());
            }
        }
    }

    private static Stream<Arguments> fromJsonSupplier() {
        return Stream.of(Arguments.of("0", 0), Arguments.of("-1", -1), Arguments.of("10000000000", 10000000000L),
            Arguments.of("-10000000000", -10000000000L),
            Arguments.of("100000000000000000000", new BigInteger("100000000000000000000")),
            Arguments.of("-100000000000000000000", new BigInteger("-100000000000000000000")), Arguments.of("0.0", 0.0D),
            Arguments.of("-1.0", -1.0D), Arguments.of("1E6", 1E6D), Arguments.of("-1E6", -1E6D),
            Arguments.of("1.0E6", 1E6D), Arguments.of("-1.0E6", -1E6D), Arguments.of("1e6", 1E6D),
            Arguments.of("-1e6", -1E6D), Arguments.of("1.0e6", 1E6D), Arguments.of("-1.0e6", -1E6D),
            Arguments.of("1E39", 1E39D), Arguments.of("-1E39", -1E39D), Arguments.of("1.0E39", 1E39D),
            Arguments.of("-1.0E39", -1E39D), Arguments.of("1e39", 1E39D), Arguments.of("-1e39", -1E39D),
            Arguments.of("1.0e39", 1E39D), Arguments.of("-1.0e39", -1E39D),
            Arguments.of("1E309", new BigDecimal("1E309")), Arguments.of("-1E309", new BigDecimal("-1E309")),
            Arguments.of("1.0E309", new BigDecimal("1E309")), Arguments.of("-1.0E309", new BigDecimal("-1E309")),
            Arguments.of("1e309", new BigDecimal("1E309")), Arguments.of("-1e309", new BigDecimal("-1E309")),
            Arguments.of("1.0e309", new BigDecimal("1E309")), Arguments.of("-1.0e309", new BigDecimal("-1E309")),
            Arguments.of("Infinity", Double.POSITIVE_INFINITY), Arguments.of("-Infinity", Double.NEGATIVE_INFINITY));
    }

    @ParameterizedTest
    @MethodSource("toJsonSupplier")
    public void toJson(Number value) throws IOException {
        JsonNumber jsonNumber = new JsonNumber(value);
        try (StringBuilderWriter writer = new StringBuilderWriter()) {
            try (JsonWriter jsonWriter = JsonWriter.toWriter(writer)) {
                jsonNumber.toJson(jsonWriter);
            }
            assertEquals(String.valueOf(value), writer.toString());
        }
    }

    private static Stream<Number> toJsonSupplier() {
        return Stream.of(0, -1, 0.0, -1.0, 1E6, -1E6, 1.0E6, -1.0E6);
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "null", "\"hello\"", "[]", "{}" })
    public void invalidFromJsonStartingPoints(String json) throws IOException {
        try (JsonReader reader = JsonReader.fromString(json)) {
            assertThrows(IllegalStateException.class, () -> JsonNumber.fromJson(reader));
        }
    }
}
