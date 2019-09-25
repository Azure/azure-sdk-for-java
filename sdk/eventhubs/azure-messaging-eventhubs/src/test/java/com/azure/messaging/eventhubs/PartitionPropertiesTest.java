// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;

public class PartitionPropertiesTest {
    /**
     * Verifies that the properties on {@link PartitionProperties} are set properly.
     */
    @Test
    public void setsProperties() {
        // Arrange
        final String eventHub = "The event hub name";
        final String id = "the partition id";
        final long beginningSequence = 1235;
        final long endSequence = 8763;
        final String lastEnqueuedOffset = "Last-Enqueued";
        final Instant lastEnqueuedTime = Instant.ofEpochSecond(1560639208);
        final boolean isEmpty = true;

        // Act
        final PartitionProperties properties = new PartitionProperties(eventHub, id, beginningSequence, endSequence,
            lastEnqueuedOffset, lastEnqueuedTime, isEmpty);

        // Assert
        Assert.assertEquals(eventHub, properties.getEventHubName());
        Assert.assertEquals(id, properties.getId());
        Assert.assertEquals(beginningSequence, properties.getBeginningSequenceNumber());
        Assert.assertEquals(endSequence, properties.getLastEnqueuedSequenceNumber());
        Assert.assertEquals(lastEnqueuedOffset, properties.getLastEnqueuedOffset());
        Assert.assertEquals(lastEnqueuedTime, properties.getLastEnqueuedTime());
        Assert.assertEquals(isEmpty, properties.isEmpty());
    }
}
