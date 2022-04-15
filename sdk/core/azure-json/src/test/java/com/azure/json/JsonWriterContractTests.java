// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.provider.Arguments;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the contract of {@link JsonWriter}.
 * <p>
 * All implementations of {@link JsonWriter} must create a subclass of this test class and pass all tests as they're
 * written to be considered an acceptable implementation.
 * <p>
 * Each test will only create a single instance of {@link JsonWriter} to simplify usage of {@link #getJsonWriter()}
 * and {@link #getJsonWriterContents()}.
 */
public abstract class JsonWriterContractTests {
    /**
     * Creates an instance of {@link JsonWriter} that will be used by a test.
     *
     * @return The {@link JsonWriter} that a test will use.
     */
    public abstract JsonWriter getJsonWriter();

    /**
     * Converts the content written to a {@link JsonWriter} during testing to a string representation.
     *
     * @return The contents of a {@link JsonWriter} converted to a string.
     */
    public abstract String getJsonWriterContents();

//    @ParameterizedTest
//    @MethodSource("basicOperationsSupplier")
    public final void basicOperations(Consumer<JsonWriter> operation, String expectedJson) {
        operation.accept(getJsonWriter());

        assertEquals(expectedJson, getJsonWriterContents());
    }

    private static Stream<Arguments> basicOperationsSupplier() {
        return Stream.of(
            // Object start and end.
            Arguments.of(createJsonConsumer(JsonWriter::writeStartObject), "{}"),
            Arguments.of(createJsonConsumer(JsonWriter::writeEndObject), "}"),

            // Array start and end.
            Arguments.of(createJsonConsumer(JsonWriter::writeStartArray), "["),
            Arguments.of(createJsonConsumer(JsonWriter::writeEndArray), "]"),

            // Field name.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFieldName("fieldName")), "\"fieldName\":"),

            // Value handling.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBinary(null)), "null"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBinary(new byte[0])), ""),
            Arguments.of(createJsonConsumer(
                jsonWriter -> jsonWriter.writeBinary("Hello".getBytes(StandardCharsets.UTF_8))), "Hello"),

            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBoolean(true)), "true"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBoolean(false)), "false"),

            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeDouble(-42D)), "-42"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeDouble(-42.0D)), "-42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeDouble(42D)), "42"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeDouble(42.0D)), "42.0"),

            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFloat(-42F)), "-42"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFloat(-42.0F)), "-42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFloat(42F)), "42"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFloat(42.0F)), "42.0"),

            Arguments.of(),
            Arguments.of(),

            Arguments.of(),
            Arguments.of(),

            Arguments.of(),

            Arguments.of(),
            Arguments.of(),

            Arguments.of(),
            Arguments.of(),
            Arguments.of(),
            Arguments.of(),

            // Field name and value.
            Arguments.of(),
            Arguments.of(),
            Arguments.of(),

            Arguments.of(),
            Arguments.of(),

            Arguments.of(),
            Arguments.of(),
            Arguments.of(),
            Arguments.of(),

            Arguments.of(),
            Arguments.of(),
            Arguments.of(),
            Arguments.of(),

            Arguments.of(),
            Arguments.of(),

            Arguments.of(),
            Arguments.of(),

            Arguments.of(),

            Arguments.of(),
            Arguments.of(),

            Arguments.of(),
            Arguments.of(),
            Arguments.of(),
            Arguments.of()
        );
    }

//    @ParameterizedTest
//    @MethodSource("basicExceptionsSupplier")
    public final void basicExceptions(Executable exceptionFunction, Class<? extends Throwable> expectedException) {
        assertThrows(expectedException, exceptionFunction);
    }

    private static Stream<Arguments> basicExceptionsSupplier() {
        return Stream.of(

        );
    }

    private static Consumer<JsonWriter> createJsonConsumer(Function<JsonWriter, JsonWriter> consumptionFunction) {
        return jsonWriter -> consumptionFunction.apply(jsonWriter).flush();
    }

    private static Executable createJsonExecutable(Runnable exceptionFunction) {
        return exceptionFunction::run;
    }
}
