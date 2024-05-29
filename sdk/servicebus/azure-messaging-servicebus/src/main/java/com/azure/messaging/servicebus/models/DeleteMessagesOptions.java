// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import java.time.OffsetDateTime;

/**
 * Options to specify when deleting a batch of messages.
 */
public final class DeleteMessagesOptions {
    private OffsetDateTime beforeEnqueueTimeUtc;

    /**
     * Creates a new instance of options to specify when deleting a batch of messages.
     */
    public DeleteMessagesOptions() {
    }

    /**
     * Sets cutoff time for the deletion, only messages that were enqueued before this time will be deleted. If not set,
     * then {@link OffsetDateTime#now()} will be assumed.
     *
     * @param beforeEnqueueTimeUtc the cutoff time for the deletion.
     *
     * @return The updated {@link DeleteMessagesOptions} object.
     */
    public DeleteMessagesOptions setBeforeEnqueueTimeUtc(OffsetDateTime beforeEnqueueTimeUtc) {
        this.beforeEnqueueTimeUtc = beforeEnqueueTimeUtc;
        return this;
    }

    /**
     * Gets the cutoff time for the deletion, only messages that were enqueued before this time will be deleted.
     *
     * @return the cutoff time for the deletion.
     */
    public OffsetDateTime getBeforeEnqueueTimeUtc() {
        return beforeEnqueueTimeUtc;
    }
}
