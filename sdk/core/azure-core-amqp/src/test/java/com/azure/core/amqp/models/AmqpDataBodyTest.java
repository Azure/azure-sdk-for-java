// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test for {@link AmqpDataBody}.
 */
public class AmqpDataBodyTest {
    private static final byte[] CONTENTS_BYTES = "Some-contents".getBytes(StandardCharsets.UTF_8);
    private static final BinaryData DATA_BYTES = new BinaryData(CONTENTS_BYTES);

    /**
     * Verifies we correctly set values via constructor for {@link AmqpAnnotatedMessage}.
     */
    @Test
    public void constructorValidValues() {
        // Arrange
        final List<BinaryData> binaryDataList = Collections.singletonList(DATA_BYTES);

        // Act
        final AmqpDataBody actual = new AmqpDataBody(binaryDataList);

        // Assert
        assertEquals(AmqpBodyType.DATA, actual.getBodyType());

        // Validate Message Body
        final BinaryData actualBinaryData = actual.getBinaryData();
        assertNull(actualBinaryData);

        final List<BinaryData> dataList = actual.getData().stream().collect(Collectors.toList());
        assertEquals(binaryDataList.size(), dataList.size());
        assertArrayEquals(CONTENTS_BYTES, dataList.get(0).getData());
    }

    /**
     * Verifies we correctly set values via constructor for {@link AmqpAnnotatedMessage}.
     */
    @Test
    public void constructorValidBinaryDataValue() {

        // Act & Arrange
        final AmqpDataBody actual = new AmqpDataBody(DATA_BYTES);

        // Assert
        assertEquals(AmqpBodyType.DATA, actual.getBodyType());

        // Validate Message Body
        final BinaryData actualBinaryData = actual.getBinaryData();
        assertNotNull(actualBinaryData);

        assertArrayEquals(CONTENTS_BYTES, actualBinaryData.getData());
    }

    /**
     * Verifies {@link BinaryData} constructor for null values.
     */
    @Test
    public void constructorNullValidValues() {
        // Arrange
        final List<BinaryData> listBinaryData = null;

        // Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpDataBody(listBinaryData));
    }
}
