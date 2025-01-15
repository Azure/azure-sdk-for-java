// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SubstitutionTests {
    @Test
    public void substitutionConstructor() {
        final Substitution s = new Substitution("A", 2, true);

        assertEquals("A", s.getUriParameterName());
        assertEquals(2, s.getMethodParameterIndex());
        assertTrue(s.shouldEncode());
    }

    @Test
    public void querySubstitutionConstructor() {
        final QuerySubstitution s = new QuerySubstitution("A", 2, true, true);

        assertEquals("A", s.getUriParameterName());
        assertEquals(2, s.getMethodParameterIndex());
        assertTrue(s.shouldEncode());
        assertTrue(s.mergeParameters());
    }
}
