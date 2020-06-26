// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Runtime information about the queue.
 */
@Immutable
public class QueueRuntimeInfo {
    private final String name;
    private final MessageCountDetails details;
    private final long messageCount;
    private final long sizeInBytes;
    private final OffsetDateTime accessedAt;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    /**
     * Creates a new instance with runtime properties extracted from the given QueueDescription.
     *
     * @param queueDescription Queue description to extract runtime information from.
     *
     * @throws NullPointerException if {@code queueDescription} is null.
     */
    public QueueRuntimeInfo(QueueDescription queueDescription) {
        Objects.requireNonNull(queueDescription, "'queueDescription' cannot be null.");
        this.name = queueDescription.getName();
        this.details = queueDescription.getMessageCountDetails();
        this.messageCount = queueDescription.getMessageCount();
        this.sizeInBytes = queueDescription.getSizeInBytes();
        this.accessedAt = queueDescription.getAccessedAt();
        this.createdAt = queueDescription.getCreatedAt();
        this.updatedAt = queueDescription.getUpdatedAt();
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
     * Gets the exact time the queue was created.
     *
     * @return The exact time the queue was created.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets details about the message counts in queue.
     *
     * @return Details about the message counts in queue.
     */
    public MessageCountDetails getDetails() {
        return details;
    }

    /**
     * Gets the number of messages in the queue.
     *
     * @return The number of messages in the queue.
     */
    public long getMessageCount() {
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
     * Gets the size of the queue, in bytes.
     *
     * @return The size of the queue, in bytes.
     */
    public long getSizeInBytes() {
        return sizeInBytes;
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
