// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EventHubConsumerClientOptionsTest {
    /**
     * Verifies we set the correct defaults.
     */
    @Test
    public void defaults() {
        // Act
        final EventHubConsumerOptions options = new EventHubConsumerOptions();

        // Assert
        Assertions.assertEquals(EventHubConsumerOptions.DEFAULT_PREFETCH_COUNT, options.getPrefetchCount());
    }

    @Test
    public void invalidPrefetchMinimum() {
        // Arrange
        final int prefetch = 235;
        final int invalid = EventHubConsumerOptions.MINIMUM_PREFETCH_COUNT - 1;
        final EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setPrefetchCount(prefetch);

        // Act
        try {
            options.setPrefetchCount(invalid);
            Assertions.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        Assertions.assertEquals(prefetch, options.getPrefetchCount());
    }

    @Test
    public void invalidPrefetchMaximum() {
        // Arrange
        final int prefetch = 235;
        final int invalid = EventHubConsumerOptions.MAXIMUM_PREFETCH_COUNT + 1;
        final EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setPrefetchCount(prefetch);

        // Act
        try {
            options.setPrefetchCount(invalid);
            Assertions.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        Assertions.assertEquals(prefetch, options.getPrefetchCount());
    }

    @Test
    public void invalidOwnerLevel() {
        // Arrange
        final long ownerLevel = 14;
        final long invalidOwnerLevel = -1;
        final EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setOwnerLevel(ownerLevel);

        // Act
        try {
            options.setOwnerLevel(invalidOwnerLevel);
            Assertions.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        Assertions.assertEquals(Long.valueOf(ownerLevel), options.getOwnerLevel());
    }
}
