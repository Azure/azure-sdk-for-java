// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class SequenceAckId {

    private final AtomicLong sequenceId = new AtomicLong(0);
    private final AtomicBoolean updated = new AtomicBoolean(false);

    /**
     * Gets latest sequenceId.
     * Sets updated to false.
     *
     * @return the latest sequenceId. {@code null} if updated is false.
     */
    public Long getUpdated() {
        if (updated.compareAndSet(true, false)) {
            return sequenceId.get();
        } else {
            return null;
        }
    }

    /**
     * Sets updated to true. Usually called when previous send of SequenceAckMessage failed.
     */
    public void setUpdated() {
        updated.set(true);
    }

    /**
     * Sets latest sequenceId.
     * Sets updated to true.
     *
     * @param id the sequenceId.
     * @return whether it is a new message.
     */
    public boolean update(long id) {
        long previousId = sequenceId.getAndUpdate(existId -> Math.max(id, existId));

        // Every time we got a message with sequence-id, we need to response a sequence ack after a period.
        // Consider we receive 1,2,3 and connection drops. After recovery, we may get id 2. We need to also
        // response 3 to tell the service what we've got.
        updated.set(true);

        return id > previousId;
    }
}
