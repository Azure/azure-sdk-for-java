// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.tracing.sleuth.implementation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpanIdTests {

    @Test
    void validateSpanId() {
        String validSpanId = "62974557992089f2";
        String invalidSpanId = "0000000000000000";
        String isNotBase16String = "this---not-valid";
        assertFalse(SpanId.isValid(null));
        assertFalse(SpanId.isValid(validSpanId + "extra"));
        assertFalse(SpanId.isValid(invalidSpanId));
        assertFalse(SpanId.isValid(isNotBase16String));
        assertTrue(SpanId.isValid(validSpanId));
    }
}
