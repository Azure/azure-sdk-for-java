// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ReasoningEffort}.
 */
class ReasoningEffortTest {

    @Test
    void testReasoningEffortValues() {
        // Assert all known values exist
        assertNotNull(ReasoningEffort.NONE);
        assertNotNull(ReasoningEffort.MINIMAL);
        assertNotNull(ReasoningEffort.LOW);
        assertNotNull(ReasoningEffort.MEDIUM);
        assertNotNull(ReasoningEffort.HIGH);
        assertNotNull(ReasoningEffort.XHIGH);
    }

    @Test
    void testReasoningEffortToString() {
        // Assert correct string values
        assertEquals("none", ReasoningEffort.NONE.toString());
        assertEquals("minimal", ReasoningEffort.MINIMAL.toString());
        assertEquals("low", ReasoningEffort.LOW.toString());
        assertEquals("medium", ReasoningEffort.MEDIUM.toString());
        assertEquals("high", ReasoningEffort.HIGH.toString());
        assertEquals("xhigh", ReasoningEffort.XHIGH.toString());
    }

    @Test
    void testReasoningEffortFromString() {
        // Act & Assert
        assertEquals(ReasoningEffort.NONE, ReasoningEffort.fromString("none"));
        assertEquals(ReasoningEffort.MINIMAL, ReasoningEffort.fromString("minimal"));
        assertEquals(ReasoningEffort.LOW, ReasoningEffort.fromString("low"));
        assertEquals(ReasoningEffort.MEDIUM, ReasoningEffort.fromString("medium"));
        assertEquals(ReasoningEffort.HIGH, ReasoningEffort.fromString("high"));
        assertEquals(ReasoningEffort.XHIGH, ReasoningEffort.fromString("xhigh"));
    }

    @Test
    void testReasoningEffortFromStringCustomValue() {
        // Act - custom/unknown values should still work as expandable string enum
        ReasoningEffort custom = ReasoningEffort.fromString("custom_value");

        // Assert
        assertNotNull(custom);
        assertEquals("custom_value", custom.toString());
    }

    @Test
    void testReasoningEffortValuesCollection() {
        // Act
        Collection<ReasoningEffort> values = ReasoningEffort.values();

        // Assert
        assertNotNull(values);
        // Should contain at least the 6 known values
        // Note: may contain more if custom values were added during test run
        assertTrue(values.size() >= 6);
    }
}
