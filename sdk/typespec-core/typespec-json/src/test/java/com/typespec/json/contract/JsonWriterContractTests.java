// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json.contract;

import com.typespec.json.JsonWriteState;
import com.typespec.json.JsonWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    public void basicOperations(IOExceptionConsumer<JsonWriter> operation, String expectedJson) throws IOException {
        writeAndValidate(operation, expectedJson);
    }

    private static Stream<Arguments> basicOperationsSupplier() {
        return Stream.of(
            // Object start and end.
            Arguments.of(write(JsonWriter::writeStartObject), "{"),

            // End object has to have start object written before it.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeEndObject()), "{}"),

            // Array start and end.
            Arguments.of(write(JsonWriter::writeStartArray), "["),

            // End array has to have start array written before it.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeEndArray()), "[]"),

            // Field name has to happening in an object.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject()
                .writeFieldName("fieldName")
                .writeString("value")
                .writeEndObject()), "{\"fieldName\":\"value\"}"),

            // Value handling.

            // Binary
            Arguments.of(write(jsonWriter -> jsonWriter.writeBinary(null)), "null"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeBinary(new byte[0])), "\"\""),
            Arguments.of(write(jsonWriter -> jsonWriter.writeBinary("Hello".getBytes(StandardCharsets.UTF_8))),
                "\"" + Base64.getEncoder().encodeToString("Hello".getBytes(StandardCharsets.UTF_8)) + "\""),

            // Boolean
            Arguments.of(write(jsonWriter -> jsonWriter.writeBoolean(true)), "true"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeBoolean(false)), "false"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeBoolean(null)), "null"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeBoolean(Boolean.TRUE)), "true"),

            // Double
            Arguments.of(write(jsonWriter -> jsonWriter.writeDouble(-42D)), "-42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeDouble(-42.0D)), "-42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeDouble(42D)), "42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeDouble(42.0D)), "42.0"),

            // Float
            Arguments.of(write(jsonWriter -> jsonWriter.writeFloat(-42F)), "-42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeFloat(-42.0F)), "-42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeFloat(42F)), "42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeFloat(42.0F)), "42.0"),

            // Integer
            Arguments.of(write(jsonWriter -> jsonWriter.writeInt(42)), "42"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeInt(-42)), "-42"),

            // Long
            Arguments.of(write(jsonWriter -> jsonWriter.writeLong(42L)), "42"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeLong(-42L)), "-42"),

            // Null
            Arguments.of(write(JsonWriter::writeNull), "null"),

            // Number
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(null)), "null"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(-42D)), "-42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(-42.0D)), "-42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(42D)), "42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(42.0D)), "42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(-42F)), "-42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(-42.0F)), "-42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(42F)), "42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(42.0F)), "42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(42)), "42"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(-42)), "-42"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(42L)), "42"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(-42L)), "-42"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber((byte) 42)), "42"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber((short) 42)), "42"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(new AtomicInteger(-42))), "-42"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(new AtomicInteger(42))), "42"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(new AtomicLong(-42L))), "-42"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeNumber(new AtomicLong(42L))), "42"),

            // String
            Arguments.of(write(jsonWriter -> jsonWriter.writeString(null)), "null"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeString("")), "\"\""),
            Arguments.of(write(jsonWriter -> jsonWriter.writeString("null")), "\"null\""),

            // Raw
            Arguments.of(write(jsonWriter -> jsonWriter.writeRawValue("\"string\"")), "\"string\""),
            Arguments.of(write(jsonWriter -> jsonWriter.writeRawValue("42")), "42"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeRawValue("42.0")), "42.0"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeRawValue("true")), "true"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeRawValue("false")), "false"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeRawValue("null")), "null"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeRawValue("[]")), "[]"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeRawValue("[null]")), "[null]"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeRawValue("{}")), "{}"),
            Arguments.of(write(jsonWriter -> jsonWriter.writeRawValue("{null}")), "{null}"),


            // Field name and value.
            // Binary
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeBinaryField("field", null)), "{}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeBinaryField("field", new byte[0])),
                "{\"field\":\"\"}"),
            Arguments.of(writeField(
                    jsonWriter -> jsonWriter.writeBinaryField("field", "Hello".getBytes(StandardCharsets.UTF_8))),
                "{\"field\":\"" + Base64.getEncoder().encodeToString("Hello".getBytes(StandardCharsets.UTF_8)) + "\"}"),

            // Boolean
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeBooleanField("field", true)), "{\"field\":true}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeBooleanField("field", false)), "{\"field\":false}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeBooleanField("field", null)), "{}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeBooleanField("field", Boolean.TRUE)),
                "{\"field\":true}"),

            // Double
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeDoubleField("field", -42D)), "{\"field\":-42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeDoubleField("field", -42.0D)), "{\"field\":-42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeDoubleField("field", 42D)), "{\"field\":42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeDoubleField("field", 42.0D)), "{\"field\":42.0}"),

            // Float
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeFloatField("field", -42F)), "{\"field\":-42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeFloatField("field", -42.0F)), "{\"field\":-42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeFloatField("field", 42F)), "{\"field\":42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeFloatField("field", 42.0F)), "{\"field\":42.0}"),

            // Integer
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeIntField("field", 42)), "{\"field\":42}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeIntField("field", -42)), "{\"field\":-42}"),

            // Long
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeLongField("field", 42L)), "{\"field\":42}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeLongField("field", -42L)), "{\"field\":-42}"),

            // Null
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNullField("field")), "{\"field\":null}"),

            // Number
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", null)), "{}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", -42D)), "{\"field\":-42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", -42.0D)), "{\"field\":-42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", 42D)), "{\"field\":42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", 42.0D)), "{\"field\":42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", -42F)), "{\"field\":-42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", -42.0F)), "{\"field\":-42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", 42F)), "{\"field\":42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", 42.0F)), "{\"field\":42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", 42)), "{\"field\":42}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", -42)), "{\"field\":-42}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", 42L)), "{\"field\":42}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", -42L)), "{\"field\":-42}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", new AtomicInteger(42))),
                "{\"field\":42}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", new AtomicInteger(-42))),
                "{\"field\":-42}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", new AtomicLong(42L))),
                "{\"field\":42}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeNumberField("field", new AtomicLong(-42L))),
                "{\"field\":-42}"),

            // String
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeStringField("field", null)), "{}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeStringField("field", "")), "{\"field\":\"\"}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeStringField("field", "null")),
                "{\"field\":\"null\"}"),

            // Raw
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeRawField("field", "\"string\"")),
                "{\"field\":\"string\"}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeRawField("field", "42")), "{\"field\":42}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeRawField("field", "42.0")), "{\"field\":42.0}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeRawField("field", "true")), "{\"field\":true}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeRawField("field", "false")), "{\"field\":false}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeRawField("field", "null")), "{\"field\":null}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeRawField("field", "[]")), "{\"field\":[]}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeRawField("field", "[null]")), "{\"field\":[null]}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeRawField("field", "{}")), "{\"field\":{}}"),
            Arguments.of(writeField(jsonWriter -> jsonWriter.writeRawField("field", "{null}")), "{\"field\":{null}}")
        );
    }

    @ParameterizedTest
    @MethodSource("basicExceptionsSupplier")
    public void basicExceptions(IOExceptionConsumer<JsonWriter> operation,
        Class<? extends Throwable> expectedException) {
        assertThrows(expectedException, () -> operation.accept(getJsonWriter()));
    }

    private static Stream<Arguments> basicExceptionsSupplier() {
        return Stream.of(
            // IllegalStateException will be thrown if the write operation isn't allowed based on the current writing
            // context.

            // Root allows start array, start object, and field value, so end array, end object, field name, and
            // field name and value will throw an exception.
            Arguments.of(write(JsonWriter::writeEndArray), IllegalStateException.class),
            Arguments.of(write(JsonWriter::writeEndObject), IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeFieldName("fieldName")), IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStringField("fieldName", "fieldValue")),
                IllegalStateException.class),

            // Start object allows start object, end object, field name, and field name and value, so start array, end
            // array, and simple value will throw an exception.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeStartArray()),
                IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeEndArray()),
                IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeInt(0)), IllegalStateException.class),

            // Start array allows start array, end array, start object, and simple value, so end object, field name,
            // and field name and value will throw an exception.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeEndObject()),
                IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeFieldName("fieldName")),
                IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeIntField("fieldName", 0)),
                IllegalStateException.class),

            // Field value allows start array, start object, and simple value, so end array, end object, field name,
            // and field name and value will throw an exception.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject()
                .writeFieldName("fieldName")
                .writeEndArray()), IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject()
                .writeFieldName("fieldName")
                .writeEndObject()), IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject()
                .writeFieldName("fieldName")
                .writeFieldName("anotherFieldName")), IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject()
                .writeFieldName("fieldName")
                .writeIntField("anotherFieldName", 0)), IllegalStateException.class),

            // Completed doesn't allow any additional writing operations.
            Arguments.of(write(jsonWriter -> jsonWriter.writeInt(0).writeStartArray()), IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeInt(0).writeEndArray()), IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeInt(0).writeStartObject()), IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeInt(0).writeEndObject()), IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeInt(0).writeFieldName("fieldName")),
                IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeInt(0).writeInt(0)), IllegalStateException.class),
            Arguments.of(write(jsonWriter -> jsonWriter.writeInt(0).writeIntField("fieldName", 0)),
                IllegalStateException.class),

            // Closing the writer on any state other than completed throws exceptions.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().close()), IllegalStateException.class),

            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().close()), IllegalStateException.class),

            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject()
                .writeFieldName("fieldName").close()), IllegalStateException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("basicWriteStateSupplier")
    public void basicWriteState(IOExceptionConsumer<JsonWriter> operation, JsonWriteState expectedState) {
        JsonWriter writer = getJsonWriter();

        assertDoesNotThrow(() -> operation.accept(writer));

        assertEquals(expectedState, writer.getWriteContext().getWriteState());
    }

    private static Stream<Arguments> basicWriteStateSupplier() {
        return Stream.of(
            // Initial state is root.
            Arguments.of(write(jsonWriter -> {
            }), JsonWriteState.ROOT),

            // Starting an object enters OBJECT state.
            Arguments.of(write(JsonWriter::writeStartObject), JsonWriteState.OBJECT),

            // Starting an array enters ARRAY state.
            Arguments.of(write(JsonWriter::writeStartArray), JsonWriteState.ARRAY),

            // Writing a simple value at ROOT enters COMPLETED state.
            Arguments.of(write(jsonWriter -> jsonWriter.writeBinary(null)), JsonWriteState.COMPLETED),
            Arguments.of(write(jsonWriter -> jsonWriter.writeBoolean(true)), JsonWriteState.COMPLETED),
            Arguments.of(write(jsonWriter -> jsonWriter.writeDouble(0.0D)), JsonWriteState.COMPLETED),
            Arguments.of(write(jsonWriter -> jsonWriter.writeFloat(0.0F)), JsonWriteState.COMPLETED),
            Arguments.of(write(jsonWriter -> jsonWriter.writeInt(0)), JsonWriteState.COMPLETED),
            Arguments.of(write(jsonWriter -> jsonWriter.writeLong(0L)), JsonWriteState.COMPLETED),
            Arguments.of(write(JsonWriter::writeNull), JsonWriteState.COMPLETED),
            Arguments.of(write(jsonWriter -> jsonWriter.writeString(null)), JsonWriteState.COMPLETED),
            Arguments.of(write(jsonWriter -> jsonWriter.writeRawValue("\"\"")), JsonWriteState.COMPLETED),

            // Writing a value into an array maintains ARRAY state.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeBinary(null)), JsonWriteState.ARRAY),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeBoolean(true)), JsonWriteState.ARRAY),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeDouble(0.0D)), JsonWriteState.ARRAY),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeFloat(0.0F)), JsonWriteState.ARRAY),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeInt(0)), JsonWriteState.ARRAY),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeLong(0L)), JsonWriteState.ARRAY),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeNull()), JsonWriteState.ARRAY),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeString(null)), JsonWriteState.ARRAY),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeRawValue("\"\"")), JsonWriteState.ARRAY),

            // Ending an object at ROOT enters COMPLETED state.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeEndObject()), JsonWriteState.COMPLETED),

            // Ending an array at ROOT enters COMPLETED state.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeEndArray()), JsonWriteState.COMPLETED),

            // Writing an object in an array enters OBJECT state.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeStartObject()), JsonWriteState.OBJECT),

            // Writing an array in an array maintains ARRAY state.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeStartArray()), JsonWriteState.ARRAY),

            // Closing an array contained in an array maintains ARRAY state.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartArray().writeStartArray().writeEndArray()),
                JsonWriteState.ARRAY),

            // Writing a field name in an object enters FIELD_VALUE state.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeFieldName("fieldName")),
                JsonWriteState.FIELD),

            // Writing a field and value maintains OBJECT state.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeBinaryField("fieldName", null)),
                JsonWriteState.OBJECT),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeBooleanField("fieldName", true)),
                JsonWriteState.OBJECT),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeDoubleField("fieldName", 0.0D)),
                JsonWriteState.OBJECT),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeFloatField("fieldName", 0.0F)),
                JsonWriteState.OBJECT),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeIntField("fieldName", 0)),
                JsonWriteState.OBJECT),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeLongField("fieldName", 0L)),
                JsonWriteState.OBJECT),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeNullField("fieldName")),
                JsonWriteState.OBJECT),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeStringField("fieldName", null)),
                JsonWriteState.OBJECT),
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject().writeRawField("fieldName", "\"\"")),
                JsonWriteState.OBJECT),

            // Starting an object in FIELD_VALUE enters OBJECT state.
            Arguments.of(write(jsonWriter ->
                jsonWriter.writeStartObject().writeFieldName("fieldName").writeStartObject()), JsonWriteState.OBJECT),

            // Starting an array in FIELD_VALUE enters ARRAY state.
            Arguments.of(write(jsonWriter ->
                jsonWriter.writeStartObject().writeFieldName("fieldName").writeStartArray()), JsonWriteState.ARRAY),

            // Closing an object that is a field value enters OBJECT state.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject()
                .writeFieldName("fieldName").writeStartObject().writeEndObject()), JsonWriteState.OBJECT),

            // Closing an array that is a field value enters OBJECT state.
            Arguments.of(write(jsonWriter -> jsonWriter.writeStartObject()
                .writeFieldName("fieldName").writeStartArray().writeEndArray()), JsonWriteState.OBJECT)
        );
    }

    @ParameterizedTest
    @MethodSource("nullPointerExceptionsSupplier")
    public void nullPointerExceptions(IOExceptionConsumer<JsonWriter> consumer) {
        assertThrows(NullPointerException.class, () -> consumer.accept(getJsonWriter()));
    }

    private static Stream<IOExceptionConsumer<JsonWriter>> nullPointerExceptionsSupplier() {
        return Stream.of(
            write(jsonWriter -> jsonWriter.writeStartObject(null)),
            write(jsonWriter -> jsonWriter.writeStartArray(null)),
            write(jsonWriter -> jsonWriter.writeFieldName(null)),
            write(jsonWriter -> jsonWriter.writeArray((Object[]) null, null)),
            write(jsonWriter -> jsonWriter.writeArray(Collections.emptyList(), null)),
            write(jsonWriter -> jsonWriter.writeMap(null, null)),
            write(jsonWriter -> jsonWriter.writeRawValue(null)),

            write(jsonWriter -> jsonWriter.writeNullableField(null, null, JsonWriter::writeUntyped)),
            write(jsonWriter -> jsonWriter.writeNullableField("field", null, null)),
            write(jsonWriter -> jsonWriter.writeJsonField(null, null)),
            write(jsonWriter -> jsonWriter.writeArrayField(null, (Object[]) null, JsonWriter::writeUntyped)),
            write(jsonWriter -> jsonWriter.writeArrayField("field", (Object[]) null, null)),
            write(jsonWriter -> jsonWriter.writeArrayField(null, Collections.emptyList(), JsonWriter::writeUntyped)),
            write(jsonWriter -> jsonWriter.writeArrayField("field", (List<Object>) null, null)),
            write(jsonWriter -> jsonWriter.writeMapField(null, null, JsonWriter::writeUntyped)),
            write(jsonWriter -> jsonWriter.writeMapField("field", null, null)),

            write(jsonWriter -> jsonWriter.writeBinaryField(null, null)),
            write(jsonWriter -> jsonWriter.writeBooleanField(null, false)),
            write(jsonWriter -> jsonWriter.writeBooleanField(null, null)),
            write(jsonWriter -> jsonWriter.writeDoubleField(null, 0.0D)),
            write(jsonWriter -> jsonWriter.writeFloatField(null, 0.0F)),
            write(jsonWriter -> jsonWriter.writeIntField(null, 0)),
            write(jsonWriter -> jsonWriter.writeLongField(null, 0L)),
            write(jsonWriter -> jsonWriter.writeNullField(null)),
            write(jsonWriter -> jsonWriter.writeNumberField(null, null)),
            write(jsonWriter -> jsonWriter.writeStringField(null, null)),
            write(jsonWriter -> jsonWriter.writeRawField(null, "0")),
            write(jsonWriter -> jsonWriter.writeRawField("field", null)),
            write(jsonWriter -> jsonWriter.writeUntypedField(null, null))
        );
    }

    @Test
    public void writeStartObjectNullFieldName() {
        assertThrows(NullPointerException.class, () -> getJsonWriter().writeStartObject(null));
    }

    @Test
    public void writeStartObjectWithFieldName() throws IOException {
        writeAndValidate(writeField(writer -> writer.writeStartObject("objectWithFieldName").writeEndObject()),
            "{\"objectWithFieldName\":{}}");
    }

    @Test
    public void writeStartArrayNullFieldName() {
        assertThrows(NullPointerException.class, () -> getJsonWriter().writeStartArray(null));
    }

    @Test
    public void writeStartArrayWithFieldName() throws IOException {
        writeAndValidate(writeField(writer -> writer.writeStartArray("arrayWithFieldName").writeEndArray()),
            "{\"arrayWithFieldName\":[]}");
    }

    @Test
    public void writeJsonFieldNullFieldNameThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> getJsonWriter().writeJsonField(null, null));
    }

    @ParameterizedTest
    @MethodSource("writeJsonSupplier")
    public void writeJson(IOExceptionConsumer<JsonWriter> write, String expected) throws IOException {
        writeAndValidate(write, expected);
    }

    private static Stream<Arguments> writeJsonSupplier() {
        SimpleSerializable serializable = new SimpleSerializable(true, 42, 42.0, "hello");
        return Stream.of(
            Arguments.of(write(writer -> writer.writeJson(null)), ""),
            Arguments.of(write(writer -> writer.writeJson(serializable)),
                "{\"boolean\":true,\"int\":42,\"decimal\":42.0,\"string\":\"hello\"}"),
            Arguments.of(writeField(writer -> writer.writeJsonField("field", null)), "{}"),
            Arguments.of(writeField(writer -> writer.writeJsonField("field", serializable)),
                "{\"field\":{\"boolean\":true,\"int\":42,\"decimal\":42.0,\"string\":\"hello\"}}")
        );
    }

    @Test
    public void writeArrayThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> getJsonWriter().writeArray(new Object[0], null));
        assertThrows(NullPointerException.class, () -> getJsonWriter().writeArray(Collections.emptyList(), null));

        assertThrows(NullPointerException.class, () -> getJsonWriter().writeArrayField(null, new Object[0], null));
        assertThrows(NullPointerException.class, () -> getJsonWriter().writeArrayField(null, Collections.emptyList(),
            null));

        assertThrows(NullPointerException.class, () -> getJsonWriter().writeArrayField("field", new Object[0], null));
        assertThrows(NullPointerException.class, () -> getJsonWriter().writeArrayField("field", Collections.emptyList(),
            null));
    }

    @ParameterizedTest
    @MethodSource("writeArraySupplier")
    public void writeArray(IOExceptionConsumer<JsonWriter> write, String expected) throws IOException {
        writeAndValidate(write, expected);
    }

    private static Stream<Arguments> writeArraySupplier() {
        Object[] array = new Object[]{true, 42, 42.0, "hello"};
        String expected = "[true,42,42.0,\"hello\"]";
        return Stream.of(
            Arguments.of(write(writer -> writer.writeArray((Object[]) null, JsonWriter::writeUntyped)), "null"),
            Arguments.of(write(writer -> writer.writeArray(new Object[0], JsonWriter::writeUntyped)), "[]"),
            Arguments.of(write(writer -> writer.writeArray(array, JsonWriter::writeUntyped)), expected),

            Arguments.of(write(writer -> writer.writeArray((List<Object>) null, JsonWriter::writeUntyped)), "null"),
            Arguments.of(write(writer -> writer.writeArray(Collections.emptyList(), JsonWriter::writeUntyped)), "[]"),
            Arguments.of(write(writer -> writer.writeArray(Arrays.asList(array), JsonWriter::writeUntyped)), expected),

            Arguments.of(write(writer -> writer.writeArrayField("field", (Object[]) null, JsonWriter::writeUntyped)),
                ""),
            Arguments.of(writeField(writer -> writer.writeArrayField("field", new Object[0], JsonWriter::writeUntyped)),
                "{\"field\":[]}"),
            Arguments.of(writeField(writer -> writer.writeArrayField("field", array, JsonWriter::writeUntyped)),
                "{\"field\":" + expected + "}"),

            Arguments.of(write(writer ->
                writer.writeArrayField("field", (List<Object>) null, JsonWriter::writeUntyped)), ""),
            Arguments.of(writeField(writer ->
                writer.writeArrayField("field", Collections.emptyList(), JsonWriter::writeUntyped)), "{\"field\":[]}"),
            Arguments.of(writeField(writer -> writer.writeArray(Arrays.asList(array), JsonWriter::writeUntyped)),
                "{\"field\":" + expected + "}")
        );
    }

    @Test
    public void writeMapThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> getJsonWriter().writeMap(Collections.emptyMap(), null));
        assertThrows(NullPointerException.class, () -> getJsonWriter().writeMapField(null, Collections.emptyMap(),
            null));
        assertThrows(NullPointerException.class, () -> getJsonWriter().writeMapField("field", Collections.emptyMap(),
            null));
    }

    @ParameterizedTest
    @MethodSource("writeMapSupplier")
    public void writeMap(IOExceptionConsumer<JsonWriter> write, String expected) throws IOException {
        writeAndValidate(write, expected);
    }

    private static Stream<Arguments> writeMapSupplier() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("boolean", true);
        map.put("int", 42);
        map.put("decimal", 42.0D);
        map.put("string", "hello");
        String expected = "{\"boolean\":true,\"int\":42,\"decimal\":42.0,\"string\":\"hello\"}";

        return Stream.of(
            Arguments.of(write(writer -> writer.writeMap(null, JsonWriter::writeUntyped)), "null"),
            Arguments.of(write(writer -> writer.writeMap(Collections.emptyMap(), JsonWriter::writeUntyped)), "{}"),
            Arguments.of(write(writer -> writer.writeMap(map, JsonWriter::writeUntyped)), expected),

            Arguments.of(write(writer -> writer.writeMapField("field", null, JsonWriter::writeUntyped)), ""),
            Arguments.of(writeField(writer ->
                writer.writeMapField("field", Collections.emptyMap(), JsonWriter::writeUntyped)), "{\"field\":{}}"),
            Arguments.of(writeField(writer -> writer.writeMapField("field", map, JsonWriter::writeUntyped)),
                "{\"field\":" + expected + "}")
        );
    }

    @ParameterizedTest
    @MethodSource("writeUntypedSupplier")
    public void writeUntyped(Object value, String expectedJson) throws IOException {
        writeAndValidate(writer -> writer.writeUntyped(value), expectedJson);
    }

    @ParameterizedTest
    @MethodSource("writeUntypedSupplier")
    public void writeUntypedField(Object value, String expectedUntypedJson) throws IOException {
        writeAndValidate(writeField(writer -> writer.writeUntypedField("untyped", value)),
            "{\"untyped\":" + expectedUntypedJson + "}");
    }

    private static Stream<Arguments> writeUntypedSupplier() {
        byte[] bytes = new byte[]{0, 1, 2, 3};
        UUID uuid = UUID.randomUUID();

        return Stream.of(
            Arguments.of(null, "null"),
            Arguments.of((short) 42, "42"),
            Arguments.of(42, "42"),
            Arguments.of(42L, "42"),
            Arguments.of(42.0F, "42.0"),
            Arguments.of(42.0D, "42.0"),
            Arguments.of(true, "true"),
            Arguments.of(bytes, "\"" + Base64.getEncoder().encodeToString(bytes) + "\""),
            Arguments.of("hello", "\"hello\""),
            Arguments.of('h', "\"h\""),
            Arguments.of(new SimpleSerializable(true, 42, 42.0D, "hello"),
                "{\"boolean\":true,\"int\":42,\"decimal\":42.0,\"string\":\"hello\"}"),
            Arguments.of(new Object[0], "[]"),
            Arguments.of(new Object[]{null, 42, 42.0, true, "hello", 'h'}, "[null,42,42.0,true,\"hello\",\"h\"]"),
            Arguments.of(Collections.emptyList(), "[]"),
            Arguments.of(Arrays.asList(null, 42, 42.0, true, "hello", 'h'), "[null,42,42.0,true,\"hello\",\"h\"]"),
            Arguments.of(Collections.singletonMap("hello", "json"), "{\"hello\":\"json\"}"),
            Arguments.of(new Object(), "{}"),
            Arguments.of(uuid, "\"" + uuid + "\"")
        );
    }

    @Test
    public void writeNullableNullWriterFuncThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> getJsonWriter().writeNullableField("field", null, null));
    }

    @Test
    public void writeNullableFieldNullValueWritesJsonNull() throws IOException {
        writeAndValidate(writeField(writer -> writer.writeNullableField("field", null, JsonWriter::writeUntyped)),
            "{\"field\":null}");
    }

    @Test
    public void writeNullableFieldNonNullValueWritesJsonField() throws IOException {
        writeAndValidate(writer -> writer.writeStartObject()
            .writeNullableField("field", "hello", JsonWriter::writeString)
            .writeEndObject(), "{\"field\":\"hello\"}");
    }

    private void writeAndValidate(IOExceptionConsumer<JsonWriter> write, String expected) throws IOException {
        try (JsonWriter writer = getJsonWriter()) {
            write.accept(writer);
            writer.flush();

            assertEquals(expected, getJsonWriterContents());
        } catch (IllegalStateException ignored) {
            // Ignore IllegalStateException if JsonWriter is closed in an invalid state.
        }
    }

    private static IOExceptionConsumer<JsonWriter> write(IOExceptionConsumer<JsonWriter> callback) {
        return callback;
    }

    private static IOExceptionConsumer<JsonWriter> writeField(IOExceptionConsumer<JsonWriter> callback) {
        return writer -> {
            writer.writeStartObject();
            callback.accept(writer);
            writer.writeEndObject();
        };
    }

    private interface IOExceptionConsumer<T> {
        void accept(T t) throws IOException;
    }
}
