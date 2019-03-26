package com.azure.common.implementation;

import org.junit.Test;

import static org.junit.Assert.*;

public class EncodedParameterTests {
    @Test
    public void constructor() {
        final EncodedParameter ep = new EncodedParameter("ABC", "123");
        assertEquals("ABC", ep.name());
        assertEquals("123", ep.encodedValue());
    }
}
