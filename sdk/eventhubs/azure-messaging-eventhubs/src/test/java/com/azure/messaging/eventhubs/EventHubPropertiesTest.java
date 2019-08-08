// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EventHubPropertiesTest {
    /**
     * Verifies that the properties on {@link EventHubProperties} are set properly.
     */
    @Test
    public void setsProperties() {
        // Arrange
        final String name = "Some-event-hub-name";
        final Instant instant = Instant.ofEpochSecond(145620);
        final String[] partitionIds = new String[]{"one-partition", "two-partition", "three-partition"};

        // Act
        final EventHubProperties eventHubProperties = new EventHubProperties(name, instant, partitionIds);

        // Assert
        Assert.assertEquals(name, eventHubProperties.name());
        Assert.assertEquals(instant, eventHubProperties.createdAt());
        Assert.assertEquals(partitionIds.length, eventHubProperties.partitionIds().length);

        final Set<String> actual = new HashSet<>(Arrays.asList(eventHubProperties.partitionIds()));
        for (String id : partitionIds) {
            Assert.assertTrue(actual.contains(id));
        }
    }

    /**
     * Verifies that the {@link EventHubProperties#partitionIds()} array is not {@code null} when we pass {@code null}
     * to the constructor.
     */
    @Test
    public void setsPropertiesNoPartitions() {
        // Arrange
        final String name = "Some-event-hub-name";
        final Instant instant = Instant.ofEpochSecond(145620);

        // Act
        final EventHubProperties eventHubProperties = new EventHubProperties(name, instant, null);

        // Assert
        Assert.assertEquals(name, eventHubProperties.name());
        Assert.assertEquals(instant, eventHubProperties.createdAt());
        Assert.assertNotNull(eventHubProperties.partitionIds());
        Assert.assertEquals(0, eventHubProperties.partitionIds().length);
    }
}
