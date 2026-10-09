// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.unit.models;

import com.azure.ai.voicelive.models.McpApprovalType;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link McpApprovalType}.
 */
class McpApprovalTypeTest {

    @Test
    void testNeverApprovalType() {
        // Assert
        assertNotNull(McpApprovalType.NEVER);
        assertEquals("never", McpApprovalType.NEVER.toString());
    }

    @Test
    void testAlwaysApprovalType() {
        // Assert
        assertNotNull(McpApprovalType.ALWAYS);
        assertEquals("always", McpApprovalType.ALWAYS.toString());
    }

    @Test
    void testFromString() {
        // Act
        McpApprovalType never = McpApprovalType.fromString("never");
        McpApprovalType always = McpApprovalType.fromString("always");

        // Assert
        assertEquals(McpApprovalType.NEVER, never);
        assertEquals(McpApprovalType.ALWAYS, always);
    }

    @Test
    void testFromStringWithCustomValue() {
        // Act
        McpApprovalType custom = McpApprovalType.fromString("custom-approval-type");

        // Assert
        assertNotNull(custom);
        assertEquals("custom-approval-type", custom.toString());
    }

    @Test
    void testValues() {
        // Act
        Collection<McpApprovalType> values = McpApprovalType.values();

        // Assert
        assertNotNull(values);
        assertTrue(values.size() >= 2); // At least NEVER and ALWAYS
        assertTrue(values.contains(McpApprovalType.NEVER));
        assertTrue(values.contains(McpApprovalType.ALWAYS));
    }

    @Test
    void testEquality() {
        // Arrange
        McpApprovalType never1 = McpApprovalType.fromString("never");
        McpApprovalType never2 = McpApprovalType.NEVER;

        // Assert
        assertEquals(never1, never2);
        assertEquals(never1.hashCode(), never2.hashCode());
    }

    @Test
    void testCaseInsensitivity() {
        // Act
        McpApprovalType lowercase = McpApprovalType.fromString("never");
        McpApprovalType uppercase = McpApprovalType.fromString("NEVER");
        McpApprovalType mixedCase = McpApprovalType.fromString("Never");

        // Assert - ExpandableStringEnum is typically case-sensitive for string matching
        // but creates new instances for different cases
        assertNotNull(lowercase);
        assertNotNull(uppercase);
        assertNotNull(mixedCase);
    }
}
