package com.microsoft.azure.servicebus.management;

import java.time.Instant;

/**
 * Base class for runtime information for entities
 */
public abstract class EntityRuntimeInfo {
    private MessageCountDetails messageCountDetails;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant accessedAt;
    String path;

    /**
     * @return The path of the entity.
     */
    public String getPath() {
        return path;
    }

    /**
     * @return Message count details of the sub-queues of the entity.
     */
    public MessageCountDetails getMessageCountDetails() {
        return messageCountDetails;
    }

    void setMessageCountDetails(MessageCountDetails messageCountDetails) {
        this.messageCountDetails = messageCountDetails;
    }

    /**
     * @return The date-time when entity was created.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return The date-time when the entity was updated
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return The date-time when the entity was last accessed.
     */
    public Instant getAccessedAt() {
        return accessedAt;
    }

    void setAccessedAt(Instant accessedAt) {
        this.accessedAt = accessedAt;
    }
}
