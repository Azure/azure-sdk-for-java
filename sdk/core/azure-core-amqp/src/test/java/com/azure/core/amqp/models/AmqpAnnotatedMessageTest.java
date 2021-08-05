// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for  {@link AmqpAnnotatedMessage}
 */
public class AmqpAnnotatedMessageTest {

    private static final byte[] CONTENTS_BYTES = "Some-contents".getBytes(StandardCharsets.UTF_8);
    private final ClientLogger logger = new ClientLogger(AmqpAnnotatedMessageTest.class);

    /**
     * Verifies we correctly set values via constructor for {@link AmqpAnnotatedMessage}.
     */
    @Test
    public void constructorValidValues() {
        // Arrange
        final AmqpMessageBody amqpMessageBody = AmqpMessageBody.fromData(CONTENTS_BYTES);

        // Act
        final AmqpAnnotatedMessage actual = new AmqpAnnotatedMessage(amqpMessageBody);

        // Assert
        assertMessageCreation(AmqpMessageBodyType.DATA, actual);
    }

    /**
     * Verifies {@link AmqpAnnotatedMessage} constructor for null values.
     */
    @Test
    public void constructorNullValidValues() {
        // Arrange
        final AmqpMessageBody body = null;

        // Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpAnnotatedMessage(body));
    }

    private void assertMessageCreation(AmqpMessageBodyType expectedType, AmqpAnnotatedMessage actual) {
        assertEquals(expectedType, actual.getBody().getBodyType());
        assertNotNull(actual.getProperties());
        assertNotNull(actual.getHeader());
        assertNotNull(actual.getFooter());
        assertNotNull(actual.getApplicationProperties());
        assertNotNull(actual.getDeliveryAnnotations());
        assertNotNull(actual.getMessageAnnotations());
        assertNotNull(actual.getApplicationProperties());

        // Validate Message Body
        assertNotNull(actual.getBody());
        assertMessageBody(CONTENTS_BYTES, actual);
    }

    private void assertMessageBody(byte[] expectedbody, AmqpAnnotatedMessage actual) {
        final AmqpMessageBodyType actualType = actual.getBody().getBodyType();
        switch (actualType) {
            case DATA:
                byte[] actualData = actual.getBody().getData().stream().findFirst().get();
                assertArrayEquals(expectedbody, actualData);
                break;
            case VALUE:
            case SEQUENCE:
                throw logger.logExceptionAsError(new UnsupportedOperationException("type not supported yet :" + actualType));
            default:
                throw logger.logExceptionAsError(new IllegalStateException("Invalid type :" + actualType));
        }
    }
}
