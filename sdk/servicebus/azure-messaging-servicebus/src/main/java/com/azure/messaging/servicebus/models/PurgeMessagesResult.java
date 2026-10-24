// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

/**
 * The result of purging messages from a Service Bus entity.
 */
public final class PurgeMessagesResult {
    private final long deletedCount;

    /**
     * Creates an instance of {@link PurgeMessagesResult}.
     *
     * @param deletedCount The total number of messages deleted by the service.
     */
    public PurgeMessagesResult(long deletedCount) {
        this.deletedCount = deletedCount;
    }

    /**
     * Gets the total number of messages deleted by the service.
     *
     * @return The total number of deleted messages.
     */
    public long getDeletedCount() {
        return deletedCount;
    }
}
