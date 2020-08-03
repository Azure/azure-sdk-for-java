// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.models.CreateTopicOptions;
import com.azure.messaging.servicebus.models.TopicDescription;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityHelperTest {
    @Test
    void createTopic() {
        // Arrange
        final String queueName = "some-queue";
        final CreateTopicOptions expected = new CreateTopicOptions(queueName)
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
            .setUserMetadata("Test-queue-Metadata");

        // Act
        final TopicDescription actual = EntityHelper.createTopic(expected);

        // Assert
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
        assertEquals(expected.getDefaultMessageTimeToLive(), actual.getDefaultMessageTimeToLive());
        assertEquals(expected.getDuplicateDetectionHistoryTimeWindow(), actual.getDuplicateDetectionHistoryTimeWindow());
        assertEquals(expected.enableBatchedOperations(), actual.enableBatchedOperations());
        assertEquals(expected.enablePartitioning(), actual.enablePartitioning());
        assertEquals(expected.requiresDuplicateDetection(), actual.requiresDuplicateDetection());
        assertEquals(expected.getUserMetadata(), actual.getUserMetadata());
    }

    @Test
    void setTopicName() {
        // Arrange
        final String newName = "I'm a new name";
        final CreateTopicOptions options = new CreateTopicOptions("some name");
        final TopicDescription properties = EntityHelper.createTopic(options);

        // Act
        EntityHelper.setTopicName(properties, newName);

        // Assert
        assertEquals(newName, properties.getName());
    }
}
