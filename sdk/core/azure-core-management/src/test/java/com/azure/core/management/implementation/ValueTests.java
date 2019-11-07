// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ValueTests {
    @Test
    public void constructorWithNoArguments() {
        final Value<Integer> v = new Value<>();
        assertNull(v.get());
        assertEquals("null", v.toString());
    }

    @Test
    public void constructorWithArgument() {
        final Value<Integer> v = new Value<>(20);
        assertEquals(20, v.get().intValue());
        assertEquals("20", v.toString());
    }
}
