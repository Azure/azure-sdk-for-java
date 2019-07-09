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
package com.azure.data.cosmos.internal.changefeed;

import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.internal.changefeed.implementation.LeaseStoreManagerImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Defines an interface for operations with {@link Lease}.
 */
public interface LeaseStoreManager extends LeaseContainer, LeaseManager, LeaseStore, LeaseCheckpointer
{
    /**
     * Provides flexible way to build lease manager constructor parameters.
     * For the actual creation of lease manager instance, delegates to lease manager factory.
     */
    interface LeaseStoreManagerBuilderDefinition {
        LeaseStoreManagerBuilderDefinition leaseContextClient(ChangeFeedContextClient leaseContextClient);

        LeaseStoreManagerBuilderDefinition leasePrefix(String leasePrefix);

        LeaseStoreManagerBuilderDefinition leaseCollectionLink(CosmosContainer leaseCollectionLink);

        LeaseStoreManagerBuilderDefinition requestOptionsFactory(RequestOptionsFactory requestOptionsFactory);

        LeaseStoreManagerBuilderDefinition hostName(String hostName);

        Mono<LeaseStoreManager> build();
    }

    static LeaseStoreManagerBuilderDefinition Builder() {
        return new LeaseStoreManagerImpl();
    }

    /**
     * @return List of all leases.
     */
    Flux<Lease> getAllLeases();

    /**
     * @return all leases owned by the current host.
     */
    Flux<Lease> getOwnedLeases();

    /**
     * Checks whether the lease exists and creates it if it does not exist.
     *
     * @param leaseToken the partition to work on.
     * @param continuationToken the continuation token if it exists.
     * @return the lease.
     */
    Mono<Lease> createLeaseIfNotExist(String leaseToken, String continuationToken);

    /**
     * DELETE the lease.
     *
     * @param lease the lease to remove.
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> delete(Lease lease);

    /**
     * Acquire ownership of the lease.
     *
     * @param lease the Lease to acquire.
     * @return the updated acquired lease.
     */
    Mono<Lease> acquire(Lease lease);

    /**
     * Release ownership of the lease.
     *
     * @param lease the lease to acquire.
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> release(Lease lease);

    /**
     * Renew the lease. Leases are periodically renewed to prevent expiration.
     *
     * @param lease the Lease to renew.
     * @return the updated renewed lease.
     */
    Mono<Lease> renew(Lease lease);

    /**
     * REPLACE item from the specified lease.
     *
     * @param leaseToUpdatePropertiesFrom the Lease containing new item.
     * @return the updated lease.
     */
    Mono<Lease> updateProperties(Lease leaseToUpdatePropertiesFrom);

    /**
     * Checkpoint the lease.
     *
     * @param lease the Lease to renew.
     * @param continuationToken the continuation token.
     * @return the updated renewed lease.
     */
    Mono<Lease> checkpoint(Lease lease, String continuationToken);

    /**
     * @return true if the lease store is initialized.
     */
    Mono<Boolean> isInitialized();

    /**
     * Mark the store as initialized.
     *
     * @return true if marked as initialized.
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
     * @return true if the lock was acquired and was relesed, false if the lock was not acquired.
     */
    Mono<Boolean> releaseInitializationLock();
}
