// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import com.azure.data.cosmos.internal.changefeed.exceptions.LeaseLostException;
import reactor.core.publisher.Mono;

/**
 * It defines a way to perform operations with {@link Lease}.
 */
public interface LeaseManager {
    /**
     * Checks whether the lease exists and creates it if it does not exist.
     *
     * @param leaseToken the lease token to work with.
     * @param continuationToken the continuation token if it exists.
     * @return the lease.
     */
    Mono<Lease> createLeaseIfNotExist(String leaseToken, String continuationToken);

    /**
     * Deletes the lease.
     *
     * @param lease the lease to delete.
     * @return a deferred computation of this call.
     */
    Mono<Void> delete(Lease lease);

    /**
     * Acquires ownership of the lease.
     *   It can throw {@link LeaseLostException} if other host acquired concurrently the lease.
     *
     * @param lease the lease to acquire.
     * @return the updated lease.
     */
    Mono<Lease> acquire(Lease lease);

    /**
     * It releases ownership of the lease.
     *   It can throw {@link LeaseLostException} if other host acquired the lease.
     *
     * @param lease the lease to acquire.
     * @return a deferred computation of this call.
     */
    Mono<Void> release(Lease lease);

    /**
     * Renew the lease; leases are periodically renewed to prevent expiration.
     *   It can throw {@link LeaseLostException} if other host acquired the lease.
     *
     * @param lease the lease to renew.
     * @return the updated lease.
     */
    Mono<Lease> renew(Lease lease);

    /**
     * REPLACE item from the specified lease.
     *   It can throw {@link LeaseLostException} if other host acquired the lease.
     *
     * @param leaseToUpdatePropertiesFrom the new item.
     * @return updated lease.
     */
    Mono<Lease> updateProperties(Lease leaseToUpdatePropertiesFrom);
}
