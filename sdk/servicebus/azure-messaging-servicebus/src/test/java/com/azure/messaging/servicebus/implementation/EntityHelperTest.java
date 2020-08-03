// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.models.CreateQueueOptions;
import com.azure.messaging.servicebus.models.EntityStatus;
import com.azure.messaging.servicebus.models.QueueProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link EntityHelper} tests.
 */
class EntityHelperTest {
    @Test
    void createQueue() {
        // Arrange
        final String queueName = "some-queue";
        final CreateQueueOptions expected = new CreateQueueOptions(queueName)
            .setAutoDeleteOnIdle(Duration.ofSeconds(15))
            .setDefaultMessageTimeToLive(Duration.ofSeconds(50))
            .setDeadLetteringOnMessageExpiration(true)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofSeconds(13))
            .setEnableBatchedOperations(false)
            .setEnablePartitioning(true)
            .setForwardTo("Forward-To-This-Queue")
            .setForwardDeadLetteredMessagesTo("Dead-Lettered-Forward-To")
            .setLockDuration(Duration.ofSeconds(120))
            .setMaxDeliveryCount(15)
            .setMaxSizeInMegabytes(2048)
            .setRequiresDuplicateDetection(true)
            .setRequiresSession(true)
            .setUserMetadata("Test-queue-Metadata")
            .setStatus(EntityStatus.DISABLED);

        // Act
        final QueueProperties actual = EntityHelper.createQueue(expected);

        // Assert
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
        assertEquals(expected.getDefaultMessageTimeToLive(), actual.getDefaultMessageTimeToLive());
        assertEquals(expected.deadLetteringOnMessageExpiration(), actual.deadLetteringOnMessageExpiration());
        assertEquals(expected.getDuplicateDetectionHistoryTimeWindow(), actual.getDuplicateDetectionHistoryTimeWindow());
        assertEquals(expected.enableBatchedOperations(), actual.enableBatchedOperations());
        assertEquals(expected.enablePartitioning(), actual.enablePartitioning());
        assertEquals(expected.getForwardTo(), actual.getForwardTo());
        assertEquals(expected.getForwardDeadLetteredMessagesTo(), actual.getForwardDeadLetteredMessagesTo());
        assertEquals(expected.getLockDuration(), actual.getLockDuration());
        assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
        assertEquals(expected.requiresDuplicateDetection(), actual.requiresDuplicateDetection());
        assertEquals(expected.requiresSession(), actual.requiresSession());
        assertEquals(expected.getUserMetadata(), actual.getUserMetadata());
        assertEquals(expected.getStatus(), actual.getStatus());
    }

    @Test
    void setQueueName() {
        // Arrange
        final String newName = "I'm a new name";
        final CreateQueueOptions options = new CreateQueueOptions("some name");
        final QueueProperties properties = EntityHelper.createQueue(options);

        // Act
        EntityHelper.setQueueName(properties, newName);

        // Assert
        assertEquals(newName, properties.getName());
    }
}
