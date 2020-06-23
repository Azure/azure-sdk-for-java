// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link JacksonJsonPrimitive}.
 */
public class JacksonJsonPrimitiveTests {
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;

    @Test
    public void booleanConstructor() {
        assertEquals(NODE_FACTORY.booleanNode(true), new JacksonJsonPrimitive(true).getValueNode());
    }

    @Test
    public void doubleConstructor() {
        assertEquals(NODE_FACTORY.numberNode(42D), new JacksonJsonPrimitive(42D).getValueNode());
    }

    @Test
    public void floatConstructor() {
        assertEquals(NODE_FACTORY.numberNode(42F), new JacksonJsonPrimitive(42F).getValueNode());
    }

    @Test
    public void intConstructor() {
        assertEquals(NODE_FACTORY.numberNode(42), new JacksonJsonPrimitive(42).getValueNode());
    }

    @Test
    public void longConstructor() {
        assertEquals(NODE_FACTORY.numberNode(42L), new JacksonJsonPrimitive(42L).getValueNode());
    }

    @Test
    public void shortConstructor() {
        assertEquals(NODE_FACTORY.numberNode((short) 42), new JacksonJsonPrimitive((short) 42).getValueNode());
    }

    @Test
    public void stringConstructor() {
        assertEquals(NODE_FACTORY.textNode("42"), new JacksonJsonPrimitive("42").getValueNode());
    }

    @Test
    public void jsonPrimitiveConstructor() {
        TextNode jsonPrimitive = NODE_FACTORY.textNode("\uD83D\uDE03");
        assertEquals(jsonPrimitive, new JacksonJsonPrimitive(jsonPrimitive).getValueNode());
    }

    @Test
    public void nullJsonPrimitiveConstructorThrows() {
        assertThrows(NullPointerException.class, () -> new JacksonJsonPrimitive((ValueNode) null));
    }

    @ParameterizedTest
    @MethodSource("isBooleanSupplier")
    public void isBoolean(JacksonJsonPrimitive jsonPrimitive, boolean expected) {
        assertEquals(expected, jsonPrimitive.isBoolean());
    }

    private static Stream<Arguments> isBooleanSupplier() {
        return isCheckSupplier(true, false, false);
    }

    @ParameterizedTest
    @MethodSource("isNumberSupplier")
    public void isNumber(JacksonJsonPrimitive jsonPrimitive, boolean expected) {
        assertEquals(expected, jsonPrimitive.isNumber());
    }

    private static Stream<Arguments> isNumberSupplier() {
        return isCheckSupplier(false, true, false);
    }

    @ParameterizedTest
    @MethodSource("isStringSupplier")
    public void isString(JacksonJsonPrimitive jsonPrimitive, boolean expected) {
        assertEquals(expected, jsonPrimitive.isString());
    }

    private static Stream<Arguments> isStringSupplier() {
        return isCheckSupplier(false, false, true);
    }

    private static Stream<Arguments> isCheckSupplier(boolean isBoolean, boolean isNumber, boolean isString) {
        return Stream.of(
            Arguments.of(new JacksonJsonPrimitive(true), isBoolean),
            Arguments.of(new JacksonJsonPrimitive(42D), isNumber),
            Arguments.of(new JacksonJsonPrimitive(42F), isNumber),
            Arguments.of(new JacksonJsonPrimitive(42), isNumber),
            Arguments.of(new JacksonJsonPrimitive(42L), isNumber),
            Arguments.of(new JacksonJsonPrimitive((short) 42), isNumber),
            Arguments.of(new JacksonJsonPrimitive("42"), isString),
            Arguments.of(new JacksonJsonPrimitive("\uD83D\uDE03"), isString)
        );
    }

    @ParameterizedTest
    @MethodSource("getAsBooleanSupplier")
    public void getAsBoolean(JacksonJsonPrimitive jsonPrimitive, boolean expected) {
        assertEquals(expected, jsonPrimitive.getAsBoolean());
    }

    private static Stream<Arguments> getAsBooleanSupplier() {
        return Stream.of(
            Arguments.of(new JacksonJsonPrimitive(true), true),
            Arguments.of(new JacksonJsonPrimitive(false), false),
            Arguments.of(new JacksonJsonPrimitive("true"), true),
            Arguments.of(new JacksonJsonPrimitive("false"), false),
            Arguments.of(new JacksonJsonPrimitive(42), false)
        );
    }

    @ParameterizedTest
    @MethodSource("getAsDoubleSupplier")
    public void getAsDouble(JacksonJsonPrimitive jsonPrimitive, Number expected) {
        assertEquals(expected, jsonPrimitive.getAsDouble());
    }

    private static Stream<Arguments> getAsDoubleSupplier() {
        return getAsNumberSupplier(42D, Number::doubleValue);
    }

    @ParameterizedTest
    @MethodSource("getAsFloatSupplier")
    public void getAsFloat(JacksonJsonPrimitive jsonPrimitive, Number expected) {
        assertEquals(expected, jsonPrimitive.getAsFloat());
    }

    private static Stream<Arguments> getAsFloatSupplier() {
        return getAsNumberSupplier(42F, Number::floatValue);
    }

    @ParameterizedTest
    @MethodSource("getAsIntSupplier")
    public void getAsInt(JacksonJsonPrimitive jsonPrimitive, Number expected) {
        assertEquals(expected, jsonPrimitive.getAsInt());
    }

    private static Stream<Arguments> getAsIntSupplier() {
        return getAsNumberSupplier(42, Number::intValue);
    }

    @ParameterizedTest
    @MethodSource("getAsLongSupplier")
    public void getAsLong(JacksonJsonPrimitive jsonPrimitive, Number expected) {
        assertEquals(expected, jsonPrimitive.getAsLong());
    }

    private static Stream<Arguments> getAsLongSupplier() {
        return getAsNumberSupplier(42L, Number::longValue);
    }

    @ParameterizedTest
    @MethodSource("getAsShortSupplier")
    public void getAsShort(JacksonJsonPrimitive jsonPrimitive, Number expected) {
        assertEquals(expected, jsonPrimitive.getAsShort());
    }

    private static Stream<Arguments> getAsShortSupplier() {
        return getAsNumberSupplier((short) 42, Number::shortValue);
    }

    private static Stream<Arguments> getAsNumberSupplier(Number number, Function<Number, Number> expectedFunction) {
        return Stream.of(
            Arguments.of(new JacksonJsonPrimitive(number.doubleValue()), expectedFunction.apply(number)),
            Arguments.of(new JacksonJsonPrimitive(number.floatValue()), expectedFunction.apply(number)),
            Arguments.of(new JacksonJsonPrimitive(number.intValue()), expectedFunction.apply(number)),
            Arguments.of(new JacksonJsonPrimitive(number.longValue()), expectedFunction.apply(number)),
            Arguments.of(new JacksonJsonPrimitive(number.shortValue()), expectedFunction.apply(number)),
            Arguments.of(new JacksonJsonPrimitive(number.toString()), expectedFunction.apply(number))
        );
    }

    @ParameterizedTest
    @MethodSource("getAsStringSupplier")
    public void getAsString(JacksonJsonPrimitive jsonPrimitive, String expected) {
        assertEquals(expected, jsonPrimitive.getAsString());
    }

    private static Stream<Arguments> getAsStringSupplier() {
        return Stream.of(
            Arguments.of(new JacksonJsonPrimitive(false), "false"),
            Arguments.of(new JacksonJsonPrimitive(true), "true"),
            Arguments.of(new JacksonJsonPrimitive(42D), "42.0"),
            Arguments.of(new JacksonJsonPrimitive(42F), "42.0"),
            Arguments.of(new JacksonJsonPrimitive(42), "42"),
            Arguments.of(new JacksonJsonPrimitive(42L), "42"),
            Arguments.of(new JacksonJsonPrimitive((short) 42), "42"),
            Arguments.of(new JacksonJsonPrimitive("42"), "42")
        );
    }
}
