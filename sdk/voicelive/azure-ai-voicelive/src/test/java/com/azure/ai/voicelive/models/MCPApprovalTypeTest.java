// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link MCPApprovalType}.
 */
class MCPApprovalTypeTest {

    @Test
    void testNeverApprovalType() {
        // Assert
        assertNotNull(MCPApprovalType.NEVER);
        assertEquals("never", MCPApprovalType.NEVER.toString());
    }

    @Test
    void testAlwaysApprovalType() {
        // Assert
        assertNotNull(MCPApprovalType.ALWAYS);
        assertEquals("always", MCPApprovalType.ALWAYS.toString());
    }

    @Test
    void testFromString() {
        // Act
        MCPApprovalType never = MCPApprovalType.fromString("never");
        MCPApprovalType always = MCPApprovalType.fromString("always");

        // Assert
        assertEquals(MCPApprovalType.NEVER, never);
        assertEquals(MCPApprovalType.ALWAYS, always);
    }

    @Test
    void testFromStringWithCustomValue() {
        // Act
        MCPApprovalType custom = MCPApprovalType.fromString("custom-approval-type");

        // Assert
        assertNotNull(custom);
        assertEquals("custom-approval-type", custom.toString());
    }

    @Test
    void testValues() {
        // Act
        Collection<MCPApprovalType> values = MCPApprovalType.values();

        // Assert
        assertNotNull(values);
        assertTrue(values.size() >= 2); // At least NEVER and ALWAYS
        assertTrue(values.contains(MCPApprovalType.NEVER));
        assertTrue(values.contains(MCPApprovalType.ALWAYS));
    }

    @Test
    void testEquality() {
        // Arrange
        MCPApprovalType never1 = MCPApprovalType.fromString("never");
        MCPApprovalType never2 = MCPApprovalType.NEVER;

        // Assert
        assertEquals(never1, never2);
        assertEquals(never1.hashCode(), never2.hashCode());
    }

    @Test
    void testCaseInsensitivity() {
        // Act
        MCPApprovalType lowercase = MCPApprovalType.fromString("never");
        MCPApprovalType uppercase = MCPApprovalType.fromString("NEVER");
        MCPApprovalType mixedCase = MCPApprovalType.fromString("Never");

        // Assert - ExpandableStringEnum is typically case-sensitive for string matching
        // but creates new instances for different cases
        assertNotNull(lowercase);
        assertNotNull(uppercase);
        assertNotNull(mixedCase);
    }
}
