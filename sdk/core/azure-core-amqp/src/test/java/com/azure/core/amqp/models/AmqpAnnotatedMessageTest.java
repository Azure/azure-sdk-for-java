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
 * Test class for  {@link AmqpAnnotatedMessage}
 */
public class AmqpAnnotatedMessageTest {

    private static final byte[] CONTENTS_BYTES = "Some-contents".getBytes(StandardCharsets.UTF_8);

    /**
     * Verifies we correctly set values via constructor for {@link AmqpAnnotatedMessage}.
     */
    @Test
    public void constructorValidValues() {
        // Arrange & Act
        AmqpAnnotatedMessage actual = new AmqpAnnotatedMessage(new AmqpDataBody(Collections.singletonList(new BinaryData(CONTENTS_BYTES))));

        // Assert
        Assertions.assertEquals(AmqpBodyType.DATA, actual.getBody().getBodyType());
        Assertions.assertNotNull(actual.getProperties());
        Assertions.assertNotNull(actual.getHeader());
        Assertions.assertNotNull(actual.getFooter());
        Assertions.assertNotNull(actual.getApplicationProperties());
        Assertions.assertNotNull(actual.getDeliveryAnnotations());
        Assertions.assertNotNull(actual.getMessageAnnotations());
        Assertions.assertNotNull(actual.getApplicationProperties());

        List<BinaryData> dataList = ((AmqpDataBody)actual.getBody()).getData().stream().collect(Collectors.toList());
        assertEquals(1, dataList.size());
        byte[] actualData = dataList.get(0).getData();
        assertArrayEquals(CONTENTS_BYTES, actualData);
    }

    /**
     * Verifies {@link AmqpAnnotatedMessage} constructor for null valeus.
     */
    @Test
    public void constructorNullValidValues() {
        // Arrange
        AmqpDataBody body = null;

        // Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpAnnotatedMessage(body));
    }
}
