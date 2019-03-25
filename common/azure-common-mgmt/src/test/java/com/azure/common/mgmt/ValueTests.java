package com.azure.common.mgmt;

import org.junit.Test;

import static org.junit.Assert.*;

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
