// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class EventDataBatchTest {
    private static final String PARTITION_KEY = "PartitionIDCopyFromProducerOption";
    private static final EventHubsProducerInstrumentation DEFAULT_INSTRUMENTATION = new EventHubsProducerInstrumentation(null, null, "fqdn", "entity");
    @Mock
    private ErrorContextProvider errorContextProvider;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void nullEventData() {
        assertThrows(NullPointerException.class, () -> {
            final EventDataBatch batch = new EventDataBatch(1024, null, PARTITION_KEY, null, DEFAULT_INSTRUMENTATION);
            batch.tryAdd(null);
        });
    }

    /**
     * Verify that if we try to add a payload that is too big for the EventDataBatch, it throws.
     */
    @Test
    public void payloadExceededException() {
        when(errorContextProvider.getErrorContext()).thenReturn(new AmqpErrorContext("test-namespace"));

        final EventDataBatch batch = new EventDataBatch(1024, null, PARTITION_KEY, errorContextProvider,
            DEFAULT_INSTRUMENTATION);
        final EventData tooBig = new EventData(new byte[1024 * 1024 * 2]);
        try {
            batch.tryAdd(tooBig);
            Assertions.fail("Expected an exception");
        } catch (AmqpException e) {
            Assertions.assertFalse(e.isTransient());
            Assertions.assertEquals(AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, e.getErrorCondition());
        }
    }

    /**
     * Verify that we can add a message that is within the batch's size limits.
     */
    @Test
    public void withinPayloadSize() {
        final int maxSize = ClientConstants.MAX_MESSAGE_LENGTH_BYTES;
        final EventDataBatch batch = new EventDataBatch(ClientConstants.MAX_MESSAGE_LENGTH_BYTES, null, PARTITION_KEY,
            null, DEFAULT_INSTRUMENTATION);
        final EventData within = new EventData(new byte[1024]);

        Assertions.assertEquals(maxSize, batch.getMaxSizeInBytes());
        Assertions.assertTrue(maxSize > batch.getSizeInBytes());
        Assertions.assertTrue(batch.tryAdd(within));
        Assertions.assertEquals(1, batch.getCount());
    }

    /**
     * Verify that we can create a batch with partition id and key.
     */
    @Test
    public void setsPartitionId() {
        final String partitionId = "My-partitionId";

        // Act
        final EventDataBatch batch = new EventDataBatch(ClientConstants.MAX_MESSAGE_LENGTH_BYTES, partitionId,
            PARTITION_KEY, null, DEFAULT_INSTRUMENTATION);

        // Assert
        Assertions.assertEquals(PARTITION_KEY, batch.getPartitionKey());
        Assertions.assertEquals(partitionId, batch.getPartitionId());
        Assertions.assertEquals(0, batch.getEvents().size());
    }
}
