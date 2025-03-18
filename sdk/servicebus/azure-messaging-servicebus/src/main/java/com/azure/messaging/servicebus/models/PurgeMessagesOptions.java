// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import java.time.OffsetDateTime;

/**
 * Options to specify when purging messages.
 */
public final class PurgeMessagesOptions {
    private OffsetDateTime beforeEnqueueTimeUtc;

    /**
     * Creates a new instance of options to specify when purging messages.
     */
    public PurgeMessagesOptions() {
    }

    /**
     * Sets cutoff time for the purging, only messages that were enqueued before this time will be purged. If not set,
     * then {@link OffsetDateTime#now()} will be assumed.
     *
     * @param beforeEnqueueTimeUtc the cutoff time for the deletion.
     *
     * @return The updated {@link PurgeMessagesOptions} object.
     */
    public PurgeMessagesOptions setBeforeEnqueueTimeUtc(OffsetDateTime beforeEnqueueTimeUtc) {
        this.beforeEnqueueTimeUtc = beforeEnqueueTimeUtc;
        return this;
    }

    /**
     * Gets the cutoff time for the purging, only messages that were enqueued before this time will be purged.
     *
     * @return the cutoff time for the purging.
     */
    public OffsetDateTime getBeforeEnqueueTimeUtc() {
        return beforeEnqueueTimeUtc;
    }
}
