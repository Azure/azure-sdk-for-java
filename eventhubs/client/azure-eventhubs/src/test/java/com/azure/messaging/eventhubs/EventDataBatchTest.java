// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.amqp.exception.ErrorContext;
import com.azure.messaging.eventhubs.implementation.ErrorContextProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

public class EventDataBatchTest {
    private static final String PARTITION_KEY = "PartitionIDCopyFromProducerOption";

    @Mock
    private ErrorContextProvider errorContextProvider;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullEventData() {
        final EventDataBatch batch = new EventDataBatch(1024, PARTITION_KEY, null);
        batch.tryAdd(null);
    }

    /**
     * Verify that if we try to add a payload that is too big for the EventDataBatch, it throws.
     */
    @Test
    public void payloadExceededException() {
        when(errorContextProvider.getErrorContext()).thenReturn(new ErrorContext("test-namespace"));

        final EventDataBatch batch = new EventDataBatch(1024, PARTITION_KEY, errorContextProvider);
        final EventData tooBig = new EventData(new byte[1024 * 1024 * 2]);
        try {
            batch.tryAdd(tooBig);
            Assert.fail("Expected an exception");
        } catch (AmqpException e) {
            Assert.assertFalse(e.isTransient());
            Assert.assertEquals(ErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, e.getErrorCondition());
        }
    }

    /**
     * Verify that we can add a message that is within the batch's size limits.
     */
    @Test
    public void withinPayloadSize() {
        final EventDataBatch batch = new EventDataBatch(EventHubProducer.MAX_MESSAGE_LENGTH_BYTES, PARTITION_KEY, null);
        final EventData within = new EventData(new byte[1024]);

        Assert.assertTrue(batch.tryAdd(within));
        Assert.assertEquals(1, batch.getSize());
    }
}
