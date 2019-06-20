// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.models;

import java.util.Map;

public final class QueueProperties {
    /**
     * The metadata property.
     */
    private final Map<String, String> metadata;

    /**
     * The approximate number of messages in the queue. This number is not
     * lower than the actual number of messages in the queue, but could be
     * higher.
     */
    private final int approximateMessagesCount;

    public QueueProperties(Map<String, String> metadata, int approximateMessagesCount) {
        this.metadata = metadata;
        this.approximateMessagesCount = approximateMessagesCount;
    }

    public Map<String, String> getMetadata() {
        return this.metadata; // Clone this?
    }

    public int getApproximateMessagesCount() {
        return approximateMessagesCount;
    }
}
