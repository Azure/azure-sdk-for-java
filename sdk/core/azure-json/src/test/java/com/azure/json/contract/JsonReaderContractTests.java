// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.contract;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
            Arguments.of("false", false, createJsonConsumer(JsonReader::getBoolean)),
            Arguments.of("true", true, createJsonConsumer(JsonReader::getBoolean)),
            Arguments.of("null", null, createJsonConsumer(reader -> reader.getNullable(JsonReader::getBoolean))),

            // Double
            Arguments.of("-42.0", -42D, createJsonConsumer(JsonReader::getDouble)),
            Arguments.of("-42", -42D, createJsonConsumer(JsonReader::getDouble)),
            Arguments.of("42.0", 42D, createJsonConsumer(JsonReader::getDouble)),
            Arguments.of("42", 42D, createJsonConsumer(JsonReader::getDouble)),
            Arguments.of("null", null, createJsonConsumer(reader -> reader.getNullable(JsonReader::getDouble))),

            // Float
            Arguments.of("-42.0", -42F, createJsonConsumer(JsonReader::getFloat)),
            Arguments.of("-42", -42F, createJsonConsumer(JsonReader::getFloat)),
            Arguments.of("42.0", 42F, createJsonConsumer(JsonReader::getFloat)),
            Arguments.of("42", 42F, createJsonConsumer(JsonReader::getFloat)),
            Arguments.of("null", null, createJsonConsumer(reader -> reader.getNullable(JsonReader::getFloat))),

            // Integer
            Arguments.of("-42", -42, createJsonConsumer(JsonReader::getInt)),
            Arguments.of("42", 42, createJsonConsumer(JsonReader::getInt)),
            Arguments.of("null", null, createJsonConsumer(reader -> reader.getNullable(JsonReader::getInt))),

            // Long
            Arguments.of("-42", -42L, createJsonConsumer(JsonReader::getLong)),
            Arguments.of("42", 42L, createJsonConsumer(JsonReader::getLong)),
            Arguments.of("null", null, createJsonConsumer(reader -> reader.getNullable(JsonReader::getLong))),

            // String
            Arguments.of("null", null, createJsonConsumer(JsonReader::getString)),
            Arguments.of("\"\"", "", createJsonConsumer(JsonReader::getString)),
            Arguments.of("\"hello\"", "hello", createJsonConsumer(JsonReader::getString))
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
            Arguments.of("null", null, createJsonConsumer(JsonReader::getBinary)),
            Arguments.of("\"\"", new byte[0], createJsonConsumer(JsonReader::getBinary)),
            Arguments.of("\"" + Base64.getEncoder().encodeToString("Hello".getBytes(StandardCharsets.UTF_8)) + "\"",
                "Hello".getBytes(StandardCharsets.UTF_8), createJsonConsumer(JsonReader::getBinary))
        );
    }

    @Test
    public void emptyObject() {
        String json = "{}";
        JsonReader reader = getJsonReader(json);

        assertJsonReaderStructInitialization(reader, JsonToken.START_OBJECT);

        while (reader.nextToken() != JsonToken.END_OBJECT) {
            fail("Empty object shouldn't have any non-END_OBJECT JsonTokens but found: " + reader.currentToken());
        }
    }

    @Test
    public void emptyArray() {
        String json = "[]";
        JsonReader reader = getJsonReader(json);

        assertJsonReaderStructInitialization(reader, JsonToken.START_ARRAY);

        while (reader.nextToken() != JsonToken.END_ARRAY) {
            fail("Empty array shouldn't have any non-END_ARRAY JsonTokens but found: " + reader.currentToken());
        }
    }

    @Test
    public void simpleObject() {
        String json = "{\"stringProperty\":\"string\",\"nullProperty\":null,\"integerProperty\":10,\"floatProperty\":10.0,\"booleanProperty\":true}";
        JsonReader reader = getJsonReader(json);

        assertJsonReaderStructInitialization(reader, JsonToken.START_OBJECT);

        String stringProperty = null;
        boolean hasNullProperty = false;
        int integerProperty = 0;
        float floatProperty = 0.0F;
        boolean booleanProperty = false;
        while (reader.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = reader.getFieldName();
            reader.nextToken();

            if ("stringProperty".equals(fieldName)) {
                stringProperty = reader.getString();
            } else if ("nullProperty".equals(fieldName)) {
                hasNullProperty = true;
            } else if ("integerProperty".equals(fieldName)) {
                integerProperty = reader.getInt();
            } else if ("floatProperty".equals(fieldName)) {
                floatProperty = reader.getFloat();
            } else if ("booleanProperty".equals(fieldName)) {
                booleanProperty = reader.getBoolean();
            } else {
                fail("Unknown property name: '" + fieldName + "'");
            }
        }

        assertEquals("string", stringProperty);
        assertTrue(hasNullProperty, "Didn't find the expected 'nullProperty'.");
        assertEquals(10, integerProperty);
        assertEquals(10.0F, floatProperty);
        assertEquals(true, booleanProperty);
    }

    @Test
    public void arrayOfBasicTypesInJsonRoot() {
        String json = "[\"string\",null,10,10.0,true]";
        JsonReader reader = getJsonReader(json);

        assertJsonReaderStructInitialization(reader, JsonToken.START_ARRAY);

        Object[] jsonArray = new Object[5];
        int jsonArrayIndex = 0;
        while (reader.nextToken() != JsonToken.END_ARRAY) {
            jsonArray[jsonArrayIndex++] = ContractUtils.readUntypedField(reader);
        }

        assertEquals("string", jsonArray[0]);
        assertNull(jsonArray[1]);
        assertEquals(10, jsonArray[2]);
        assertEquals(10.0F, jsonArray[3]);
        assertEquals(true, jsonArray[4]);
    }

    @ParameterizedTest
    @MethodSource("objectWithInnerObjectSupplier")
    public void objectWithInnerObject(String json) {
        JsonReader reader = getJsonReader(json);

        assertJsonReaderStructInitialization(reader, JsonToken.START_OBJECT);

        String stringProperty = null;
        boolean hasNullProperty = false;
        int integerProperty = 0;
        float floatProperty = 0.0F;
        boolean booleanProperty = false;
        String innerStringProperty = null;
        while (reader.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = reader.getFieldName();
            reader.nextToken();

            if ("stringProperty".equals(fieldName)) {
                stringProperty = reader.getString();
            } else if ("nullProperty".equals(fieldName)) {
                hasNullProperty = true;
            } else if ("integerProperty".equals(fieldName)) {
                integerProperty = reader.getInt();
            } else if ("floatProperty".equals(fieldName)) {
                floatProperty = reader.getFloat();
            } else if ("booleanProperty".equals(fieldName)) {
                booleanProperty = reader.getBoolean();
            } else if ("innerObject".equals(fieldName)) {
                assertEquals(JsonToken.START_OBJECT, reader.currentToken());
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("innerStringProperty".equals(fieldName)) {
                        innerStringProperty = reader.getString();
                    } else {
                        fail("Unknown property name: '" + fieldName + "'");
                    }
                }
            } else {
                fail("Unknown property name: '" + fieldName + "'");
            }
        }

        assertEquals("string", stringProperty);
        assertTrue(hasNullProperty, "Didn't find the expected 'nullProperty'.");
        assertEquals(10, integerProperty);
        assertEquals(10.0F, floatProperty);
        assertEquals(true, booleanProperty);
        assertEquals("innerString", innerStringProperty);
    }

    private static Stream<Arguments> objectWithInnerObjectSupplier() {
        return Stream.of(
            Arguments.of(Named.of("objectWithInnerObjectAsFirstProperty",
                "{\"innerObject\":{\"innerStringProperty\":\"innerString\"},\"stringProperty\":\"string\","
                    + "\"nullProperty\":null,\"integerProperty\":10,\"floatProperty\":10.0,\"booleanProperty\":true}")),

            Arguments.of(Named.of("objectWithInnerObjectAsMiddleProperty",
                "{\"stringProperty\":\"string\",\"nullProperty\":null,\"integerProperty\":10,"
                    + "\"innerObject\":{\"innerStringProperty\":\"innerString\"},\"floatProperty\":10.0,"
                    + "\"booleanProperty\":true}")),

            Arguments.of(Named.of("objectWithInnerObjectAsLastProperty",
                "{\"stringProperty\":\"string\",\"nullProperty\":null,\"integerProperty\":10,\"floatProperty\":10.0,"
                    + "\"booleanProperty\":true,\"innerObject\":{\"innerStringProperty\":\"innerString\"}}"))
        );
    }

    @ParameterizedTest
    @MethodSource("objectWithInnerArraySupplier")
    public void objectWithInnerArray(String json) {
        JsonReader reader = getJsonReader(json);

        assertJsonReaderStructInitialization(reader, JsonToken.START_OBJECT);

        String stringProperty = null;
        boolean hasNullProperty = false;
        int integerProperty = 0;
        float floatProperty = 0.0F;
        boolean booleanProperty = false;
        String innerStringProperty = null;
        while (reader.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = reader.getFieldName();
            reader.nextToken();

            if ("stringProperty".equals(fieldName)) {
                stringProperty = reader.getString();
            } else if ("nullProperty".equals(fieldName)) {
                hasNullProperty = true;
            } else if ("integerProperty".equals(fieldName)) {
                integerProperty = reader.getInt();
            } else if ("floatProperty".equals(fieldName)) {
                floatProperty = reader.getFloat();
            } else if ("booleanProperty".equals(fieldName)) {
                booleanProperty = reader.getBoolean();
            } else if ("innerArray".equals(fieldName)) {
                assertEquals(JsonToken.START_ARRAY, reader.currentToken());
                while (reader.nextToken() != JsonToken.END_ARRAY) {
                    if (innerStringProperty != null) {
                        fail("Only expected one value in the inner array but found more.");
                    }
                    innerStringProperty = reader.getString();
                }
            } else {
                fail("Unknown property name: '" + fieldName + "'");
            }
        }

        assertEquals("string", stringProperty);
        assertTrue(hasNullProperty, "Didn't find the expected 'nullProperty'.");
        assertEquals(10, integerProperty);
        assertEquals(10.0F, floatProperty);
        assertEquals(true, booleanProperty);
        assertEquals("innerString", innerStringProperty);
    }

    private static Stream<Arguments> objectWithInnerArraySupplier() {
        return Stream.of(
            Arguments.of(Named.of("objectWithInnerArrayAsFirstProperty",
                "{\"innerArray\":[\"innerString\"],\"stringProperty\":\"string\",\"nullProperty\":null,"
                    + "\"integerProperty\":10,\"floatProperty\":10.0,\"booleanProperty\":true}")),

            Arguments.of(Named.of("objectWithInnerArrayAsMiddleProperty",
                "{\"stringProperty\":\"string\",\"nullProperty\":null,\"integerProperty\":10,"
                    + "\"innerArray\":[\"innerString\"],\"floatProperty\":10.0,\"booleanProperty\":true}")),

            Arguments.of(Named.of("objectWithInnerArrayAsLastProperty",
                "{\"stringProperty\":\"string\",\"nullProperty\":null,\"integerProperty\":10,\"floatProperty\":10.0,"
                    + "\"booleanProperty\":true,\"innerArray\":[\"innerString\"]}"))
        );
    }

    @ParameterizedTest
    @MethodSource("arrayWithInnerArraySupplier")
    public void arrayWithInnerArray(String json) {
        JsonReader reader = getJsonReader(json);

        assertJsonReaderStructInitialization(reader, JsonToken.START_ARRAY);

        Object[] jsonArray = new Object[6];
        int jsonArrayIndex = 0;
        while (reader.nextToken() != JsonToken.END_ARRAY) {
            if (reader.currentToken() == JsonToken.START_ARRAY) {
                while (reader.nextToken() != JsonToken.END_ARRAY) {
                    if (jsonArray[5] != null) {
                        fail("Only expected one value in the inner array but found more.");
                    }

                    jsonArray[5] = reader.getString();
                }
            } else {
                jsonArray[jsonArrayIndex++] = ContractUtils.readUntypedField(reader);
            }
        }

        assertEquals("string", jsonArray[0]);
        assertNull(jsonArray[1]);
        assertEquals(10, jsonArray[2]);
        assertEquals(10.0F, jsonArray[3]);
        assertEquals(true, jsonArray[4]);
        assertEquals("innerString", jsonArray[5]);
    }

    private static Stream<Arguments> arrayWithInnerArraySupplier() {
        return Stream.of(
            Arguments.of(Named.of("arrayWithInnerArrayAsFirstProperty",
                "[[\"innerString\"],\"string\",null,10,10.0,true]")),

            Arguments.of(Named.of("arrayWithInnerArrayAsMiddleProperty",
                "[\"string\",null,10,[\"innerString\"],10.0,true]")),

            Arguments.of(Named.of("arrayWithInnerArrayAsLastProperty",
                "[\"string\",null,10,10.0,true,[\"innerString\"]]"))
        );
    }

    @ParameterizedTest
    @MethodSource("arrayWithInnerObjectSupplier")
    public void arrayWithInnerObject(String json) {
        JsonReader reader = getJsonReader(json);

        assertJsonReaderStructInitialization(reader, JsonToken.START_ARRAY);

        Object[] jsonArray = new Object[6];
        int jsonArrayIndex = 0;
        while (reader.nextToken() != JsonToken.END_ARRAY) {
            if (reader.currentToken() == JsonToken.START_OBJECT) {
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("innerStringProperty".equals(fieldName)) {
                        jsonArray[5] = reader.getString();
                    } else {
                        fail("Unknown property name: '" + fieldName + "'");
                    }
                }
            } else {
                jsonArray[jsonArrayIndex++] = ContractUtils.readUntypedField(reader);
            }
        }

        assertEquals("string", jsonArray[0]);
        assertNull(jsonArray[1]);
        assertEquals(10, jsonArray[2]);
        assertEquals(10.0F, jsonArray[3]);
        assertEquals(true, jsonArray[4]);
        assertEquals("innerString", jsonArray[5]);
    }

    private static Stream<Arguments> arrayWithInnerObjectSupplier() {
        return Stream.of(
            Arguments.of(Named.of("arrayWithInnerObjectAsFirstProperty",
                "[{\"innerStringProperty\":\"innerString\"},\"string\",null,10,10.0,true]")),

            Arguments.of(Named.of("arrayWithInnerObjectAsMiddleProperty",
                "[\"string\",null,10,{\"innerStringProperty\":\"innerString\"},10.0,true]")),

            Arguments.of(Named.of("arrayWithInnerObjectAsLastProperty",
                "[\"string\",null,10,10.0,true,{\"innerStringProperty\":\"innerString\"}]"))
        );
    }

    @ParameterizedTest
    @MethodSource("bufferObjectSupplier")
    public void bufferObject(String json, int nextCount) {
        JsonReader reader = getJsonReader(json);

        for (int i = 0; i < nextCount; i++) {
            reader.nextToken();
        }

        JsonReader buffer = reader.bufferObject();
        TestData testData = TestData.fromJson(buffer);

        assertEquals("test", testData.getTest());
    }

    private static Stream<Arguments> bufferObjectSupplier() {
        return Stream.of(
            // Arguments.of("{\"test\":\"test\"}", 1),
            Arguments.of("{\"outerfield\":{\"test\":\"test\"}}", 2)
        );
    }

    @ParameterizedTest
    @MethodSource("bufferObjectIllegalStateSupplier")
    public void bufferObjectIllegalState(String json, int nextCount) {
        JsonReader reader = getJsonReader(json);

        for (int i = 0; i < nextCount; i++) {
            reader.nextToken();
        }

        assertThrows(IllegalStateException.class, reader::bufferObject);
    }

    private static Stream<Arguments> bufferObjectIllegalStateSupplier() {
        return Stream.of(
            Arguments.of("[]", 1),
            Arguments.of("12", 1),
            Arguments.of("null", 1),
            Arguments.of("true", 1),
            Arguments.of("\"hello\"", 1),
            Arguments.of("{\"outerfield\": []}", 2),
            Arguments.of("{\"outerfield\": 12}", 2),
            Arguments.of("{\"outerfield\": null}", 2),
            Arguments.of("{\"outerfield\": true}", 2),
            Arguments.of("{\"outerfield\": \"hello\"}", 2)
        );
    }

    private static void assertJsonReaderStructInitialization(JsonReader reader, JsonToken expectedInitialToken) {
        assertNull(reader.currentToken());
        reader.nextToken();

        assertEquals(expectedInitialToken, reader.currentToken());
    }

    private static <T> Function<JsonReader, T> createJsonConsumer(Function<JsonReader, T> func) {
        return func;
    }

    private static final class TestData implements JsonSerializable<TestData> {
        private String test;

        public String getTest() {
            return test;
        }

        public TestData setTest(String test) {
            this.test = test;
            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) {
            return jsonWriter.writeStartObject()
                .writeStringField("test", test)
                .writeEndObject();
        }

        public static TestData fromJson(JsonReader jsonReader) {
            return jsonReader.readObject(reader -> {
                TestData result = new TestData();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("test".equals(fieldName)) {
                        result.setTest(reader.getString());
                    } else {
                        reader.skipChildren();
                    }
                }

                return result;
            });
        }
    }
}
