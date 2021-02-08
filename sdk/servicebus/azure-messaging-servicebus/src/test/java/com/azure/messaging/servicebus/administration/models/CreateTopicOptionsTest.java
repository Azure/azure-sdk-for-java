// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import com.azure.messaging.servicebus.implementation.EntityHelper;
import com.azure.messaging.servicebus.implementation.models.TopicDescription;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DEFAULT_DUPLICATE_DETECTION_DURATION;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DEFAULT_LOCK_DURATION;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DEFAULT_MAX_DELIVERY_COUNT;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DEFAULT_TOPIC_SIZE;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.MAX_DURATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateTopicOptionsTest {

    /**
     * Creates an instance with the given defaults.
     */
    @Test
    void constructor() {
        // Act
        final CreateTopicOptions actual = new CreateTopicOptions();

        // Assert
        assertEquals(MAX_DURATION, actual.getAutoDeleteOnIdle());
        assertEquals(MAX_DURATION, actual.getDefaultMessageTimeToLive());
        assertEquals(DEFAULT_DUPLICATE_DETECTION_DURATION, actual.getDuplicateDetectionHistoryTimeWindow());
        assertTrue(actual.isBatchedOperationsEnabled());
        assertFalse(actual.isPartitioningEnabled());
        assertEquals(DEFAULT_LOCK_DURATION, actual.getLockDuration());
        assertEquals(DEFAULT_MAX_DELIVERY_COUNT, actual.getMaxDeliveryCount());
        assertEquals(DEFAULT_TOPIC_SIZE, actual.getMaxSizeInMegabytes());
        assertFalse(actual.isDuplicateDetectionRequired());
        assertFalse(actual.isSessionRequired());
        assertNull(actual.getUserMetadata());
        assertEquals(EntityStatus.ACTIVE, actual.getStatus());
    }

    @Test
    void constructorWithOptions() {
        // Arrange
        final TopicDescription description = new TopicDescription()
            .setAutoDeleteOnIdle(Duration.ofSeconds(15))
            .setDefaultMessageTimeToLive(Duration.ofSeconds(50))
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofSeconds(13))
            .setEnableBatchedOperations(false)
            .setEnablePartitioning(true)
            .setMaxSizeInMegabytes(2048L)
            .setRequiresDuplicateDetection(true)
            .setUserMetadata("Test-queue-Metadata")
            .setStatus(EntityStatus.DELETING);

        final TopicProperties expected = EntityHelper.toModel(description);

        // Act
        final CreateTopicOptions actual = new CreateTopicOptions(expected);

        // Assert
        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
        assertEquals(expected.getDefaultMessageTimeToLive(), actual.getDefaultMessageTimeToLive());
        assertEquals(expected.getDuplicateDetectionHistoryTimeWindow(),
            actual.getDuplicateDetectionHistoryTimeWindow());
        assertEquals(expected.isBatchedOperationsEnabled(), actual.isBatchedOperationsEnabled());
        assertEquals(expected.isPartitioningEnabled(), actual.isPartitioningEnabled());
        assertEquals(expected.isDuplicateDetectionRequired(), actual.isDuplicateDetectionRequired());
        assertEquals(expected.getUserMetadata(), actual.getUserMetadata());
        assertEquals(expected.getStatus(), actual.getStatus());
    }
}
