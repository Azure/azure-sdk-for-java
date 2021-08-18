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
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            Arguments.of("key", "newValue", "newValue"),
            Arguments.of("key", "", ""),

            // New values.
            Arguments.of("key2", "newValue", "value"),
            Arguments.of("key2", "", "value")
        );
    }

    @Test
    public void addDataKeyCannotBeNull() {
        Context context = new Context("key",  "value");

        assertThrows(IllegalArgumentException.class, () -> context.addData(null, null));
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
        assertThrows(IllegalArgumentException.class, () -> Context.NONE.getData(null));
    }

    @ParameterizedTest
    @MethodSource("getValuesSupplier")
    public void getValues(Context context, Map<Object, Object> expected) {
        assertEquals(expected, context.getValues());
    }

    private static Stream<Arguments> getValuesSupplier() {
        Context contextWithMultipleKeys = new Context("key", "value")
            .addData("key2", "value2");
        Map<Object, Object> expectedMultipleKeys = new HashMap<>();
        expectedMultipleKeys.put("key", "value");
        expectedMultipleKeys.put("key2", "value2");

        Context contextWithMultipleSameKeys = new Context("key", "value")
            .addData("key", "value2");

        return Stream.of(
            Arguments.of(Context.NONE, Collections.emptyMap()),
            Arguments.of(new Context("key", "value"), Collections.singletonMap("key", "value")),
            Arguments.of(contextWithMultipleKeys, expectedMultipleKeys),
            Arguments.of(contextWithMultipleSameKeys, Collections.singletonMap("key", "value2"))
        );
    }

    @Test
    public void getValueNullParent() {
        final Context context = new Context();
        final Map<Object, Object> values = context.getValues();
        assertTrue(values.isEmpty());

        final Context context1 = context.addData("key1", "value1");
        final Map<Object, Object> values1 = context1.getValues();

        assertEquals(1, values1.size());
        assertTrue(values1.containsKey("key1"));
        assertEquals("value1", values1.get("key1"));
    }
}
