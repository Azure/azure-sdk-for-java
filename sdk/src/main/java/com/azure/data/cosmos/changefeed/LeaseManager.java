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

import com.azure.data.cosmos.changefeed.exceptions.LeaseLostException;

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
