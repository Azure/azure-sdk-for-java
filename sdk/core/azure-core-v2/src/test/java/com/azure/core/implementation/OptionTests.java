// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OptionTests {
    @Test
    public void testOfNonNull() {
        final Option<Integer> setOption = Option.of(1);
        assertTrue(setOption.isInitialized());
        assertEquals(1, setOption.getValue());
    }

    @Test
    public void testOfNull() {
        final Option<?> setOption = Option.of(null);
        assertTrue(setOption.isInitialized());
        assertNull(setOption.getValue());
    }

    @Test
    public void testEmpty() {
        final Option<Void> emptyOption = Option.empty();
        assertTrue(emptyOption.isInitialized());
        assertNull(emptyOption.getValue());
    }

    @Test
    public void testUninitialized() {
        final Option<Void> uninitializedOption = Option.uninitialized();
        assertFalse(uninitializedOption.isInitialized());
    }

    @Test
    public void testGetValueThrows() {
        final Option<Void> uninitializedOption = Option.uninitialized();
        assertThrows(NoSuchElementException.class, uninitializedOption::getValue);
    }

    @Test
    public void testHashCode() {
        final Option<Void> uninitializedOption = Option.uninitialized();
        assertEquals(-1, uninitializedOption.hashCode());

        final Option<Void> nullValueOption = Option.of(null);
        assertEquals(0, nullValueOption.hashCode());

        final Integer val = 44;
        final Option<Integer> nonNullValueOption = Option.of(val);
        assertEquals(val.hashCode(), nonNullValueOption.hashCode());
    }

    @Test
    public void testEqual() {
        assertEquals(Option.uninitialized(), Option.uninitialized());
        assertEquals(Option.of(44), Option.of(44));
        assertEquals(Option.of(null), Option.of(null));

        assertNotEquals(Option.uninitialized(), Option.of(null));
        assertNotEquals(Option.uninitialized(), Option.of(44));
        assertNotEquals(Option.of(null), Option.of(44));
        assertNotEquals(Option.of(88), Option.of(44));
    }
}
