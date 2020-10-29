// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.experimental.util.BinaryData;
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
public class AmqpDataMessageBodyTest {

    /**
     * Verifies we correctly set {@link BinaryData} values via constructor for {@link AmqpAnnotatedMessage}.
     */
    @Test
    public void constructorValidBinaryDataValues() {
        // Arrange
        final byte[] expectedData = "some data".getBytes();
        final List<BinaryData> expectedDataList = new ArrayList<>();
        expectedDataList.add(BinaryData.fromBytes(expectedData));

        // Act
        final AmqpDataMessageBody actual = new AmqpDataMessageBody(expectedDataList);

        // Assert
        assertEquals(AmqpBodyType.DATA, actual.getBodyType());

        // Validate Message Body
        final List<BinaryData> dataList = actual.getData().stream().collect(Collectors.toList());
        assertEquals(1, dataList.size());
        assertArrayEquals(expectedData, dataList.get(0).toBytes());
    }

    /**
     * Verifies {@link AmqpDataBody} constructor for various invalid/cases.
     */
    @Test
    public void constructorInvalidValues() {
        // Arrange
        final List<BinaryData> listBinaryData = null;

        final byte[] expectedData = "some data".getBytes();
        final List<BinaryData> unexpectedLargeDataList = new ArrayList<>();
        unexpectedLargeDataList.add(BinaryData.fromBytes(expectedData));
        unexpectedLargeDataList.add(BinaryData.fromBytes(expectedData));

        final List<BinaryData> unexpectedEmptyDataList = new ArrayList<>();

        // Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpDataMessageBody(listBinaryData));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AmqpDataMessageBody(unexpectedLargeDataList));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AmqpDataMessageBody(unexpectedEmptyDataList));
    }

    /**
     * Verifies we correctly create {@link BinaryData} using copy constructor.
     */
    @Test
    public void copyConstructorTest() {
        // Arrange
        final byte[] expectedData = "some data".getBytes();
        final List<BinaryData> expectedDataList = new ArrayList<>();
        expectedDataList.add(BinaryData.fromBytes(expectedData));

        final AmqpDataMessageBody expected = new AmqpDataMessageBody(expectedDataList);

        // Act
        final AmqpDataMessageBody actual = new AmqpDataMessageBody(expected);

        // Assert
        assertEquals(expected.getBodyType(), actual.getBodyType());

        // Validate Message Body
        final List<BinaryData> actialDataList = actual.getData().stream().collect(Collectors.toList());
        assertEquals(1, actialDataList.size());
        assertArrayEquals(expectedData, actialDataList.get(0).toBytes());
    }
}
