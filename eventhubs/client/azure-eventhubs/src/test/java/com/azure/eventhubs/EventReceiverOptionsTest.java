// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class EventReceiverOptionsTest {
    /**
     * Verifies we set the correct defaults.
     */
    @Test
    public void defaults() {
        // Act
        final EventReceiverOptions options = new EventReceiverOptions();

        // Assert
        Assert.assertEquals(EventReceiverOptions.DEFAULT_CONSUMER_GROUP_NAME, options.consumerGroup());
        Assert.assertEquals(EventReceiverOptions.DEFAULT_PREFETCH_COUNT, options.prefetchCount());
        Assert.assertEquals(EventPosition.firstAvailableEvent(), options.beginReceivingAt());
        Assert.assertFalse(options.keepPartitionInformationUpdated());
    }

    @Test
    public void invalidIdentifier() {
        // Arrange
        final int length = EventReceiverOptions.MAXIMUM_IDENTIFIER_LENGTH + 1;
        final String longIdentifier = new String(new char[length]).replace("\0", "f");
        final String identifier = "An Identifier";
        final EventReceiverOptions options = new EventReceiverOptions()
            .identifier(identifier);

        // Act
        try {
            options.identifier(longIdentifier);
            Assert.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        Assert.assertEquals(identifier, options.identifier());
    }

    @Test
    public void invalidPrefetchMinimum() {
        // Arrange
        final int prefetch = 235;
        final int invalid = EventReceiverOptions.MINIMUM_PREFETCH_COUNT - 1;
        final EventReceiverOptions options = new EventReceiverOptions()
            .prefetchCount(prefetch);

        // Act
        try {
            options.prefetchCount(invalid);
            Assert.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        Assert.assertEquals(prefetch, options.prefetchCount());
    }

    @Test
    public void invalidPrefetchMaximum() {
        // Arrange
        final int prefetch = 235;
        final int invalid = EventReceiverOptions.MAXIMUM_PREFETCH_COUNT + 1;
        final EventReceiverOptions options = new EventReceiverOptions()
            .prefetchCount(prefetch);

        // Act
        try {
            options.prefetchCount(invalid);
            Assert.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        Assert.assertEquals(prefetch, options.prefetchCount());
    }

    @Test
    public void invalidReceiverPriority() {
        // Arrange
        final long priority = 14;
        final long invalidPriority = -1;
        final EventReceiverOptions options = new EventReceiverOptions()
            .exclusiveReceiverPriority(priority);

        // Act
        try {
            options.exclusiveReceiverPriority(invalidPriority);
            Assert.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        final Optional<Long> setPriority = options.exclusiveReceiverPriority();
        Assert.assertTrue(setPriority.isPresent());
        Assert.assertEquals(Long.valueOf(priority), setPriority.get());
    }
}
