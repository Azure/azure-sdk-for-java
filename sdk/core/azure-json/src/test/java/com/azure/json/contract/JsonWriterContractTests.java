// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.contract;

import com.azure.json.JsonWriteState;
import com.azure.json.JsonWriter;
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
    public void basicOperations(Consumer<JsonWriter> operation, String expectedJson) {
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
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBoolean(null)), "null"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBoolean(null, true)), "null"),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBoolean(null, false)), ""),

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
    public void basicExceptions(Consumer<JsonWriter> operation, Class<? extends Throwable> expectedException) {
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

    @ParameterizedTest
    @MethodSource("basicWriteStateSupplier")
    public void basicWriteState(Consumer<JsonWriter> operation, JsonWriteState expectedState) {
        JsonWriter writer = getJsonWriter();

        operation.accept(writer);

        assertEquals(expectedState, writer.getWriteContext().getWriteState());
    }

    private static Stream<Arguments> basicWriteStateSupplier() {
        return Stream.of(
            // Initial state is root.
            Arguments.of(createJsonConsumer(Function.identity()), JsonWriteState.ROOT),

            // Starting an object enters OBJECT state.
            Arguments.of(createJsonConsumer(JsonWriter::writeStartObject), JsonWriteState.OBJECT),

            // Starting an array enters ARRAY state.
            Arguments.of(createJsonConsumer(JsonWriter::writeStartArray), JsonWriteState.ARRAY),

            // Writing a simple value at ROOT enters COMPLETED state.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBinary(null)), JsonWriteState.COMPLETED),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeBoolean(true)), JsonWriteState.COMPLETED),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeDouble(0.0D)), JsonWriteState.COMPLETED),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeFloat(0.0F)), JsonWriteState.COMPLETED),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeInt(0)), JsonWriteState.COMPLETED),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeLong(0L)), JsonWriteState.COMPLETED),
            Arguments.of(createJsonConsumer(JsonWriter::writeNull), JsonWriteState.COMPLETED),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeString(null)), JsonWriteState.COMPLETED),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeRawValue("\"\"")), JsonWriteState.COMPLETED),

            // Writing a value into an array maintains ARRAY state.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeBinary(null)),
                JsonWriteState.ARRAY),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeBoolean(true)),
                JsonWriteState.ARRAY),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeDouble(0.0D)),
                JsonWriteState.ARRAY),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeFloat(0.0F)),
                JsonWriteState.ARRAY),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeInt(0)),
                JsonWriteState.ARRAY),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeLong(0L)),
                JsonWriteState.ARRAY),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeNull()),
                JsonWriteState.ARRAY),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeString(null)),
                JsonWriteState.ARRAY),
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeRawValue("\"\"")),
                JsonWriteState.ARRAY),

            // Ending an object at ROOT enters COMPLETED state.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartObject().writeEndObject()),
                JsonWriteState.COMPLETED),

            // Ending an array at ROOT enters COMPLETED state.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeEndArray()),
                JsonWriteState.COMPLETED),

            // Writing an object in an array enters OBJECT state.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeStartObject()),
                JsonWriteState.OBJECT),

            // Writing an array in an array maintains ARRAY state.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartArray().writeStartArray()),
                JsonWriteState.ARRAY),

            // Closing an array contained in an array maintains ARRAY state.
            Arguments.of(createJsonConsumer(jsonWriter ->
                jsonWriter.writeStartArray().writeStartArray().writeEndArray()), JsonWriteState.ARRAY),

            // Writing a field name in an object enters FIELD_VALUE state.
            Arguments.of(createJsonConsumer(jsonWriter -> jsonWriter.writeStartObject().writeFieldName("fieldName")),
                JsonWriteState.FIELD),

            // Writing a field and value maintains OBJECT state.
            Arguments.of(createJsonConsumer(jsonWriter ->
                jsonWriter.writeStartObject().writeBinaryField("fieldName", null)), JsonWriteState.OBJECT),
            Arguments.of(createJsonConsumer(jsonWriter ->
                jsonWriter.writeStartObject().writeBooleanField("fieldName", true)), JsonWriteState.OBJECT),
            Arguments.of(createJsonConsumer(jsonWriter ->
                jsonWriter.writeStartObject().writeDoubleField("fieldName", 0.0D)), JsonWriteState.OBJECT),
            Arguments.of(createJsonConsumer(jsonWriter ->
                jsonWriter.writeStartObject().writeFloatField("fieldName", 0.0F)), JsonWriteState.OBJECT),
            Arguments.of(createJsonConsumer(jsonWriter ->
                jsonWriter.writeStartObject().writeIntField("fieldName", 0)), JsonWriteState.OBJECT),
            Arguments.of(createJsonConsumer(jsonWriter ->
                jsonWriter.writeStartObject().writeLongField("fieldName", 0L)), JsonWriteState.OBJECT),
            Arguments.of(createJsonConsumer(jsonWriter ->
                jsonWriter.writeStartObject().writeNullField("fieldName")), JsonWriteState.OBJECT),
            Arguments.of(createJsonConsumer(jsonWriter ->
                jsonWriter.writeStartObject().writeStringField("fieldName", null)), JsonWriteState.OBJECT),
            Arguments.of(createJsonConsumer(jsonWriter ->
                jsonWriter.writeStartObject().writeRawField("fieldName", "\"\"")), JsonWriteState.OBJECT),

            // Starting an object in FIELD_VALUE enters OBJECT state.
            Arguments.of(createJsonConsumer(jsonWriter ->
                jsonWriter.writeStartObject().writeFieldName("fieldName").writeStartObject()), JsonWriteState.OBJECT),

            // Starting an array in FIELD_VALUE enters ARRAY state.
            Arguments.of(createJsonConsumer(jsonWriter ->
                jsonWriter.writeStartObject().writeFieldName("fieldName").writeStartArray()), JsonWriteState.ARRAY),

            // Closing an object that is a field value enters OBJECT state.
            Arguments.of(createJsonConsumer(jsonWriter ->
                    jsonWriter.writeStartObject().writeFieldName("fieldName").writeStartObject().writeEndObject()),
                JsonWriteState.OBJECT),

            // Closing an array that is a field value enters OBJECT state.
            Arguments.of(createJsonConsumer(jsonWriter ->
                    jsonWriter.writeStartObject().writeFieldName("fieldName").writeStartArray().writeEndArray()),
                JsonWriteState.OBJECT)
        );
    }

    private static Consumer<JsonWriter> createJsonConsumer(Function<JsonWriter, JsonWriter> consumptionFunction) {
        return jsonWriter -> consumptionFunction.apply(jsonWriter).flush();
    }
}
