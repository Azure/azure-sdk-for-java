// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Represents the lease store container to deal with initialization/cleanup of leases
 *  for particular monitoring collection and lease container prefix.
 */
public interface LeaseStore {

    /**
     * @return true if the lease store is initialized.
     */
    Mono<Boolean> isInitialized();

    /**
     * Mark the store as initialized.
     *
     * @return a deferred computation of this operation call.
     */
    Mono<Boolean> markInitialized();

    /**
     * Places a lock on the lease store for initialization. Only one process may own the store for the lock time.
     *
     * @param lockExpirationTime the time for the lock to expire.
     * @return true if the lock was acquired, false otherwise.
     */
    Mono<Boolean> acquireInitializationLock(Duration lockExpirationTime);

    /**
     * Releases the lock one the lease store for initialization.
     *
     * @return true if the lock was acquired and was released, false if the lock was not acquired.
     */
    Mono<Boolean> releaseInitializationLock();
}
