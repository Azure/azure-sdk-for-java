// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
 * Each test will only create a single instance of {@link JsonWriter} to simplify usage of {@link #getJsonWriter()} and
 * {@link #getJsonWriterContents()}.
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

    @ParameterizedTest
    @MethodSource("basicOperationsSupplier")
    public final void basicOperations(Consumer<JsonWriter> operation, String expectedJson) {
        operation.accept(getJsonWriter());

        assertEquals(expectedJson, getJsonWriterContents());
    }

    private static Stream<Arguments> basicOperationsSupplier() {
        return Stream.of(
            // Object start and end.
            Arguments.of(createJsonConsumer(JsonWriter::writeStartObject), "{"),

            // End object has to have start object written before it.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartObject().writeEndObject()), "{}"),

            // Array start and end.
            Arguments.of(createJsonConsumer(JsonWriter::writeStartArray), "["),

            // End array has to have start array written before it.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeEndArray()), "[]"),

            // Field name has to happening in an object.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartObject().writeFieldName("fieldName")
                .writeString("value").writeEndObject()), "{\"fieldName\":\"value\"}"),

            // Value handling.

            // Binary
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBinary(null)), "null"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBinary(new byte[0])), "\"\""),
            Arguments.of(createJsonConsumer(
                jsonWriter -> jsonWriter.writeBinary("Hello".getBytes(StandardCharsets.UTF_8))),
                "\"" + Base64.getEncoder().encodeToString("Hello".getBytes(StandardCharsets.UTF_8)) + "\""),

            // Boolean
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBoolean(true)), "true"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBoolean(false)), "false"),

            // Double
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeDouble(-42D)), "-42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeDouble(-42.0D)), "-42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeDouble(42D)), "42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeDouble(42.0D)), "42.0"),

            // Float
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFloat(-42F)), "-42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFloat(-42.0F)), "-42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFloat(42F)), "42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFloat(42.0F)), "42.0"),

            // Integer
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeInt(42)), "42"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeInt(-42)), "-42"),

            // Long
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeLong(42L)), "42"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeLong(-42L)), "-42"),

            // Null
            Arguments.of(createJsonConsumer(JsonWriter::writeNull), "null"),

            // String
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeString(null)), "null"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeString("")), "\"\""),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeString("null")), "\"null\""),

            // Raw
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("\"string\"")), "\"string\""),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("42")), "42"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("42.0")), "42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("true")), "true"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("false")), "false"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("null")), "null"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("[]")), "[]"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("[null]")), "[null]"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("{}")), "{}"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("{null}")), "{null}"),


            // Field name and value.
            // Binary
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBinary(null)), "null"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBinary(new byte[0])), "\"\""),
            Arguments.of(createJsonConsumer(
                jsonWriter -> jsonWriter.writeBinary("Hello".getBytes(StandardCharsets.UTF_8))),
                "\"" + Base64.getEncoder().encodeToString("Hello".getBytes(StandardCharsets.UTF_8)) + "\""),

            // Boolean
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBoolean(true)), "true"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBoolean(false)), "false"),

            // Double
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeDouble(-42D)), "-42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeDouble(-42.0D)), "-42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeDouble(42D)), "42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeDouble(42.0D)), "42.0"),

            // Float
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFloat(-42F)), "-42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFloat(-42.0F)), "-42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFloat(42F)), "42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFloat(42.0F)), "42.0"),

            // Integer
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeInt(42)), "42"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeInt(-42)), "-42"),

            // Long
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeLong(42L)), "42"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeLong(-42L)), "-42"),

            // Null
            Arguments.of(createJsonConsumer(JsonWriter::writeNull), "null"),

            // String
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeString(null)), "null"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeString("")), "\"\""),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeString("null")), "\"null\""),

            // Raw
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("\"string\"")), "\"string\""),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("42")), "42"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("42.0")), "42.0"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("true")), "true"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("false")), "false"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("null")), "null"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("[]")), "[]"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("[null]")), "[null]"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("{}")), "{}"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("{null}")), "{null}")
        );
    }

    @ParameterizedTest
    @MethodSource("basicExceptionsSupplier")
    public final void basicExceptions(Consumer<JsonWriter> operation, Class<? extends Throwable> expectedException) {
        assertThrows(expectedException, () -> operation.accept(getJsonWriter()));
    }

    private static Stream<Arguments> basicExceptionsSupplier() {
        return Stream.of(
            // IllegalStateException will be thrown if the write operation isn't allowed based on the current writing
            // context.

            // Root allows start array, start object, and field value, so end array, end object, field name, and
            // field name and value will throw an exception.
            Arguments.of(createJsonConsumer(JsonWriter::writeEndArray), IllegalStateException.class),
            Arguments.of(createJsonConsumer(JsonWriter::writeEndObject), IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFieldName("fieldName")),
                IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStringField("fieldName", "fieldValue")),
                IllegalStateException.class),

            // Start object allows start object, end object, field name, and field name and value, so start array, end
            // array, and simple value will throw an exception.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartObject().writeStartArray()),
                IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartObject().writeEndArray()),
                IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartObject().writeInt(0)),
                IllegalStateException.class),

            // Start array allows start array, end array, start object, and simple value, so end object, field name,
            // and field name and value will throw an exception.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeEndObject()),
                IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeFieldName("fieldName")),
                IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeIntField("fieldName", 0)),
                IllegalStateException.class),

            // Field value allows start array, start object, and simple value, so end array, end object, field name,
            // and field name and value will throw an exception.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartObject().writeFieldName("fieldName")
                .writeEndArray()), IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartObject().writeFieldName("fieldName")
                .writeEndObject()), IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartObject().writeFieldName("fieldName")
                .writeFieldName("anotherFieldName")), IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartObject().writeFieldName("fieldName")
                .writeIntField("anotherFieldName", 0)), IllegalStateException.class),

            // Completed doesn't allow any additional writing operations.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeInt(0).writeStartArray()),
                IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeInt(0).writeEndArray()),
                IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeInt(0).writeStartObject()),
                IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeInt(0).writeEndObject()),
                IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeInt(0).writeFieldName("fieldName")),
                IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeInt(0).writeInt(0)),
                IllegalStateException.class),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeInt(0).writeIntField("fieldName", 0)),
                IllegalStateException.class),

            // Closing the writer on any state other than completed throws exceptions.
            Arguments.of(createJsonConsumer(jsonWriter -> {
                try {
                    jsonWriter.writeStartObject().close();
                    return jsonWriter;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }), IllegalStateException.class),

            Arguments.of(createJsonConsumer(jsonWriter -> {
                try {
                    jsonWriter.writeStartArray().close();
                    return jsonWriter;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }), IllegalStateException.class),

            Arguments.of(createJsonConsumer(jsonWriter -> {
                try {
                    jsonWriter.writeStartObject().writeFieldName("fieldName").close();
                    return jsonWriter;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }), IllegalStateException.class)
        );
    }

    private static Consumer<JsonWriter> createJsonConsumer(Function<JsonWriter, JsonWriter> consumptionFunction) {
        return jsonWriter -> consumptionFunction.apply(jsonWriter).flush();
    }
}
