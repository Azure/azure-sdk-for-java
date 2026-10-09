// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

/**
 * The result of deleting a batch of messages from a Service Bus entity.
 */
public final class DeleteMessagesResult {
    private final long deletedCount;

    /**
     * Creates an instance of {@link DeleteMessagesResult}.
     *
     * @param deletedCount The number of messages deleted by the service.
     */
    public DeleteMessagesResult(long deletedCount) {
        this.deletedCount = deletedCount;
    }

    /**
     * Gets the number of messages deleted by the service.
     *
     * @return The number of deleted messages.
     */
    public long getDeletedCount() {
        return deletedCount;
    }
}
