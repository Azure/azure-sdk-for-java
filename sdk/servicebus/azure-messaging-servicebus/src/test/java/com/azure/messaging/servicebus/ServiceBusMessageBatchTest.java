// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.BinaryData;
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
        assertThrows(NullPointerException.class, () -> batch.tryAddMessage(null));
    }

    /**
     * Verify that if we try to add a payload that is too big for the ServiceBusMessageBatch, it throws.
     */
    @Test
    public void payloadExceededException() {
        // Arrange
        when(errorContextProvider.getErrorContext()).thenReturn(new AmqpErrorContext("test-namespace"));

        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(1024, errorContextProvider, tracerProvider, serializer, null, null);
        final ServiceBusMessage tooBig = new ServiceBusMessage(BinaryData.fromBytes(new byte[1024 * 1024 * 2]));

        // Act
        ServiceBusException thrownException = assertThrows(ServiceBusException.class, () -> batch.tryAddMessage(tooBig));

        // Assert
        Assertions.assertFalse(thrownException.isTransient());
        Assertions.assertEquals(ServiceBusErrorSource.SEND, thrownException.getErrorSource());
        Assertions.assertEquals(ServiceBusFailureReason.MESSAGE_SIZE_EXCEEDED, thrownException.getReason());
    }

    /**
     * Verify that we can add a message that is within the batch's size limits.
     */
    @Test
    public void withinPayloadSize() {
        final int maxSize = MAX_MESSAGE_LENGTH_BYTES;
        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(maxSize, errorContextProvider, tracerProvider,
            serializer, null, null);
        final ServiceBusMessage within = new ServiceBusMessage(BinaryData.fromBytes(new byte[1024]));

        Assertions.assertEquals(maxSize, batch.getMaxSizeInBytes());
        Assertions.assertTrue(maxSize > batch.getSizeInBytes());
        Assertions.assertTrue(batch.tryAddMessage(within));
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
