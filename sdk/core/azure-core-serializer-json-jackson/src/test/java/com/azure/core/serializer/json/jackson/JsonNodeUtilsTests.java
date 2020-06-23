// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.serializer.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ShortNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.AfterEach;
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
    @MethodSource("toJacksonNodeSupplier")
    public void toJacksonNode(JsonNode jsonNode, com.fasterxml.jackson.databind.JsonNode expected) {
        assertEquals(expected, JsonNodeUtils.toJacksonNode(jsonNode));
    }

    private static Stream<Arguments> toJacksonNodeSupplier() {
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
        NullNode nullNode = NullNode.getInstance();
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        BooleanNode booleanNode = BooleanNode.TRUE;
        DoubleNode doubleNode = new DoubleNode(42D);
        FloatNode floatNode = new FloatNode(42F);
        IntNode intNode = new IntNode(42);
        LongNode longNode = new LongNode(42L);
        ShortNode shortNode = new ShortNode((short) 42);
        TextNode textNode = new TextNode("42");
        TextNode utf8TextNode = new TextNode("\uD83D\uDE03");

        return Stream.of(
            Arguments.of(new JacksonJsonArray(arrayNode), arrayNode),
            Arguments.of(new JacksonJsonArray(), arrayNode),
            Arguments.of(JacksonJsonNull.INSTANCE, nullNode),
            Arguments.of(new JacksonJsonObject(objectNode), objectNode),
            Arguments.of(new JacksonJsonObject(), objectNode),
            Arguments.of(new JacksonJsonPrimitive(booleanNode), booleanNode),
            Arguments.of(new JacksonJsonPrimitive(true), booleanNode),
            Arguments.of(new JacksonJsonPrimitive(doubleNode), doubleNode),
            Arguments.of(new JacksonJsonPrimitive(42D), doubleNode),
            Arguments.of(new JacksonJsonPrimitive(floatNode), floatNode),
            Arguments.of(new JacksonJsonPrimitive(42F), floatNode),
            Arguments.of(new JacksonJsonPrimitive(intNode), intNode),
            Arguments.of(new JacksonJsonPrimitive(42), intNode),
            Arguments.of(new JacksonJsonPrimitive(longNode), longNode),
            Arguments.of(new JacksonJsonPrimitive(42L), longNode),
            Arguments.of(new JacksonJsonPrimitive(shortNode), shortNode),
            Arguments.of(new JacksonJsonPrimitive((short) 42), shortNode),
            Arguments.of(new JacksonJsonPrimitive(textNode), textNode),
            Arguments.of(new JacksonJsonPrimitive("42"), textNode),
            Arguments.of(new JacksonJsonPrimitive(utf8TextNode), utf8TextNode),
            Arguments.of(new JacksonJsonPrimitive("\uD83D\uDE03"), utf8TextNode)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidToJacksonNodeSupplier")
    public void invalidToJacksonNode(JsonNode jsonNode) {
        assertThrows(IllegalArgumentException.class, () -> JsonNodeUtils.toJacksonNode(jsonNode));
    }

    private static Stream<Arguments> invalidToJacksonNodeSupplier() {
        // Azure Core JsonNode without a type used in conversion.
        JsonNode unknownNode = mock(JsonNode.class);
        when(unknownNode.isArray()).thenReturn(false);
        when(unknownNode.isNull()).thenReturn(false);
        when(unknownNode.isObject()).thenReturn(false);
        when(unknownNode.isValue()).thenReturn(false);

        // Azure Core JsonNode that is an array but isn't an expected JacksonJsonArray.
        JsonNode unexpectedArray = mock(JsonNode.class);
        when(unexpectedArray.isArray()).thenReturn(true);
        when(unexpectedArray.isNull()).thenReturn(false);
        when(unexpectedArray.isObject()).thenReturn(false);
        when(unexpectedArray.isValue()).thenReturn(false);

        // Azure Core JsonNode that is null but isn't an expected JacksonJsonNull.
        JsonNode unexpectedNull = mock(JsonNode.class);
        when(unexpectedNull.isArray()).thenReturn(false);
        when(unexpectedNull.isNull()).thenReturn(true);
        when(unexpectedNull.isObject()).thenReturn(false);
        when(unexpectedNull.isValue()).thenReturn(false);

        // Azure Core JsonNode that is an object but isn't an expected JacksonJsonObject.
        JsonNode unexpectedObject = mock(JsonNode.class);
        when(unexpectedObject.isArray()).thenReturn(false);
        when(unexpectedObject.isNull()).thenReturn(false);
        when(unexpectedObject.isObject()).thenReturn(true);
        when(unexpectedObject.isValue()).thenReturn(false);

        // Azure Core JsonNode that is a value but isn't an expected JacksonJsonValue.
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
    @MethodSource("fromJacksonNodeSupplier")
    public void fromJacksonNode(com.fasterxml.jackson.databind.JsonNode jsonNode, JsonNode expected) {
        assertEquals(expected, JsonNodeUtils.fromJacksonNode(jsonNode));
    }

    private static Stream<Arguments> fromJacksonNodeSupplier() {
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
        NullNode nullNode = NullNode.getInstance();
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        BooleanNode booleanNode = BooleanNode.TRUE;
        DoubleNode doubleNode = new DoubleNode(42D);
        FloatNode floatNode = new FloatNode(42F);
        IntNode intNode = new IntNode(42);
        LongNode longNode = new LongNode(42L);
        ShortNode shortNode = new ShortNode((short) 42);
        TextNode textNode = new TextNode("42");
        TextNode utf8TextNode = new TextNode("\uD83D\uDE03");

        return Stream.of(
            Arguments.of(arrayNode, new JacksonJsonArray(arrayNode)),
            Arguments.of(nullNode, JacksonJsonNull.INSTANCE),
            Arguments.of(objectNode, new JacksonJsonObject(objectNode)),
            Arguments.of(booleanNode, new JacksonJsonPrimitive(booleanNode)),
            Arguments.of(doubleNode, new JacksonJsonPrimitive(doubleNode)),
            Arguments.of(floatNode, new JacksonJsonPrimitive(floatNode)),
            Arguments.of(intNode, new JacksonJsonPrimitive(intNode)),
            Arguments.of(longNode, new JacksonJsonPrimitive(longNode)),
            Arguments.of(shortNode, new JacksonJsonPrimitive(shortNode)),
            Arguments.of(textNode, new JacksonJsonPrimitive(textNode)),
            Arguments.of(utf8TextNode, new JacksonJsonPrimitive(utf8TextNode))
        );
    }

    @ParameterizedTest
    @MethodSource("invalidFromJacksonNodeSupplier")
    public void invalidFromJacksonNode(com.fasterxml.jackson.databind.JsonNode jsonNode) {
        assertThrows(IllegalArgumentException.class, () -> JsonNodeUtils.fromJacksonNode(jsonNode));
    }

    private static Stream<Arguments> invalidFromJacksonNodeSupplier() {
        // Jackson JsonNode without a type used in conversion.
        com.fasterxml.jackson.databind.JsonNode unknownNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        when(unknownNode.isArray()).thenReturn(false);
        when(unknownNode.getNodeType()).thenReturn(JsonNodeType.MISSING);
        when(unknownNode.isObject()).thenReturn(false);

        // Jackson JsonNode that is an array but isn't an expected ArrayNode.
        com.fasterxml.jackson.databind.JsonNode unexpectedArray = mock(com.fasterxml.jackson.databind.JsonNode.class);
        when(unexpectedArray.isArray()).thenReturn(true);
        when(unexpectedArray.getNodeType()).thenReturn(JsonNodeType.ARRAY);
        when(unexpectedArray.isObject()).thenReturn(false);

        // Jackson JsonNode that is null but isn't an expected NullNode.
        com.fasterxml.jackson.databind.JsonNode unexpectedNull = mock(com.fasterxml.jackson.databind.JsonNode.class);
        when(unexpectedNull.isArray()).thenReturn(false);
        when(unexpectedNull.getNodeType()).thenReturn(JsonNodeType.NULL);
        when(unexpectedNull.isObject()).thenReturn(false);

        // Jackson JsonNode that is an object but isn't an expected ObjectNode.
        com.fasterxml.jackson.databind.JsonNode unexpectedObject = mock(com.fasterxml.jackson.databind.JsonNode.class);
        when(unexpectedObject.isArray()).thenReturn(false);
        when(unexpectedObject.getNodeType()).thenReturn(JsonNodeType.OBJECT);
        when(unexpectedObject.isObject()).thenReturn(true);

        // Jackson JsonNode that is a value but isn't an expected ValueNode.
        com.fasterxml.jackson.databind.JsonNode unexpectedValue = mock(com.fasterxml.jackson.databind.JsonNode.class);
        when(unexpectedValue.isArray()).thenReturn(false);
        when(unexpectedValue.getNodeType()).thenReturn(JsonNodeType.STRING);
        when(unexpectedValue.isObject()).thenReturn(false);

        return Stream.of(
            Arguments.of(unknownNode),
            Arguments.of(unexpectedArray),
            Arguments.of(unexpectedNull),
            Arguments.of(unexpectedObject),
            Arguments.of(unexpectedValue)
        );
    }
}
