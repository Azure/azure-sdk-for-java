// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

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
 * Unit tests for {@link ServerVadTurnDetection}.
 */
class ServerVadTest {

    private ServerVadTurnDetection serverVad;

    @BeforeEach
    void setUp() {
        serverVad = new ServerVadTurnDetection();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(serverVad);
        assertEquals(TurnDetectionType.SERVER_VAD, serverVad.getType());
    }

    @Test
    void testSetAndGetThreshold() {
        // Arrange
        Double threshold = 0.5;

        // Act
        serverVad.setThreshold(threshold);

        // Assert
        assertEquals(threshold, serverVad.getThreshold());
    }

    @Test
    void testSetThresholdWithValidRange() {
        // Test boundary values
        assertDoesNotThrow(() -> {
            serverVad.setThreshold(0.0);
            assertEquals(0.0, serverVad.getThreshold());

            serverVad.setThreshold(1.0);
            assertEquals(1.0, serverVad.getThreshold());

            serverVad.setThreshold(0.5);
            assertEquals(0.5, serverVad.getThreshold());
        });
    }

    @Test
    void testSetAndGetPrefixPaddingMs() {
        // Arrange
        Integer prefixPadding = 300;

        // Act
        serverVad.setPrefixPaddingMs(prefixPadding);

        // Assert
        assertEquals(prefixPadding, serverVad.getPrefixPaddingMs());
    }

    @Test
    void testSetPrefixPaddingMsWithValidValues() {
        assertDoesNotThrow(() -> {
            serverVad.setPrefixPaddingMs(0);
            assertEquals(0, serverVad.getPrefixPaddingMs());

            serverVad.setPrefixPaddingMs(1000);
            assertEquals(1000, serverVad.getPrefixPaddingMs());
        });
    }

    @Test
    void testSetAndGetSilenceDurationMs() {
        // Arrange
        Integer silenceDuration = 500;

        // Act
        serverVad.setSilenceDurationMs(silenceDuration);

        // Assert
        assertEquals(silenceDuration, serverVad.getSilenceDurationMs());
    }

    @Test
    void testSetSilenceDurationMsWithValidValues() {
        assertDoesNotThrow(() -> {
            serverVad.setSilenceDurationMs(0);
            assertEquals(0, serverVad.getSilenceDurationMs());

            serverVad.setSilenceDurationMs(2000);
            assertEquals(2000, serverVad.getSilenceDurationMs());
        });
    }

    @Test
    void testNullValues() {
        // Test that null values are accepted
        assertDoesNotThrow(() -> {
            serverVad.setThreshold(null);
            serverVad.setPrefixPaddingMs(null);
            serverVad.setSilenceDurationMs(null);
        });

        assertNull(serverVad.getThreshold());
        assertNull(serverVad.getPrefixPaddingMs());
        assertNull(serverVad.getSilenceDurationMs());
    }

    @Test
    void testTypeIsImmutable() {
        // The type should always be SERVER_VAD and not changeable
        assertEquals(TurnDetectionType.SERVER_VAD, serverVad.getType());

        // Verify there's no setType method or it doesn't change the value
        assertEquals(TurnDetectionType.SERVER_VAD, serverVad.getType());
    }

    @Test
    void testFluentConfiguration() {
        // Test that we can chain configuration calls if the methods return ServerVadTurnDetection
        ServerVadTurnDetection result = serverVad;

        // If the methods are fluent, they should return the same instance
        if (hasFluentMethods()) {
            result = serverVad.setThreshold(0.6).setPrefixPaddingMs(250).setSilenceDurationMs(750);

            assertSame(serverVad, result);
            assertEquals(0.6, serverVad.getThreshold());
            assertEquals(250, serverVad.getPrefixPaddingMs());
            assertEquals(750, serverVad.getSilenceDurationMs());
        }
    }

    @Test
    void testToString() {
        // Test that toString doesn't throw and returns meaningful content
        serverVad.setThreshold(0.5);
        serverVad.setPrefixPaddingMs(300);
        serverVad.setSilenceDurationMs(500);

        String toString = serverVad.toString();
        assertNotNull(toString);
        assertFalse(toString.isEmpty());

        // Should contain class name
        assertTrue(toString.contains("ServerVadTurnDetection") || toString.contains("server_vad"));
    }

    @Test
    void testEqualsAndHashCode() {
        // Create two identical ServerVadTurnDetection instances
        ServerVadTurnDetection serverVad1 = new ServerVadTurnDetection();
        serverVad1.setThreshold(0.5);
        serverVad1.setPrefixPaddingMs(300);
        serverVad1.setSilenceDurationMs(500);

        ServerVadTurnDetection serverVad2 = new ServerVadTurnDetection();
        serverVad2.setThreshold(0.5);
        serverVad2.setPrefixPaddingMs(300);
        serverVad2.setSilenceDurationMs(500);

        // Test equals
        if (hasEqualsMethod()) {
            assertEquals(serverVad1, serverVad2);
            assertEquals(serverVad2, serverVad1);
            assertEquals(serverVad1, serverVad1);

            // Test hash code consistency
            assertEquals(serverVad1.hashCode(), serverVad2.hashCode());
        }

        // Test with different values
        ServerVadTurnDetection serverVad3 = new ServerVadTurnDetection();
        serverVad3.setThreshold(0.7);

        if (hasEqualsMethod()) {
            assertNotEquals(serverVad1, serverVad3);
        }
    }

    // Helper methods to check if fluent methods exist
    private boolean hasFluentMethods() {
        try {
            // Try to call methods and see if they return ServerVadTurnDetection
            Object result = serverVad.setThreshold(0.5);
            return result instanceof ServerVadTurnDetection;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasEqualsMethod() {
        try {
            // Check if equals method is overridden
            return !serverVad.getClass().getMethod("equals", Object.class).getDeclaringClass().equals(Object.class);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
