// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static com.azure.messaging.servicebus.ServiceBusSenderAsyncClient.MAX_MESSAGE_LENGTH_BYTES;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class ServiceBusMessageBatchTest {
    @Mock
    private ErrorContextProvider errorContextProvider;

    private MessageSerializer serializer = new ServiceBusMessageSerializer();
    private TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void nullMessage() {
        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(1024, errorContextProvider, tracerProvider, serializer, null, null);
        assertThrows(IllegalArgumentException.class, () -> batch.tryAdd(null));
    }

    /**
     * Verify that if we try to add a payload that is too big for the ServiceBusMessageBatch, it throws.
     */
    @Test
    public void payloadExceededException() {
        // Arrange
        when(errorContextProvider.getErrorContext()).thenReturn(new AmqpErrorContext("test-namespace"));

        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(1024, errorContextProvider, tracerProvider, serializer, null, null);
        final ServiceBusMessage tooBig = new ServiceBusMessage(new byte[1024 * 1024 * 2]);

        // Act
        AmqpException amqpException = assertThrows(AmqpException.class, () -> batch.tryAdd(tooBig));

        // Assert
        Assertions.assertFalse(amqpException.isTransient());
        Assertions.assertEquals(AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, amqpException.getErrorCondition());
    }

    /**
     * Verify that we can add a message that is within the batch's size limits.
     */
    @Test
    public void withinPayloadSize() {
        final int maxSize = MAX_MESSAGE_LENGTH_BYTES;
        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(maxSize, errorContextProvider, tracerProvider,
            serializer, null, null);
        final ServiceBusMessage within = new ServiceBusMessage(new byte[1024]);

        Assertions.assertEquals(maxSize, batch.getMaxSizeInBytes());
        Assertions.assertTrue(maxSize > batch.getSizeInBytes());
        Assertions.assertTrue(batch.tryAdd(within));
        Assertions.assertEquals(1, batch.getCount());
    }

    /**
     * Verify that a batch is empty.
     */
    @Test
    public void setsPartitionId() {
        // Act
        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(MAX_MESSAGE_LENGTH_BYTES, errorContextProvider,
            tracerProvider, serializer, null, null);

        // Assert
        Assertions.assertTrue(batch.getMessages().isEmpty());
    }
}
