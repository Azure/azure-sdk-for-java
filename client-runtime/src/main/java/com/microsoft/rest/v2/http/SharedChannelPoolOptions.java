/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

/**
 * Optional configurations for http channel pool.
 */
class SharedChannelPoolOptions {
    // Default duration in sec to keep the connection alive in available pool before closing it.
    private static final long DEFAULT_TTL_OF_IDLE_CHANNEL = 5 * 60;
    private long idleChannelKeepAliveDurationInSec;

    /**
     * Creates SharedChannelPoolOptions.
     */
    SharedChannelPoolOptions() {
        this.idleChannelKeepAliveDurationInSec = DEFAULT_TTL_OF_IDLE_CHANNEL;
    }

    /**
     * Duration in sec to keep the connection alive in available pool before closing it.
     *
     * @param ttlDurationInSec the duration
     * @return SharedChannelPoolOptions
     */
    SharedChannelPoolOptions withIdleChannelKeepAliveDurationInSec(long ttlDurationInSec) {
        this.idleChannelKeepAliveDurationInSec = ttlDurationInSec;
        return this;
    }

    /**
     * @return gets duration in sec the connection alive in available pool before closing it.
     */
    long idleChannelKeepAliveDurationInSec() {
        return this.idleChannelKeepAliveDurationInSec;
    }

    @Override
    public SharedChannelPoolOptions clone() {
        return new SharedChannelPoolOptions()
                .withIdleChannelKeepAliveDurationInSec(this.idleChannelKeepAliveDurationInSec);
    }
}
