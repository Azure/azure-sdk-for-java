// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class JsonCreationTests {

    //Part 1: Simple Creation of Objects
    @Test
    public void createJsonObject() {
        JsonObject test = new JsonObject();
        assertTrue(test.isObject());
    }

    @Test
    public void createJsonString() {
        JsonString test = new JsonString("");
        assertTrue(test.isString());
    }

    @Test
    public void createJsonNumber() {
        JsonNumber test = new JsonNumber(0);
        assertTrue(test.isNumber());
    }

    @Test
    public void createJsonBoolean() {
        JsonBoolean test = JsonBoolean.getInstance(true);
        assertTrue(test.isBoolean());
    }

    @Test
    public void createJsonNull() {
        JsonNull test = JsonNull.getInstance();
        assertTrue(test.isNull());
    }

    @Test
    public void createJsonArray() {
        JsonArray test = new JsonArray();
        assertTrue(test.isArray());
    }

    //Part 2: Test that created object is not any of the other types.
    //2.1: Object
    @Test
    public void jsonObjectNotJsonString() {
        JsonObject test = new JsonObject();
        assertFalse(test.isString());
    }

    @Test
    public void jsonObjectNotJsonNumber() {
        JsonObject test = new JsonObject();
        assertFalse(test.isNumber());
    }

    @Test
    public void jsonObjectNotJsonBoolean() {
        JsonObject test = new JsonObject();
        assertFalse(test.isBoolean());
    }

    @Test
    public void jsonObjectNotJsonNull() {
        JsonObject test = new JsonObject();
        assertFalse(test.isNull());
    }

    @Test
    public void jsonObjectNotJsonArray() {
        JsonObject test = new JsonObject();
        assertFalse(test.isArray());
    }

    //2.2: Number
    @Test
    public void jsonNumberNotJsonString() {
        JsonNumber test = new JsonNumber(0);
        assertFalse(test.isString());
    }

    @Test
    public void jsonNumberNotJsonObject() {
        JsonNumber test = new JsonNumber(0);
        assertFalse(test.isObject());
    }

    @Test
    public void jsonNumberNotJsonBoolean() {
        JsonNumber test = new JsonNumber(0);
        assertFalse(test.isBoolean());
    }

    @Test
    public void jsonNumberNotJsonNull() {
        JsonNumber test = new JsonNumber(0);
        assertFalse(test.isNull());
    }

    @Test
    public void jsonNumberNotJsonArray() {
        JsonNumber test = new JsonNumber(0);
        assertFalse(test.isArray());
    }

    // Cannot construct JsonNumber from null Number object
    @Test
    public void jsonNumberNotNull() {
        assertThrows(IllegalArgumentException.class, () -> new JsonNumber((Number) null));
    }

    // Cannot construct JsonNumber from non-parseable int or float value
    // NOTE: Strings storing octal representations such as 010 (8 in decimal)
    // are parseable - they will just be interpreted as decimal with the
    // preceeding 0s removed and therefore parseable; however, binary (0b or 0B)
    // and hexadecimal (0x or 0X) representations will not be parseable.
    @ParameterizedTest
    @ValueSource(
        strings = {
            "null",
            "true",
            "false",
            " 0 0 ",
            "0 1 2 3",
            "1. 2",
            "1 . 2",
            "1a1",
            "1-",
            "-1-",
            "abc",
            "-a",
            "0b01010101",
            "0B10101",
            "0x10",
            "0xFF",
            "0x1A",
            ".",
            "-." })
    public void jsonNumberNotValidIntOrFloatStrings(String value) {
        assertThrows(IllegalArgumentException.class, () -> {
            JsonNumber test = new JsonNumber(value);
        });
    }

    // Making sure JsonNumber does successfully construct with parseable int and
    // float String representations
    @ParameterizedTest
    @ValueSource(
        strings = {
            " 123",
            "123 ",
            " -123",
            "-123 ",
            "00000",
            "0123",
            "123",
            "-1934234",
            "929.12342",
            "-1.2345",
            ".12345",
            "0.0",
            "-.12345",
            ".0",
            "-.0",
            " .0",
            ".0 ",
            "1000000000000000000000000000000",
            "-1000000000000000000000000000000" })
    public void jsonNumberValidIntOrFloatStrings(String value) {
        assertDoesNotThrow(() -> {
            JsonNumber test = new JsonNumber(value);
        });
    }

    //2.3: Boolean
    @Test
    public void jsonBooleanNotJsonString() {
        JsonBoolean test = JsonBoolean.getInstance(true);
        assertFalse(test.isString());
    }

    @Test
    public void jsonBooleanNotJsonNumber() {
        JsonBoolean test = JsonBoolean.getInstance(true);
        assertFalse(test.isNumber());
    }

    @Test
    public void jsonBooleanNotJsonObject() {
        JsonBoolean test = JsonBoolean.getInstance(true);
        assertFalse(test.isObject());
    }

    @Test
    public void jsonBooleanNotJsonNull() {
        JsonBoolean test = JsonBoolean.getInstance(true);
        assertFalse(test.isNull());
    }

    @Test
    public void jsonBooleanNotJsonArray() {
        JsonBoolean test = JsonBoolean.getInstance(true);
        assertFalse(test.isArray());
    }

    //2.4: Null
    @Test
    public void jsonNullNotJsonString() {
        JsonNull test = JsonNull.getInstance();
        assertFalse(test.isString());
    }

    @Test
    public void jsonNullNotJsonNumber() {
        JsonNull test = JsonNull.getInstance();
        assertFalse(test.isNumber());
    }

    @Test
    public void jsonNullNotJsonBoolean() {
        JsonNull test = JsonNull.getInstance();
        assertFalse(test.isBoolean());
    }

    @Test
    public void jsonNullNotJsonObject() {
        JsonNull test = JsonNull.getInstance();
        assertFalse(test.isObject());
    }

    @Test
    public void jsonNullNotJsonArray() {
        JsonNull test = JsonNull.getInstance();
        assertFalse(test.isArray());
    }

    //2.5: Array
    @Test
    public void jsonArrayNotJsonString() {
        JsonArray test = new JsonArray();
        assertFalse(test.isString());
    }

    @Test
    public void jsonArrayNotJsonNumber() {
        JsonArray test = new JsonArray();
        assertFalse(test.isNumber());
    }

    @Test
    public void jsonArrayNotJsonBoolean() {
        JsonArray test = new JsonArray();
        assertFalse(test.isBoolean());
    }

    @Test
    public void jsonArrayNotJsonNull() {
        JsonArray test = new JsonArray();
        assertFalse(test.isNull());
    }

    @Test
    public void jsonArrayNotJsonObject() {
        JsonArray test = new JsonArray();
        assertFalse(test.isObject());
    }
}
