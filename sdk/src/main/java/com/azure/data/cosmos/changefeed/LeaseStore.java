/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.changefeed;

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
