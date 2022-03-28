// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed;

import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.LeaseStoreManagerImpl;
import com.azure.cosmos.CosmosAsyncContainer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Defines an interface for operations with {@link Lease}.
 */
public interface LeaseStoreManager extends LeaseContainer, LeaseManager, LeaseStore, LeaseCheckpointer
{
    /**
     * Provides flexible way to buildAsyncClient lease manager constructor parameters.
     * For the actual creation of lease manager instance, delegates to lease manager factory.
     */
    interface LeaseStoreManagerBuilderDefinition {
        LeaseStoreManagerBuilderDefinition leaseContextClient(ChangeFeedContextClient leaseContextClient);

        LeaseStoreManagerBuilderDefinition leasePrefix(String leasePrefix);

        LeaseStoreManagerBuilderDefinition leaseContainer(CosmosAsyncContainer leaseContainer);

        LeaseStoreManagerBuilderDefinition monitoredContainer(CosmosAsyncContainer monitoredContainer);

        LeaseStoreManagerBuilderDefinition monitoredContainerRid(String monitoredContainerRid);

        LeaseStoreManagerBuilderDefinition requestOptionsFactory(RequestOptionsFactory requestOptionsFactory);

        LeaseStoreManagerBuilderDefinition hostName(String hostName);

        Mono<LeaseStoreManager> build();
    }

    static LeaseStoreManagerBuilderDefinition builder() {
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
     * @param cancellationToken the cancellation token.
     * @return the updated renewed lease.
     */
    Mono<Lease> checkpoint(Lease lease, String continuationToken, CancellationToken cancellationToken);

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
