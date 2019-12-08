// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubstitutionTests {
    @Test
    public void constructor() {
        final Substitution s = new Substitution("A", 2, true);
        assertEquals("A", s.getUrlParameterName());
        assertEquals(2, s.getMethodParameterIndex());
        assertEquals(true, s.shouldEncode());
    }
}
