// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.serializer.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link JsonNodeUtils}.
 */
public class JsonNodeUtilsTests {
    @AfterEach
    public void cleanupInlineMocks() {
        Mockito.framework().clearInlineMocks();
    }

    @ParameterizedTest
    @MethodSource("toGsonElementSupplier")
    public void toGsonElement(JsonNode jsonNode, JsonElement expected) {
        assertEquals(expected, JsonNodeUtils.toGsonElement(jsonNode));
    }

    private static Stream<Arguments> toGsonElementSupplier() {
        JsonArray jsonArray = new JsonArray();
        JsonNull jsonNull = JsonNull.INSTANCE;
        JsonObject jsonObject = new JsonObject();
        JsonPrimitive booleanNode = new JsonPrimitive(true);
        JsonPrimitive doubleNode = new JsonPrimitive(42D);
        JsonPrimitive floatNode = new JsonPrimitive(42F);
        JsonPrimitive intNode = new JsonPrimitive(42);
        JsonPrimitive longNode = new JsonPrimitive(42L);
        JsonPrimitive shortNode = new JsonPrimitive((short) 42);
        JsonPrimitive textNode = new JsonPrimitive("42");

        return Stream.of(
            Arguments.of(new GsonJsonArray(jsonArray), jsonArray),
            Arguments.of(new GsonJsonArray(), jsonArray),
            Arguments.of(GsonJsonNull.INSTANCE, jsonNull),
            Arguments.of(new GsonJsonObject(jsonObject), jsonObject),
            Arguments.of(new GsonJsonObject(), jsonObject),
            Arguments.of(new GsonJsonPrimitive(booleanNode), booleanNode),
            Arguments.of(new GsonJsonPrimitive(true), booleanNode),
            Arguments.of(new GsonJsonPrimitive(doubleNode), doubleNode),
            Arguments.of(new GsonJsonPrimitive(42D), doubleNode),
            Arguments.of(new GsonJsonPrimitive(floatNode), floatNode),
            Arguments.of(new GsonJsonPrimitive(42F), floatNode),
            Arguments.of(new GsonJsonPrimitive(intNode), intNode),
            Arguments.of(new GsonJsonPrimitive(42), intNode),
            Arguments.of(new GsonJsonPrimitive(longNode), longNode),
            Arguments.of(new GsonJsonPrimitive(42L), longNode),
            Arguments.of(new GsonJsonPrimitive(shortNode), shortNode),
            Arguments.of(new GsonJsonPrimitive((short) 42), shortNode),
            Arguments.of(new GsonJsonPrimitive(textNode), textNode),
            Arguments.of(new GsonJsonPrimitive("42"), textNode)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidToGsonElementSupplier")
    public void invalidToGsonElement(JsonNode jsonNode) {
        assertThrows(IllegalArgumentException.class, () -> JsonNodeUtils.toGsonElement(jsonNode));
    }

    private static Stream<Arguments> invalidToGsonElementSupplier() {
        // Azure Core JsonNode without a type used in conversion.
        JsonNode unknownNode = mock(JsonNode.class);
        when(unknownNode.isArray()).thenReturn(false);
        when(unknownNode.isNull()).thenReturn(false);
        when(unknownNode.isObject()).thenReturn(false);
        when(unknownNode.isValue()).thenReturn(false);

        // Azure Core JsonNode that is an array but isn't an expected GsonJsonArray.
        JsonNode unexpectedArray = mock(JsonNode.class);
        when(unexpectedArray.isArray()).thenReturn(true);
        when(unexpectedArray.isNull()).thenReturn(false);
        when(unexpectedArray.isObject()).thenReturn(false);
        when(unexpectedArray.isValue()).thenReturn(false);

        // Azure Core JsonNode that is null but isn't an expected GsonJsonNull.
        JsonNode unexpectedNull = mock(JsonNode.class);
        when(unexpectedNull.isArray()).thenReturn(false);
        when(unexpectedNull.isNull()).thenReturn(true);
        when(unexpectedNull.isObject()).thenReturn(false);
        when(unexpectedNull.isValue()).thenReturn(false);

        // Azure Core JsonNode that is an object but isn't an expected GsonJsonObject.
        JsonNode unexpectedObject = mock(JsonNode.class);
        when(unexpectedObject.isArray()).thenReturn(false);
        when(unexpectedObject.isNull()).thenReturn(false);
        when(unexpectedObject.isObject()).thenReturn(true);
        when(unexpectedObject.isValue()).thenReturn(false);

        // Azure Core JsonNode that is a value but isn't an expected GsonJsonValue.
        JsonNode unexpectedValue = mock(JsonNode.class);
        when(unexpectedValue.isArray()).thenReturn(false);
        when(unexpectedValue.isNull()).thenReturn(false);
        when(unexpectedValue.isObject()).thenReturn(false);
        when(unexpectedValue.isValue()).thenReturn(true);

        return Stream.of(
            Arguments.of(unknownNode),
            Arguments.of(unexpectedArray),
            Arguments.of(unexpectedNull),
            Arguments.of(unexpectedObject),
            Arguments.of(unexpectedValue)
        );
    }

    @ParameterizedTest
    @MethodSource("fromGsonElementSupplier")
    public void fromGsonElement(JsonElement jsonElement, JsonNode expected) {
        assertEquals(expected, JsonNodeUtils.fromGsonElement(jsonElement));
    }

    private static Stream<Arguments> fromGsonElementSupplier() {
        JsonArray jsonArray = new JsonArray();
        JsonNull jsonNull = JsonNull.INSTANCE;
        JsonObject jsonObject = new JsonObject();
        JsonPrimitive booleanNode = new JsonPrimitive(true);
        JsonPrimitive doubleNode = new JsonPrimitive(42D);
        JsonPrimitive floatNode = new JsonPrimitive(42F);
        JsonPrimitive intNode = new JsonPrimitive(42);
        JsonPrimitive longNode = new JsonPrimitive(42L);
        JsonPrimitive shortNode = new JsonPrimitive((short) 42);
        JsonPrimitive textNode = new JsonPrimitive("42");

        return Stream.of(
            Arguments.of(jsonArray, new GsonJsonArray(jsonArray)),
            Arguments.of(jsonNull, GsonJsonNull.INSTANCE),
            Arguments.of(jsonObject, new GsonJsonObject(jsonObject)),
            Arguments.of(booleanNode, new GsonJsonPrimitive(booleanNode)),
            Arguments.of(doubleNode, new GsonJsonPrimitive(doubleNode)),
            Arguments.of(floatNode, new GsonJsonPrimitive(floatNode)),
            Arguments.of(intNode, new GsonJsonPrimitive(intNode)),
            Arguments.of(longNode, new GsonJsonPrimitive(longNode)),
            Arguments.of(shortNode, new GsonJsonPrimitive(shortNode)),
            Arguments.of(textNode, new GsonJsonPrimitive(textNode))
        );
    }

    @Test
    public void invalidFromGsonElement() {
        JsonElement unknownNode = mock(JsonElement.class);
        when(unknownNode.isJsonArray()).thenReturn(false);
        when(unknownNode.isJsonNull()).thenReturn(false);
        when(unknownNode.isJsonObject()).thenReturn(false);
        when(unknownNode.isJsonPrimitive()).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> JsonNodeUtils.fromGsonElement(unknownNode));
    }
}
