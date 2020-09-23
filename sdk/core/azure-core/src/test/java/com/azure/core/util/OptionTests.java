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
    public void testNone() {
        final Option<Void> noneOption = Option.none();
        assertTrue(noneOption.isNone());
    }

    @Test
    public void testSomeNull() {
        final Option<Void> someNullOption = Option.some(null);
        assertFalse(someNullOption.isNone());
        assertNull(someNullOption.getValue());
    }

    @Test
    public void testSomeNonNull() {
        final Option<Integer> someNullOption = Option.some(1);
        assertFalse(someNullOption.isNone());
        assertEquals(1, someNullOption.getValue());
    }

    @Test
    public void testGetValueThrows() {
        final Option<Void> noneOption = Option.none();
        Exception exception = assertThrows(NoSuchElementException.class, () -> {
            noneOption.getValue();
        });
    }
}
