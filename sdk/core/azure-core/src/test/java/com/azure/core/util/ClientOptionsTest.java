// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the ClientOptions API.
 */
public class ClientOptionsTest {

    @Test
    public void testNullHeaders() {
        // Arrange
        final int expectedTotal = 0;
        final ClientOptions clientOptions = new ClientOptions().setHeaders(null);

        // Act
        final Iterable<Header> headers = clientOptions.getHeaders();

        // Assert
        assertNotNull(headers);

        int actualCount = 0;
        for (Header value : headers) {
            actualCount++;
        }

        assertEquals(expectedTotal, actualCount);
    }

    @Test
    public void testLongApplicationId() {
        // Arrange
        String expected = "LongApplicationIdIsAllowedIfItContainsNoSpaces";

        // Act & Assert
        assertEquals(expected, new ClientOptions().setApplicationId(expected).getApplicationId());
    }

    @Test
    public void testInvalidApplicationId() {
        assertThrows(IllegalArgumentException.class, () -> new ClientOptions().setApplicationId("appid with spaces"));
    }

    @Test
    public void testSetApplicationId() {
        // Arrange
        String expected = "AzCopy/10.0.4-Preview";

        // Act & Assert
        assertEquals(expected, new ClientOptions().setApplicationId(expected).getApplicationId());
    }
}
