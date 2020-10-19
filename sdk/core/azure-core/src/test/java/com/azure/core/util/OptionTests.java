// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertEquals(null, setOption.getValue());
    }

    @Test
    public void testEmpty() {
        final Option<Void> emptyOption = Option.empty();
        assertTrue(emptyOption.isInitialized());
        assertEquals(null, emptyOption.getValue());
    }

    @Test
    public void testUninitialized() {
        final Option<Void> unsetOption = Option.uninitialized();
        assertFalse(unsetOption.isInitialized());
    }

    @Test
    public void testGetValueThrows() {
        final Option<Void> noneOption = Option.uninitialized();
        assertThrows(NoSuchElementException.class, () -> {
            noneOption.getValue();
        });
    }
}
