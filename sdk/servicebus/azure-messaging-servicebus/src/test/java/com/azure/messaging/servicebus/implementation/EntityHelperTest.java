// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.models.CreateQueueOptions;
import com.azure.messaging.servicebus.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.models.CreateTopicOptions;
import com.azure.messaging.servicebus.models.EntityStatus;
import com.azure.messaging.servicebus.models.QueueProperties;
import com.azure.messaging.servicebus.models.SubscriptionProperties;
import com.azure.messaging.servicebus.models.TopicProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link EntityHelper} tests.
 */
class EntityHelperTest {
    @Test
    void createTopic() {
        // Arrange
        final String queueName = "some-topic";
        final CreateTopicOptions expected = new CreateTopicOptions(queueName)
            .setStatus(EntityStatus.RECEIVE_DISABLED)
            .setUserMetadata("Test-topic-Metadata");

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
    }

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
        final QueueDescription actual = EntityHelper.getQueueDescription(expected);

        // Assert
        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
        assertEquals(expected.getDefaultMessageTimeToLive(), actual.getDefaultMessageTimeToLive());
        assertEquals(expected.deadLetteringOnMessageExpiration(), actual.isDeadLetteringOnMessageExpiration());
        assertEquals(expected.getDuplicateDetectionHistoryTimeWindow(), actual.getDuplicateDetectionHistoryTimeWindow());
        assertEquals(expected.enableBatchedOperations(), actual.isEnableBatchedOperations());
        assertEquals(expected.enablePartitioning(), actual.isEnablePartitioning());
        assertEquals(expected.getForwardTo(), actual.getForwardTo());
        assertEquals(expected.getForwardDeadLetteredMessagesTo(), actual.getForwardDeadLetteredMessagesTo());
        assertEquals(expected.getLockDuration(), actual.getLockDuration());
        assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
        assertEquals(expected.requiresDuplicateDetection(), actual.isRequiresDuplicateDetection());
        assertEquals(expected.requiresSession(), actual.isRequiresSession());
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
    void setQueueName() {
        // Arrange
        final String newName = "I'm a new name";
        final CreateQueueOptions options = new CreateQueueOptions("some name");
        final QueueProperties properties = EntityHelper.toModel(EntityHelper.getQueueDescription(options));

        // Act
        EntityHelper.setQueueName(properties, newName);

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
            .setUserMetadata("Test-topic-Metadata");

        // Act
        final SubscriptionProperties actual = EntityHelper.createSubscription(expected);

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
    void setTopicAndSubscriptionName() {
        // Arrange
        final String topicName = "I'm a new topic name";
        final String subscriptionName = "I'm a new subscription name";
        final CreateSubscriptionOptions options = new CreateSubscriptionOptions("some name", "sub-name");
        final SubscriptionProperties properties = EntityHelper.createSubscription(options);

        // Act
        EntityHelper.setTopicName(properties, topicName);
        EntityHelper.setSubscriptionName(properties, subscriptionName);

        // Assert
        assertEquals(topicName, properties.getTopicName());
        assertEquals(subscriptionName, properties.getSubscriptionName());
    }
}
