// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OptionTests {
    @Test
    public void testUnset() {
        final Option<Void> unsetOption = Option.unset();
        assertFalse(unsetOption.isSet());
    }

    @Test
    public void testEmpty() {
        final Option<Void> emptyOption = Option.empty();
        assertTrue(emptyOption.isSet());
        assertNull(emptyOption.getValue());
    }

    @Test
    public void testOf() {
        final Option<Integer> setOption = Option.of(1);
        assertTrue(setOption.isSet());
        assertEquals(1, setOption.getValue());
    }

    @Test
    public void testOfThrows() {
        assertThrows(NullPointerException.class, () -> {
            Option.of(null);
        });
    }

    @Test
    public void testOfNullable() {
        final Option<Integer> nullOption = Option.ofNullable(null);
        assertTrue(nullOption.isSet());
        assertNull(nullOption.getValue());

        final Option<Integer> nonNullOption = Option.ofNullable(1);
        assertTrue(nonNullOption.isSet());
        assertEquals(1, nonNullOption.getValue());
    }

    @Test
    public void testGetValueThrows() {
        final Option<Void> noneOption = Option.unset();
        assertThrows(NoSuchElementException.class, () -> {
            noneOption.getValue();
        });
    }
}
