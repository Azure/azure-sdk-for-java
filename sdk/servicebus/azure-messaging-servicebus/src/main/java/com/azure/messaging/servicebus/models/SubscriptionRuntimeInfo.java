// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Runtime information about a subscription.
 */
@Immutable
public class SubscriptionRuntimeInfo {
    private final String subscriptionName;
    private final String topicName;
    private final MessageCountDetails details;
    private final long messageCount;
    private final OffsetDateTime accessedAt;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    /**
     * Creates a new instance with runtime properties extracted from the given SubscriptionDescription.
     *
     * @param subscriptionDescription Subscription description to extract runtime information from.
     *
     * @throws NullPointerException if {@code subscriptionDescription} is null.
     */
    public SubscriptionRuntimeInfo(SubscriptionDescription subscriptionDescription) {
        Objects.requireNonNull(subscriptionDescription, "'subscriptionDescription' cannot be null.");
        this.subscriptionName = subscriptionDescription.getSubscriptionName();
        this.topicName = subscriptionDescription.getTopicName();
        this.details = subscriptionDescription.getMessageCountDetails();
        this.messageCount = subscriptionDescription.getMessageCount();
        this.accessedAt = subscriptionDescription.getAccessedAt();
        this.createdAt = subscriptionDescription.getCreatedAt();
        this.updatedAt = subscriptionDescription.getUpdatedAt();
    }

    /**
     * Gets the last time the subscription was accessed.
     *
     * @return The last time the subscription was accessed.
     */
    public OffsetDateTime getAccessedAt() {
        return accessedAt;
    }

    /**
     * Gets the exact time the subscription was created.
     *
     * @return The exact time the subscription was created.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets details about the message counts in subscription.
     *
     * @return Details about the message counts in subscription.
     */
    public MessageCountDetails getDetails() {
        return details;
    }

    /**
     * Gets the number of messages in the subscription.
     *
     * @return The number of messages in the subscription.
     */
    public long getMessageCount() {
        return messageCount;
    }

    /**
     * Gets the name of the subscription.
     *
     * @return The name of the subscription.
     */
    public String getSubscriptionName() {
        return subscriptionName;
    }

    /**
     * Gets the name of the topic this subscription is associated with.
     *
     * @return The name of the topic this subscription is associated with.
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Gets the exact time the subscription was updated.
     *
     * @return The exact time the subscription was updated.
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
