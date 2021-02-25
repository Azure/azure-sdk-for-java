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
 * Runtime properties about a subscription.
 *
 * @see ServiceBusAdministrationAsyncClient#getSubscriptionRuntimeProperties(String, String)
 * @see ServiceBusAdministrationClient#getSubscriptionRuntimeProperties(String, String)
 */
@Immutable
public class SubscriptionRuntimeProperties {
    private final String subscriptionName;
    private final String topicName;
    private final long messageCount;
    private final OffsetDateTime accessedAt;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final int activeMessageCount;
    private final int deadLetterMessageCount;
    private final int transferDeadLetterMessageCount;
    private final int transferMessageCount;


    /**
     * Creates a new instance with runtime properties extracted from the given SubscriptionDescription.
     *
     * @param subscriptionProperties Subscription description to extract runtime properties from.
     *
     * @throws NullPointerException if {@code subscriptionDescription} is null.
     */
    public SubscriptionRuntimeProperties(SubscriptionProperties subscriptionProperties) {
        Objects.requireNonNull(subscriptionProperties, "'subscriptionProperties' cannot be null.");
        this.subscriptionName = subscriptionProperties.getSubscriptionName();
        this.topicName = subscriptionProperties.getTopicName();
        this.messageCount = subscriptionProperties.getMessageCount();
        this.accessedAt = subscriptionProperties.getAccessedAt();
        this.createdAt = subscriptionProperties.getCreatedAt();
        this.updatedAt = subscriptionProperties.getUpdatedAt();

        final MessageCountDetails details = subscriptionProperties.getMessageCountDetails();
        this.activeMessageCount = details != null ? details.getActiveMessageCount() : 0;
        this.deadLetterMessageCount = details != null ? details.getDeadLetterMessageCount() : 0;
        this.transferDeadLetterMessageCount = details != null ? details.getTransferDeadLetterMessageCount() : 0;
        this.transferMessageCount = details != null ? details.getTransferMessageCount() : 0;
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
     * Get the activeMessageCount property: Number of active messages in the queue, topic, or subscription.
     *
     * @return the activeMessageCount value.
     */
    public int getActiveMessageCount() {
        return this.activeMessageCount;
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
     * Get the deadLetterMessageCount property: Number of messages that are dead lettered.
     *
     * @return the deadLetterMessageCount value.
     */
    public int getDeadLetterMessageCount() {
        return this.deadLetterMessageCount;
    }

    /**
     * Gets the number of messages in the subscription.
     *
     * @return The number of messages in the subscription.
     */
    public long getTotalMessageCount() {
        return messageCount;
    }

    /**
     * Get the transferDeadLetterMessageCount property: Number of messages transferred into dead letters.
     *
     * @return the transferDeadLetterMessageCount value.
     */
    public int getTransferDeadLetterMessageCount() {
        return this.transferDeadLetterMessageCount;
    }

    /**
     * Get the transferMessageCount property: Number of messages transferred to another queue, topic, or subscription.
     *
     * @return the transferMessageCount value.
     */
    public int getTransferMessageCount() {
        return this.transferMessageCount;
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
