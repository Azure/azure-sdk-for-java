// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link VoiceLiveFunctionDefinition}.
 */
class FunctionToolTest {

    private VoiceLiveFunctionDefinition functionTool;
    private static final String TEST_FUNCTION_NAME = "test_function";

    @BeforeEach
    void setUp() {
        functionTool = new VoiceLiveFunctionDefinition(TEST_FUNCTION_NAME);
    }

    @Test
    void testConstructorWithValidName() {
        // Assert
        assertNotNull(functionTool);
        assertEquals(TEST_FUNCTION_NAME, functionTool.getName());
        assertEquals(ToolType.FUNCTION, functionTool.getType());
    }

    @Test
    void testSetAndGetDescription() {
        // Arrange
        String description = "This is a test function";

        // Act
        functionTool.setDescription(description);

        // Assert
        assertEquals(description, functionTool.getDescription());
    }

    @Test
    void testSetDescriptionWithNull() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            functionTool.setDescription(null);
        });
        assertNull(functionTool.getDescription());
    }

    @Test
    void testSetDescriptionWithEmptyString() {
        // Act
        functionTool.setDescription("");

        // Assert
        assertEquals("", functionTool.getDescription());
    }

    @Test
    void testSetAndGetParameters() {
        // Arrange
        String parametersJson = "{\"type\":\"object\",\"properties\":{\"param1\":{\"type\":\"string\"}}}";
        BinaryData parameters = BinaryData.fromString(parametersJson);

        // Act
        functionTool.setParameters(parameters);

        // Assert
        assertEquals(parameters, functionTool.getParameters());
        assertEquals(parametersJson, functionTool.getParameters().toString());
    }

    @Test
    void testSetParametersWithNull() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            functionTool.setParameters(null);
        });
        assertNull(functionTool.getParameters());
    }

    @Test
    void testTypeIsAlwaysFunction() {
        // The type should always be FUNCTION and not changeable
        assertEquals(ToolType.FUNCTION, functionTool.getType());

        // Create another instance to verify consistency
        VoiceLiveFunctionDefinition anotherTool = new VoiceLiveFunctionDefinition("another_function");
        assertEquals(ToolType.FUNCTION, anotherTool.getType());
    }

    @Test
    void testFluentConfiguration() {
        // Arrange
        String description = "Test description";
        BinaryData parameters = BinaryData.fromString("{\"type\":\"object\"}");

        // Act - Test if methods return the same instance for chaining
        VoiceLiveFunctionDefinition result = functionTool;
        if (hasFluentMethods()) {
            result = functionTool.setDescription(description).setParameters(parameters);

            assertSame(functionTool, result);
        }

        // Verify values were set regardless of fluent interface
        functionTool.setDescription(description);
        functionTool.setParameters(parameters);

        assertEquals(description, functionTool.getDescription());
        assertEquals(parameters, functionTool.getParameters());
    }

    @Test
    void testGetName() {
        // The name should be immutable after construction
        assertEquals(TEST_FUNCTION_NAME, functionTool.getName());

        // Verify there's no setName method
        assertFalse(hasSetNameMethod());
    }

    @Test
    void testToString() {
        // Arrange
        functionTool.setDescription("Test function description");

        // Act
        String toString = functionTool.toString();

        // Assert
        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertTrue(toString.contains("VoiceLiveFunctionDefinition") || toString.contains(TEST_FUNCTION_NAME));
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        VoiceLiveFunctionDefinition tool1 = new VoiceLiveFunctionDefinition("same_function");
        tool1.setDescription("Same description");

        VoiceLiveFunctionDefinition tool2 = new VoiceLiveFunctionDefinition("same_function");
        tool2.setDescription("Same description");

        VoiceLiveFunctionDefinition tool3 = new VoiceLiveFunctionDefinition("different_function");

        // Test equals if implemented
        if (hasEqualsMethod()) {
            // Test reflexive
            assertEquals(tool1, tool1);

            // Test symmetric
            assertEquals(tool1, tool2);
            assertEquals(tool2, tool1);

            // Test hash code consistency
            assertEquals(tool1.hashCode(), tool2.hashCode());

            // Test with different function
            assertNotEquals(tool1, tool3);

            // Test with null
            assertNotEquals(tool1, null);
        }
    }

    @Test
    void testParametersJsonValidation() {
        // Test with invalid JSON (if validation is implemented)
        String invalidJson = "{invalid json}";

        // This might throw an exception if JSON validation is strict
        assertDoesNotThrow(() -> {
            functionTool.setParameters(BinaryData.fromString(invalidJson));
        });

        // Or it might store it as-is for later validation
        functionTool.setParameters(BinaryData.fromString(invalidJson));
        assertEquals(invalidJson, functionTool.getParameters().toString());
    }

    @Test
    void testDefaultValues() {
        // Test default values after construction
        assertEquals(TEST_FUNCTION_NAME, functionTool.getName());
        assertEquals(ToolType.FUNCTION, functionTool.getType());
        assertNull(functionTool.getDescription());
        assertNull(functionTool.getParameters());
    }

    // Helper methods
    private boolean hasFluentMethods() {
        try {
            Object result = functionTool.setDescription("test");
            return result instanceof VoiceLiveFunctionDefinition;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasSetNameMethod() {
        return java.util.Arrays.stream(functionTool.getClass().getMethods())
            .anyMatch(method -> method.getName().equals("setName"));
    }

    private boolean hasEqualsMethod() {
        try {
            return !functionTool.getClass().getMethod("equals", Object.class).getDeclaringClass().equals(Object.class);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
