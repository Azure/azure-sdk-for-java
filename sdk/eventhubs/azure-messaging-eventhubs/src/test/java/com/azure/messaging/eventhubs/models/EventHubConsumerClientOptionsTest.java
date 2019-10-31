// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import org.junit.Assert;
import org.junit.Test;

public class EventHubConsumerClientOptionsTest {
    /**
     * Verifies we set the correct defaults.
     */
    @Test
    public void defaults() {
        // Act
        final EventHubConsumerOptions options = new EventHubConsumerOptions();

        // Assert
        Assert.assertEquals(EventHubConsumerOptions.DEFAULT_PREFETCH_COUNT, options.getPrefetchCount());
    }

    @Test
    public void invalidIdentifier() {
        // Arrange
        final int length = EventHubConsumerOptions.MAXIMUM_IDENTIFIER_LENGTH + 1;
        final String longIdentifier = new String(new char[length]).replace("\0", "f");
        final String identifier = "An Identifier";
        final EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setIdentifier(identifier);

        // Act
        try {
            options.setIdentifier(longIdentifier);
            Assert.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        Assert.assertEquals(identifier, options.getIdentifier());
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
            Assert.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        Assert.assertEquals(prefetch, options.getPrefetchCount());
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
            Assert.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        Assert.assertEquals(prefetch, options.getPrefetchCount());
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
            Assert.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        Assert.assertEquals(Long.valueOf(ownerLevel), options.getOwnerLevel());
    }
}
