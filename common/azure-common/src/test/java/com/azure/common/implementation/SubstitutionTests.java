package com.azure.common.implementation;

import org.junit.Test;

import static org.junit.Assert.*;

public class SubstitutionTests {
    @Test
    public void constructor() {
        final Substitution s = new Substitution("A", 2, true);
        assertEquals("A", s.urlParameterName());
        assertEquals(2, s.methodParameterIndex());
        assertEquals(true, s.shouldEncode());
    }
}
