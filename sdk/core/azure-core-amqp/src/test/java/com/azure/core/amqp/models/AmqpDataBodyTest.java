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
public class AmqpDataBodyTest {

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
        final AmqpDataBody actual = new AmqpDataBody(expectedDataList);

        // Assert
        assertEquals(AmqpBodyType.DATA, actual.getBodyType());

        // Validate Message Body
        final List<BinaryData> dataList = actual.getDataAsBinaryData().stream().collect(Collectors.toList());
        assertEquals(1, dataList.size());
        assertArrayEquals(expectedData, dataList.get(0).toBytes());
    }

    /**
     * Verifies we correctly set byte array values via constructor for {@link AmqpAnnotatedMessage}.
     */
    @Test
    public void constructorValidByteArrayValues() {
        // Arrange
        final byte[] expectedData = "some data".getBytes();

        List<byte[]> byteArrayList = new ArrayList<>();
        byteArrayList.add(expectedData);
        final Iterable<byte[]> expectedByteArrayIterable = byteArrayList;

        // Act
        final AmqpDataBody actual = new AmqpDataBody(expectedByteArrayIterable);

        // Assert
        assertEquals(AmqpBodyType.DATA, actual.getBodyType());

        // Validate Message Body
        final List<byte[]> dataList = actual.getData().stream().collect(Collectors.toList());
        assertEquals(1, dataList.size());
        assertArrayEquals(expectedData, dataList.get(0));

        final List<BinaryData> dataListBinaryData = actual.getDataAsBinaryData().stream().collect(Collectors.toList());
        assertEquals(dataListBinaryData.size(), dataList.size());
        assertArrayEquals(dataListBinaryData.get(0).toBytes(), dataList.get(0));
    }

    /**
     * Verifies {@link AmqpDataBody} constructor for various invalid/cases.
     */
    @Test
    public void constructorInvalidValues() {
        // Arrange
        final List<BinaryData> listBinaryData = null;
        final Iterable<byte[]> byteArrayData = null;

        final byte[] expectedData = "some data".getBytes();
        final List<BinaryData> unexpectedLargeDataList = new ArrayList<>();
        unexpectedLargeDataList.add(BinaryData.fromBytes(expectedData));
        unexpectedLargeDataList.add(BinaryData.fromBytes(expectedData));

        final List<BinaryData> unexpectedEmptyDataList = new ArrayList<>();
        final Iterable<byte[]> unexpectedEmptyByteArrayIterable = new ArrayList<>();

        // Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpDataBody(listBinaryData));
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpDataBody(byteArrayData));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AmqpDataBody(unexpectedLargeDataList));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AmqpDataBody(unexpectedEmptyDataList));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AmqpDataBody(unexpectedEmptyByteArrayIterable));
    }
}
