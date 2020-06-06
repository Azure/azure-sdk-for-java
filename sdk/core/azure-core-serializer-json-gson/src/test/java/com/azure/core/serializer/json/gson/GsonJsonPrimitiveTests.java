// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link GsonJsonPrimitive}.
 */
public class GsonJsonPrimitiveTests {

    @Test
    public void booleanConstructor() {
        assertEquals(new JsonPrimitive(true), new GsonJsonPrimitive(true).getJsonPrimitive());
    }

    @Test
    public void doubleConstructor() {
        assertEquals(new JsonPrimitive(42D), new GsonJsonPrimitive(42D).getJsonPrimitive());
    }

    @Test
    public void floatConstructor() {
        assertEquals(new JsonPrimitive(42F), new GsonJsonPrimitive(42F).getJsonPrimitive());
    }

    @Test
    public void intConstructor() {
        assertEquals(new JsonPrimitive(42), new GsonJsonPrimitive(42).getJsonPrimitive());
    }

    @Test
    public void longConstructor() {
        assertEquals(new JsonPrimitive(42L), new GsonJsonPrimitive(42L).getJsonPrimitive());
    }

    @Test
    public void shortConstructor() {
        assertEquals(new JsonPrimitive((short) 42),
            new GsonJsonPrimitive((short) 42).getJsonPrimitive());
    }

    @Test
    public void stringConstructor() {
        assertEquals(new JsonPrimitive("42"), new GsonJsonPrimitive("42").getJsonPrimitive());
    }

    @Test
    public void jsonPrimitiveConstructor() {
        JsonPrimitive jsonPrimitive = new JsonPrimitive("\uD83D\uDE03");
        assertEquals(jsonPrimitive, new GsonJsonPrimitive(jsonPrimitive).getJsonPrimitive());
    }

    @Test
    public void nullJsonPrimitiveConstructorThrows() {
        assertThrows(NullPointerException.class, () -> new GsonJsonPrimitive((JsonPrimitive) null));
    }

    @ParameterizedTest
    @MethodSource("isBooleanSupplier")
    public void isBoolean(GsonJsonPrimitive gsonJsonPrimitive, boolean expected) {
        assertEquals(expected, gsonJsonPrimitive.isBoolean());
    }

    private static Stream<Arguments> isBooleanSupplier() {
        return isCheckSupplier(true, false, false);
    }

    @ParameterizedTest
    @MethodSource("isNumberSupplier")
    public void isNumber(GsonJsonPrimitive gsonJsonPrimitive, boolean expected) {
        assertEquals(expected, gsonJsonPrimitive.isNumber());
    }

    private static Stream<Arguments> isNumberSupplier() {
        return isCheckSupplier(false, true, false);
    }

    @ParameterizedTest
    @MethodSource("isStringSupplier")
    public void isString(GsonJsonPrimitive gsonJsonPrimitive, boolean expected) {
        assertEquals(expected, gsonJsonPrimitive.isString());
    }

    private static Stream<Arguments> isStringSupplier() {
        return isCheckSupplier(false, false, true);
    }

    private static Stream<Arguments> isCheckSupplier(boolean isBoolean, boolean isNumber, boolean isString) {
        return Stream.of(
            Arguments.of(new GsonJsonPrimitive(true), isBoolean),
            Arguments.of(new GsonJsonPrimitive(42D), isNumber),
            Arguments.of(new GsonJsonPrimitive(42F), isNumber),
            Arguments.of(new GsonJsonPrimitive(42), isNumber),
            Arguments.of(new GsonJsonPrimitive(42L), isNumber),
            Arguments.of(new GsonJsonPrimitive((short) 42), isNumber),
            Arguments.of(new GsonJsonPrimitive("42"), isString),
            Arguments.of(new GsonJsonPrimitive("\uD83D\uDE03"), isString)
        );
    }

    @ParameterizedTest
    @MethodSource("getAsBooleanSupplier")
    public void getAsBoolean(GsonJsonPrimitive gsonJsonPrimitive, boolean expected) {
        assertEquals(expected, gsonJsonPrimitive.getBoolean());
    }

    private static Stream<Arguments> getAsBooleanSupplier() {
        return Stream.of(
            Arguments.of(new GsonJsonPrimitive(true), true),
            Arguments.of(new GsonJsonPrimitive(false), false),
            Arguments.of(new GsonJsonPrimitive("true"), true),
            Arguments.of(new GsonJsonPrimitive("false"), false),
            Arguments.of(new GsonJsonPrimitive(42), false)
        );
    }

    @ParameterizedTest
    @MethodSource("getAsDoubleSupplier")
    public void getAsDouble(GsonJsonPrimitive gsonJsonPrimitive, Number expected) {
        assertEquals(expected, gsonJsonPrimitive.getDouble());
    }

    private static Stream<Arguments> getAsDoubleSupplier() {
        return getAsNumberSupplier(42D, Number::doubleValue);
    }

    @ParameterizedTest
    @MethodSource("getAsFloatSupplier")
    public void getAsFloat(GsonJsonPrimitive gsonJsonPrimitive, Number expected) {
        assertEquals(expected, gsonJsonPrimitive.getFloat());
    }

    private static Stream<Arguments> getAsFloatSupplier() {
        return getAsNumberSupplier(42F, Number::floatValue);
    }

    @ParameterizedTest
    @MethodSource("getAsIntSupplier")
    public void getAsInt(GsonJsonPrimitive gsonJsonPrimitive, Number expected) {
        assertEquals(expected, gsonJsonPrimitive.getInteger());
    }

    private static Stream<Arguments> getAsIntSupplier() {
        return getAsNumberSupplier(42, Number::intValue);
    }

    @ParameterizedTest
    @MethodSource("getAsLongSupplier")
    public void getAsLong(GsonJsonPrimitive gsonJsonPrimitive, Number expected) {
        assertEquals(expected, gsonJsonPrimitive.getLong());
    }

    private static Stream<Arguments> getAsLongSupplier() {
        return getAsNumberSupplier(42L, Number::longValue);
    }

    @ParameterizedTest
    @MethodSource("getAsShortSupplier")
    public void getAsShort(GsonJsonPrimitive gsonJsonPrimitive, Number expected) {
        assertEquals(expected, gsonJsonPrimitive.getShort());
    }

    private static Stream<Arguments> getAsShortSupplier() {
        return getAsNumberSupplier((short) 42, Number::shortValue);
    }

    private static Stream<Arguments> getAsNumberSupplier(Number number, Function<Number, Number> expectedFunction) {
        return Stream.of(
            Arguments.of(new GsonJsonPrimitive(number.doubleValue()), expectedFunction.apply(number)),
            Arguments.of(new GsonJsonPrimitive(number.floatValue()), expectedFunction.apply(number)),
            Arguments.of(new GsonJsonPrimitive(number.intValue()), expectedFunction.apply(number)),
            Arguments.of(new GsonJsonPrimitive(number.longValue()), expectedFunction.apply(number)),
            Arguments.of(new GsonJsonPrimitive(number.shortValue()), expectedFunction.apply(number)),
            Arguments.of(new GsonJsonPrimitive(number.toString()), expectedFunction.apply(number))
        );
    }

    @ParameterizedTest
    @MethodSource("getAsStringSupplier")
    public void getAsString(GsonJsonPrimitive gsonJsonPrimitive, String expected) {
        assertEquals(expected, gsonJsonPrimitive.getString());
    }

    private static Stream<Arguments> getAsStringSupplier() {
        return Stream.of(
            Arguments.of(new GsonJsonPrimitive(false), "false"),
            Arguments.of(new GsonJsonPrimitive(true), "true"),
            Arguments.of(new GsonJsonPrimitive(42D), "42.0"),
            Arguments.of(new GsonJsonPrimitive(42F), "42.0"),
            Arguments.of(new GsonJsonPrimitive(42), "42"),
            Arguments.of(new GsonJsonPrimitive(42L), "42"),
            Arguments.of(new GsonJsonPrimitive((short) 42), "42"),
            Arguments.of(new GsonJsonPrimitive("42"), "42")
        );
    }
}
