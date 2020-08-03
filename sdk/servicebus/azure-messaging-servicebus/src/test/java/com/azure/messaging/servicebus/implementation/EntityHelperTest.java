// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.models.CreateTopicOptions;
import com.azure.messaging.servicebus.models.EntityStatus;
import com.azure.messaging.servicebus.models.SubscriptionDescription;
import com.azure.messaging.servicebus.models.TopicProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityHelperTest {
    @Test
    void createTopic() {
        // Arrange
        final String queueName = "some-topic";
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
            .setStatus(EntityStatus.RECEIVE_DISABLED)
            .setUserMetadata("Test-queue-Metadata");

        // Act
        final TopicProperties actual = EntityHelper.createTopic(expected);

        // Assert
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
        assertEquals(expected.getDefaultMessageTimeToLive(), actual.getDefaultMessageTimeToLive());
        assertEquals(expected.getDuplicateDetectionHistoryTimeWindow(), actual.getDuplicateDetectionHistoryTimeWindow());
        assertEquals(expected.enableBatchedOperations(), actual.enableBatchedOperations());
        assertEquals(expected.enablePartitioning(), actual.enablePartitioning());
        assertEquals(expected.requiresDuplicateDetection(), actual.requiresDuplicateDetection());
        assertEquals(expected.getUserMetadata(), actual.getUserMetadata());
        assertEquals(expected.getStatus(), actual.getStatus());
    }

    @Test
    void setTopicName() {
        // Arrange
        final String newName = "I'm a new name";
        final CreateTopicOptions options = new CreateTopicOptions("some name");
        final TopicProperties properties = EntityHelper.createTopic(options);

        // Act
        EntityHelper.setTopicName(properties, newName);

        // Assert
        assertEquals(newName, properties.getName());
    }

    @Test
    void createSubscription() {
        // Arrange
        final String topicName = "topic?";
        final String subscriptionName = "subscription";
        final CreateSubscriptionOptions expected = new CreateSubscriptionOptions(topicName, subscriptionName)
            .setAutoDeleteOnIdle(Duration.ofSeconds(15))
            .setDefaultMessageTimeToLive(Duration.ofSeconds(50))
            .setDeadLetteringOnMessageExpiration(true)
            .setEnableDeadLetteringOnFilterEvaluationExceptions(true)
            .setEnableBatchedOperations(false)
            .setForwardTo("Forward-To-This-Queue")
            .setForwardDeadLetteredMessagesTo("Dead-Lettered-Forward-To")
            .setLockDuration(Duration.ofSeconds(120))
            .setMaxDeliveryCount(15)
            .setRequiresSession(true)
            .setStatus(EntityStatus.RECEIVE_DISABLED)
            .setUserMetadata("Test-queue-Metadata");

        // Act
        final SubscriptionDescription actual = EntityHelper.createSubscription(expected);

        // Assert
        assertEquals(expected.getTopicName(), actual.getTopicName());
        assertEquals(expected.getSubscriptionName(), actual.getSubscriptionName());
        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
        assertEquals(expected.getDefaultMessageTimeToLive(), actual.getDefaultMessageTimeToLive());
        assertEquals(expected.enableDeadLetteringOnFilterEvaluationExceptions(),
            actual.enableDeadLetteringOnFilterEvaluationExceptions());
        assertEquals(expected.enableBatchedOperations(), actual.enableBatchedOperations());
        assertEquals(expected.getUserMetadata(), actual.getUserMetadata());
        assertEquals(expected.getStatus(), actual.getStatus());
    }

    @Test
    void setSubscriptionNames() {
        // Arrange
        final String topicName = "I'm a new topic name";
        final String subscriptionName = "I'm a new subscription name";
        final CreateSubscriptionOptions options = new CreateSubscriptionOptions("some name", "sub-name");
        final SubscriptionDescription properties = EntityHelper.createSubscription(options);

        // Act
        EntityHelper.setTopicName(properties, subscriptionName);
        EntityHelper.setSubscriptionName(properties, subscriptionName);

        // Assert
        assertEquals(topicName, properties.getTopicName());
        assertEquals(subscriptionName, properties.getSubscriptionName());
    }
}
