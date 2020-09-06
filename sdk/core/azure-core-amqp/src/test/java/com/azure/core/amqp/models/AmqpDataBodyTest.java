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
        AmqpDataBody actual = new AmqpDataBody(binaryDataList);

        // Assert
        assertEquals(AmqpBodyType.DATA, actual.getBodyType());

        // Validate Message Body
        List<BinaryData> dataList = actual.getData().stream().collect(Collectors.toList());
        assertEquals(binaryDataList.size(), dataList.size());
        byte[] actualData = dataList.get(0).getData();
        assertArrayEquals(CONTENTS_BYTES, actualData);
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
