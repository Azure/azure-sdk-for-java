// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link Context}
 */
public class ContextTests {
    @Test
    public void simpleContext() {
        Context context = Context.of("key", "value");

        assertEquals("value", context.get("key"));
        assertNull(context.get("fakeKey"));
    }

    @ParameterizedTest
    @MethodSource("keysCannotBeNullSupplier")
    public void keysCannotBeNull(Supplier<Context> contextSupplier) {
        assertThrows(NullPointerException.class, contextSupplier::get);
    }

    private static Stream<Supplier<Context>> keysCannotBeNullSupplier() {
        return Stream.of(
            // One key-value pair
            () -> Context.of(null, null),

            // Two key-value pairs
            () -> Context.of(null, null, "key", null), () -> Context.of("key", null, null, null),

            // Three key-value pairs
            () -> Context.of(null, null, "key", null, "key", null),
            () -> Context.of("key", null, null, null, "key", null),
            () -> Context.of("key", null, "key", null, null, null),

            // Four key-value pairs
            () -> Context.of(null, null, "key", null, "key", null, "key", null),
            () -> Context.of("key", null, null, null, "key", null, "key", null),
            () -> Context.of("key", null, "key", null, null, null, "key", null),
            () -> Context.of("key", null, "key", null, "key", null, null, null),

            // Null map
            () -> Context.of(null),

            // Map with null key
            () -> Context.of(Collections.singletonMap(null, null)));
    }

    @ParameterizedTest
    @MethodSource("addDataSupplier")
    public void addData(String key, String value, String expectedOriginalValue) {
        Context context = Context.of("key", "value").put(key, value);

        assertEquals(value, context.get(key));
        assertEquals(expectedOriginalValue, context.get("key"));
    }

    private static Stream<Arguments> addDataSupplier() {
        return Stream.of(
            // Adding with same key overwrites value.
            Arguments.of("key", "newValue", "newValue"), Arguments.of("key", "", ""),

            // New values.
            Arguments.of("key2", "newValue", "value"), Arguments.of("key2", "", "value"));
    }

    @Test
    public void putKeyCannotBeNull() {
        assertThrows(NullPointerException.class, () -> {
            Context context = Context.of("key", "value").put(null, null);
        });
    }

    @Test
    public void putValueCanBeNull() {
        Context context = Context.none().put("key", null);

        assertNull(context.get("key"));
    }

    @Test
    public void of() {
        Context context = Context.of(Collections.singletonMap("key", "value"));

        assertEquals("value", context.get("key"));

        Map<Object, Object> complexValues = new HashMap<>();
        complexValues.put("key", "value");
        complexValues.put("key2", "value2");
        context = Context.of(complexValues);

        assertEquals("value", context.get("key"));
        assertEquals("value2", context.get("key2"));
    }

    @Test
    public void getValueKeyCannotBeNull() {
        assertThrows(NullPointerException.class, () -> Context.none().get(null));
    }
}
