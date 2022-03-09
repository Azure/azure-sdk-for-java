// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import com.azure.core.annotation.Immutable;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.implementation.models.MessageCountDetails;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Runtime properties about the topic.
 *
 * @see ServiceBusAdministrationAsyncClient#getTopicRuntimeProperties(String)
 * @see ServiceBusAdministrationClient#getTopicRuntimeProperties(String)
 */
@Immutable
public final class TopicRuntimeProperties {
    private final String name;
    private final int subscriptionCount;
    private final long sizeInBytes;
    private final OffsetDateTime accessedAt;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final int scheduledMessageCount;

    /**
     * Creates a new instance with runtime properties extracted from the given TopicDescription.
     *
     * @param topicProperties Topic description to extract runtime properties from.
     *
     * @throws NullPointerException if {@code topicDescription} is null.
     */
    public TopicRuntimeProperties(TopicProperties topicProperties) {
        Objects.requireNonNull(topicProperties, "'topicDescription' cannot be null.");

        this.name = topicProperties.getName();
        this.subscriptionCount = topicProperties.getSubscriptionCount();
        this.sizeInBytes = topicProperties.getSizeInBytes();
        this.accessedAt = topicProperties.getAccessedAt();
        this.createdAt = topicProperties.getCreatedAt();
        this.updatedAt = topicProperties.getUpdatedAt();
        final MessageCountDetails details = topicProperties.getMessageCountDetails();
        this.scheduledMessageCount = details != null ? details.getScheduledMessageCount() : 0;
    }

    /**
     * Gets the last time a message was sent, or the last time there was a receive request to this topic.
     *
     * @return The last time a message was sent, or the last time there was a receive request to this topic.
     */
    public OffsetDateTime getAccessedAt() {
        return accessedAt;
    }

    /**
     * Gets the exact time the queue was created.
     *
     * @return The exact time the queue was created.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the name of the topic.
     *
     * @return The name of the topic.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the size of the topic, in bytes.
     *
     * @return The size of the topic, in bytes.
     */
    public long getSizeInBytes() {
        return sizeInBytes;
    }

    /**
     * Get the scheduledMessageCount property: Number of scheduled messages.
     *
     * @return the scheduledMessageCount value.
     */
    public int getScheduledMessageCount() {
        return this.scheduledMessageCount;
    }

    /**
     * Gets the number of subscriptions to the topic.
     *
     * @return The number of subscriptions to the topic.
     */
    public int getSubscriptionCount() {
        return subscriptionCount;
    }

    /**
     * Gets the exact time the topic description was updated.
     *
     * @return The exact time the topic description was updated.
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
