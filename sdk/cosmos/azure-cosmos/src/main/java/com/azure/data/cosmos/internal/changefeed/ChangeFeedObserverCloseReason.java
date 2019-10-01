// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

/**
 * The reason for the {@link ChangeFeedObserver} to close.
 */
public enum ChangeFeedObserverCloseReason {
    /**
     * UNKNOWN failure. This should never be sent to observers.
     */
    UNKNOWN,

    /**
     * The ChangeFeedEventProcessor is shutting down.
     */
    SHUTDOWN,

    /**
     * The resource, such as database or collection was removed.
     */
    RESOURCE_GONE,

    /**
     * Lease was lost due to expiration or load-balancing.
     */
    LEASE_LOST,

    /**
     * ChangeFeedObserver threw an exception.
     */
    OBSERVER_ERROR,

    /**
     * The lease is gone. This can be due to partition split.
     */
    LEASE_GONE,
}
