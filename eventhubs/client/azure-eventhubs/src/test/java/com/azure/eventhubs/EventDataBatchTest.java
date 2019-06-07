// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import org.junit.Assert;
import org.junit.Test;

public class EventDataBatchTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullEventData() {
        final EventDataBatch batch = new EventDataBatch(1, "key1");
        batch.tryAdd(null);
    }

    @Test(expected = AmqpException.class)
    public void payloadExceededException() {
        final EventDataBatch batch = new EventDataBatch(1024, "key1");
        final EventData tooBig = new EventData(new byte[1024 * 1024 * 2]);
        batch.tryAdd(tooBig);
    }

    @Test
    public void withinPayloadSize() {
        final EventDataBatch batch = new EventDataBatch(EventSender.MAX_MESSAGE_LENGTH_BYTES, "key1");
        final EventData within = new EventData(new byte[1024]);
        Assert.assertTrue(batch.tryAdd(within));
    }
}
