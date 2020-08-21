// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import com.azure.core.annotation.Immutable;
import com.azure.messaging.servicebus.implementation.models.MessageCountDetails;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Runtime information about the queue.
 */
@Immutable
public class QueueRuntimeInfo {
    private final String name;
    private final long messageCount;
    private final long sizeInBytes;
    private final OffsetDateTime accessedAt;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final int activeMessageCount;
    private final int deadLetterMessageCount;
    private final int scheduledMessageCount;
    private final int transferDeadLetterMessageCount;
    private final int transferMessageCount;

    /**
     * Creates a new instance with runtime properties extracted from the given QueueDescription.
     *
     * @param queueProperties Queue description to extract runtime information from.
     *
     * @throws NullPointerException if {@code queueDescription} is null.
     */
    public QueueRuntimeInfo(QueueProperties queueProperties) {
        Objects.requireNonNull(queueProperties, "'queueProperties' cannot be null.");
        this.name = queueProperties.getName();
        this.messageCount = queueProperties.getMessageCount();
        this.sizeInBytes = queueProperties.getSizeInBytes();
        this.accessedAt = queueProperties.getAccessedAt();
        this.createdAt = queueProperties.getCreatedAt();
        this.updatedAt = queueProperties.getUpdatedAt();

        final MessageCountDetails details = queueProperties.getMessageCountDetails();
        this.activeMessageCount = details != null ? details.getActiveMessageCount() : 0;
        this.deadLetterMessageCount = details != null ? details.getDeadLetterMessageCount() : 0;
        this.scheduledMessageCount = details != null ? details.getScheduledMessageCount() : 0;
        this.transferDeadLetterMessageCount = details != null ? details.getTransferDeadLetterMessageCount() : 0;
        this.transferMessageCount = details != null ? details.getTransferMessageCount() : 0;
    }

    /**
     * Gets the last time a message was sent, or the last time there was a receive request to this queue.
     *
     * @return The last time a message was sent, or the last time there was a receive request to this queue.
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
     * Gets the exact time the queue was created.
     *
     * @return The exact time the queue was created.
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
     * Gets the number of messages in the queue.
     *
     * @return The number of messages in the queue.
     */
    public long getTotalMessageCount() {
        return messageCount;
    }

    /**
     * Gets the name of the queue.
     *
     * @return The name of the queue.
     */
    public String getName() {
        return name;
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
     * Gets the size of the queue, in bytes.
     *
     * @return The size of the queue, in bytes.
     */
    public long getSizeInBytes() {
        return sizeInBytes;
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
     * Gets the exact time a message was updated in the queue.
     *
     * @return The exact time a message was updated in the queue.
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
