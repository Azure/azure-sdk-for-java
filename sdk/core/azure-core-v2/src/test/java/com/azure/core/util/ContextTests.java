// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link Context}
 */
public class ContextTests {
    @Test
    public void simpleContext() {
        Context context = new Context("key", "value");

        assertEquals("value", context.getData("key").orElse(""));
        assertFalse(context.getData("fakeKey").isPresent());
    }

    @Test
    public void constructorKeyCannotBeNull() {
        assertThrows(NullPointerException.class, () -> new Context(null, null));
    }

    @ParameterizedTest
    @MethodSource("addDataSupplier")
    public void addData(String key, String value, String expectedOriginalValue) {
        Context context = new Context("key", "value").addData(key, value);

        assertEquals(value, context.getData(key).orElse(""));
        assertEquals(expectedOriginalValue, context.getData("key").orElse(""));
    }

    private static Stream<Arguments> addDataSupplier() {
        return Stream.of(
            // Adding with same key overwrites value.
            Arguments.of("key", "newValue", "newValue"), Arguments.of("key", "", ""),

            // New values.
            Arguments.of("key2", "newValue", "value"), Arguments.of("key2", "", "value"));
    }

    @Test
    public void addDataKeyCannotBeNull() {
        Context context = new Context("key", "value");

        assertThrows(IllegalArgumentException.class, () -> context.addData(null, null));
    }

    @Test
    public void addDataValueCanBeNull() {
        Context context = new Context("key", null);

        assertFalse(context.getData("key").isPresent());
    }

    @Test
    public void of() {
        Context context = Context.of(Collections.singletonMap("key", "value"));

        assertEquals("value", context.getData("key").orElse(""));

        Map<Object, Object> complexValues = new HashMap<>();
        complexValues.put("key", "value");
        complexValues.put("key2", "value2");
        context = Context.of(complexValues);

        assertEquals("value", context.getData("key").orElse(""));
        assertEquals("value2", context.getData("key2").orElse(""));
    }

    @Test
    public void ofValuesCannotBeNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> Context.of(null));
        assertThrows(IllegalArgumentException.class, () -> Context.of(Collections.emptyMap()));
    }

    @Test
    public void getValueKeyCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> Context.none().getData(null));
    }

    @ParameterizedTest
    @MethodSource("getValuesSupplier")
    public void getValues(Context context, Map<Object, Object> expected) {
        assertEquals(expected, context.getValues());
    }

    @Test
    public void getContextChain() {
        Context context = Context.none().addData("key", "value");
        Context[] chain = context.getContextChain();
        assertEquals(1, chain.length);
        assertEquals("value", chain[0].getValue());

        context = FluxUtil.withContext(Mono::just).block();
        context = context.addData("key1", "value1");
        chain = context.getContextChain();
        assertEquals(1, chain.length);
        assertEquals("value1", chain[0].getValue());
    }

    private static Stream<Arguments> getValuesSupplier() {
        Context contextWithMultipleKeys = new Context("key", "value").addData("key2", "value2");
        Map<Object, Object> expectedMultipleKeys = new HashMap<>();
        expectedMultipleKeys.put("key", "value");
        expectedMultipleKeys.put("key2", "value2");

        Context contextWithMultipleSameKeys = new Context("key", "value").addData("key", "value2");

        return Stream.of(Arguments.of(Context.none(), Collections.emptyMap()),
            Arguments.of(new Context("key", "value"), Collections.singletonMap("key", "value")),
            Arguments.of(contextWithMultipleKeys, expectedMultipleKeys),
            Arguments.of(contextWithMultipleSameKeys, Collections.singletonMap("key", "value2")));
    }
}
