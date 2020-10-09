// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for {@link AmqpDataBody}.
 */
public class AmqpDataBodyTest {

    /**
     * Verifies we correctly set values via constructor for {@link AmqpAnnotatedMessage}.
     */
    @Test
    public void constructorValidValues() {
        // Arrange
        final List<byte[]> expectedDataList = new ArrayList<>();
        expectedDataList.add("some data 1".getBytes());
        expectedDataList.add("some data 2".getBytes());

        // Act
        final AmqpDataBody actual = new AmqpDataBody(expectedDataList);

        // Assert
        assertEquals(AmqpBodyType.DATA, actual.getBodyType());

        // Validate Message Body
        final List<byte[]> dataList = actual.getData().stream().collect(Collectors.toList());
        assertEquals(expectedDataList.size(), dataList.size());
        assertArrayEquals(expectedDataList.toArray(), dataList.toArray());
    }

    /**
     * Verifies {@link AmqpDataBody} constructor for null values.
     */
    @Test
    public void constructorNullValidValues() {
        // Arrange
        final List<byte[]> listBinaryData = null;

        // Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpDataBody(listBinaryData));
    }
}
