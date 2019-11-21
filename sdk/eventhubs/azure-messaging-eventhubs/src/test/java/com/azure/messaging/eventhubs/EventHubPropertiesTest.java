// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

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
        Assertions.assertEquals(name, eventHubProperties.getName());
        Assertions.assertEquals(instant, eventHubProperties.getCreatedAt());
        Assertions.assertEquals(partitionIds.length, eventHubProperties.getPartitionIds().stream().count());

        final Set<String> actual = eventHubProperties.getPartitionIds().stream().collect(Collectors.toSet());
        for (String id : partitionIds) {
            Assertions.assertTrue(actual.contains(id));
        }
    }

    /**
     * Verifies that the {@link EventHubProperties#getPartitionIds()} array is not {@code null} when we pass {@code null}
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
        Assertions.assertEquals(name, eventHubProperties.getName());
        Assertions.assertEquals(instant, eventHubProperties.getCreatedAt());
        Assertions.assertNotNull(eventHubProperties.getPartitionIds());
        Assertions.assertEquals(0, eventHubProperties.getPartitionIds().stream().count());
    }
}
