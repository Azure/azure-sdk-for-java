// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Runtime information about the topic.
 */
@Immutable
public class TopicRuntimeInfo {
    private final String name;
    private final int subscriptionCount;
    private final long sizeInBytes;
    private final OffsetDateTime accessedAt;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    /**
     * Creates a new instance with runtime properties extracted from the given TopicDescription.
     *
     * @param topicProperties Topic description to extract runtime information from.
     *
     * @throws NullPointerException if {@code topicDescription} is null.
     */
    public TopicRuntimeInfo(TopicProperties topicProperties) {
        Objects.requireNonNull(topicProperties, "'topicDescription' cannot be null.");

        this.name = topicProperties.getName();
        this.subscriptionCount = topicProperties.getSubscriptionCount();
        this.sizeInBytes = topicProperties.getSizeInBytes();
        this.accessedAt = topicProperties.getAccessedAt();
        this.createdAt = topicProperties.getCreatedAt();
        this.updatedAt = topicProperties.getUpdatedAt();
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
