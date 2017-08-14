package com.microsoft.rest.v2;

import org.junit.Test;

import static org.junit.Assert.*;

public class SubstitutionTests {
    @Test
    public void constructor() {
        final Substitution s = new Substitution("A", 2, true);
        assertEquals("A", s.getURLParameterName());
        assertEquals(2, s.getMethodParameterIndex());
        assertEquals(true, s.shouldEncode());
    }
}
