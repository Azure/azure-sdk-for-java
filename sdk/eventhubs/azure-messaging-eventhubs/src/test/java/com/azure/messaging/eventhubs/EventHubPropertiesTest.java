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
     * Throws when we try to set null partitionIds.
     */
    @Test
    public void requiresPartitions() {
        // Arrange
        final String name = "Some-event-hub-name";
        final Instant instant = Instant.ofEpochSecond(145620);

        // Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> new EventHubProperties(name, instant, null));
    }

    /**
     * Throws when we try to set null createdAt.
     */
    @Test
    public void requiresCreatedAt() {
        // Arrange
        final String name = "Some-event-hub-name";
        final String[] partitionIds = new String[]{"one-partition", "two-partition", "three-partition"};

        // Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> new EventHubProperties(name, null, partitionIds));
    }

    /**
     */
    @Test
    public void requiresName() {
        // Arrange
        final Instant instant = Instant.ofEpochSecond(145620);
        final String[] partitionIds = new String[]{"one-partition", "two-partition", "three-partition"};

        // Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> new EventHubProperties(null, instant, partitionIds));
    }

}
