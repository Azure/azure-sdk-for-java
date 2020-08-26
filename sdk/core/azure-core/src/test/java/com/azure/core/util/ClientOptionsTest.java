// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the ClientOptions API.
 */
public class ClientOptionsTest {

    @Test
    public void testNullHeaders() {
        // Arrange, Act & Assert
        assertThrows(NullPointerException.class, () -> new ClientOptions().setHeaders(null));
    }

    @ParameterizedTest
    @MethodSource("invalidApplicationId")
    public void testMaxApplicationId(String applicationId) {
        // Arrange, Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new ClientOptions().setApplicationId(applicationId));
    }

    @Test
    public void testSetApplicationId() {
        // Arrange
        String expected = "AzCopy/10.0.4-Preview";

        // Act & Assert
        assertEquals(expected, new ClientOptions().setApplicationId(expected).getApplicationId());
    }

    @Test
    public void testSetApplicationIdEmpty() {
        // Arrange, Act & Assert
        assertNull(new ClientOptions().setApplicationId("").getApplicationId());
    }

    private static Stream<Arguments> invalidApplicationId() {
        return Stream.of(
            Arguments.arguments("AppId-0123456789012345678912345"),
            Arguments.arguments("AppId 78912345")
        );
    }
}
