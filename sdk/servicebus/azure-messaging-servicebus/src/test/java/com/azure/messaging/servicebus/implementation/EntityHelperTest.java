// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.administration.models.CreateTopicOptions;
import com.azure.messaging.servicebus.administration.models.EntityStatus;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.SubscriptionProperties;
import com.azure.messaging.servicebus.administration.models.TopicProperties;
import com.azure.messaging.servicebus.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.implementation.models.SubscriptionDescription;
import com.azure.messaging.servicebus.implementation.models.TopicDescription;
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
        final CreateTopicOptions expected = new CreateTopicOptions()
            .setStatus(EntityStatus.RECEIVE_DISABLED)
            .setUserMetadata("Test-topic-Metadata");

        // Act
        final TopicDescription actual = EntityHelper.getTopicDescription(expected);

        // Assert
        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
        assertEquals(expected.getDefaultMessageTimeToLive(), actual.getDefaultMessageTimeToLive());
        assertEquals(expected.getDuplicateDetectionHistoryTimeWindow(),
            actual.getDuplicateDetectionHistoryTimeWindow());
        assertEquals(expected.enableBatchedOperations(), actual.isEnableBatchedOperations());
        assertEquals(expected.enablePartitioning(), actual.isEnablePartitioning());
        assertEquals(expected.getMaxSizeInMegabytes(), actual.getMaxSizeInMegabytes());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.requiresDuplicateDetection(), actual.isRequiresDuplicateDetection());
        assertEquals(expected.isSupportOrdering(), actual.isSupportOrdering());
        assertEquals(expected.getUserMetadata(), actual.getUserMetadata());
    }

    @Test
    void createQueue() {
        // Arrange
        final CreateQueueOptions expected = new CreateQueueOptions()
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
        final TopicProperties properties = EntityHelper.toModel(new TopicDescription());

        // Act
        EntityHelper.setTopicName(properties, newName);

        // Assert
        assertEquals(newName, properties.getName());
    }

    @Test
    void setQueueName() {
        // Arrange
        final String newName = "I'm a new name";
        final CreateQueueOptions options = new CreateQueueOptions();
        final QueueProperties properties = EntityHelper.toModel(EntityHelper.getQueueDescription(options));

        // Act
        EntityHelper.setQueueName(properties, newName);

        // Assert
        assertEquals(newName, properties.getName());
    }

    @Test
    void createSubscription() {
        // Arrange
        final CreateSubscriptionOptions expected = new CreateSubscriptionOptions()
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
        final SubscriptionDescription actual = EntityHelper.getSubscriptionDescription(expected);

        // Assert
        assertEquals(expected.getAutoDeleteOnIdle(), actual.getAutoDeleteOnIdle());
        assertEquals(expected.getDefaultMessageTimeToLive(), actual.getDefaultMessageTimeToLive());
        assertEquals(expected.deadLetteringOnMessageExpiration(), actual.isDeadLetteringOnMessageExpiration());
        assertEquals(expected.enableBatchedOperations(), actual.isEnableBatchedOperations());
        assertEquals(expected.enableDeadLetteringOnFilterEvaluationExceptions(),
            actual.isDeadLetteringOnFilterEvaluationExceptions());
        assertEquals(expected.getForwardTo(), actual.getForwardTo());
        assertEquals(expected.getForwardDeadLetteredMessagesTo(), actual.getForwardDeadLetteredMessagesTo());
        assertEquals(expected.getLockDuration(), actual.getLockDuration());
        assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
        assertEquals(expected.requiresSession(), actual.isRequiresSession());
        assertEquals(expected.getUserMetadata(), actual.getUserMetadata());
        assertEquals(expected.getStatus(), actual.getStatus());
    }

    @Test
    void setTopicAndSubscriptionName() {
        // Arrange
        final String topicName = "I'm a new topic name";
        final String subscriptionName = "I'm a new subscription name";
        final SubscriptionProperties properties = EntityHelper.toModel(new SubscriptionDescription());

        // Act
        EntityHelper.setTopicName(properties, topicName);
        EntityHelper.setSubscriptionName(properties, subscriptionName);

        // Assert
        assertEquals(topicName, properties.getTopicName());
        assertEquals(subscriptionName, properties.getSubscriptionName());
    }
}
