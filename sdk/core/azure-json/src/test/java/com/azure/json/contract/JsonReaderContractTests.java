// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.contract;

import com.azure.json.JsonReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the contract of {@link JsonReader}.
 * <p>
 * All implementations of {@link JsonReader} must create a subclass of this test class and pass all tests as they're
 * written to be considered an acceptable implementation.
 * <p>
 * Each test will only create a single instance of {@link JsonReader} to simplify the usage of
 * {@link #getJsonReader(String)}.
 */
public abstract class JsonReaderContractTests {
    /**
     * Creates an instance of {@link JsonReader} that will be used by a test.
     *
     * @param json The JSON to be read.
     * @return The {@link JsonReader} that a test will use.
     */
    protected abstract JsonReader getJsonReader(String json);

    @ParameterizedTest
    @MethodSource("basicOperationsSupplier")
    public <T> void basicOperations(String json, T expectedValue, Function<JsonReader, T> function) {
        JsonReader reader = getJsonReader(json);
        reader.nextToken(); // Initialize the JsonReader for reading.

        T actualValue = assertDoesNotThrow(() -> function.apply(reader));

        assertEquals(expectedValue, actualValue);
    }

    private static Stream<Arguments> basicOperationsSupplier() {
        return Stream.of(
            // Value handling.

            // Boolean
            Arguments.of("false", false, createJsonConsumer(JsonReader::getBooleanValue)),
            Arguments.of("true", true, createJsonConsumer(JsonReader::getBooleanValue)),

            // Double
            Arguments.of("-42.0", -42D, createJsonConsumer(JsonReader::getDoubleValue)),
            Arguments.of("-42", -42D, createJsonConsumer(JsonReader::getDoubleValue)),
            Arguments.of("42.0", 42D, createJsonConsumer(JsonReader::getDoubleValue)),
            Arguments.of("42", 42D, createJsonConsumer(JsonReader::getDoubleValue)),

            // Float
            Arguments.of("-42.0", -42F, createJsonConsumer(JsonReader::getFloatValue)),
            Arguments.of("-42", -42F, createJsonConsumer(JsonReader::getFloatValue)),
            Arguments.of("42.0", 42F, createJsonConsumer(JsonReader::getFloatValue)),
            Arguments.of("42", 42F, createJsonConsumer(JsonReader::getFloatValue)),

            // Integer
            Arguments.of("-42", -42, createJsonConsumer(JsonReader::getIntValue)),
            Arguments.of("42", 42, createJsonConsumer(JsonReader::getIntValue)),

            // Long
            Arguments.of("-42", -42L, createJsonConsumer(JsonReader::getLongValue)),
            Arguments.of("42", 42L, createJsonConsumer(JsonReader::getLongValue)),

            // String
            Arguments.of("null", null, createJsonConsumer(JsonReader::getStringValue)),
            Arguments.of("\"\"", "", createJsonConsumer(JsonReader::getStringValue)),
            Arguments.of("\"hello\"", "hello", createJsonConsumer(JsonReader::getStringValue))
        );
    }

    // Byte arrays can't use Object.equals as they'll be compared by memory location instead of value equality.
    @ParameterizedTest
    @MethodSource("binaryOperationsSupplier")
    public void binaryOperations(String json, byte[] expectedValue, Function<JsonReader, byte[]> function) {
        JsonReader reader = getJsonReader(json);
        reader.nextToken(); // Initialize the JsonReader for reading.

        byte[] actualValue = assertDoesNotThrow(() -> function.apply(reader));

        assertArrayEquals(expectedValue, actualValue);
    }

    private static Stream<Arguments> binaryOperationsSupplier() {
        return Stream.of(
            // Binary
            Arguments.of("null", null, createJsonConsumer(JsonReader::getBinaryValue)),
            Arguments.of("\"\"", new byte[0], createJsonConsumer(JsonReader::getBinaryValue)),
            Arguments.of("\"" + Base64.getEncoder().encodeToString("Hello".getBytes(StandardCharsets.UTF_8)) + "\"",
                "Hello".getBytes(StandardCharsets.UTF_8), createJsonConsumer(JsonReader::getBinaryValue))
        );
    }

    private static <T> Function<JsonReader, T> createJsonConsumer(Function<JsonReader, T> func) {
        return func;
    }
}
