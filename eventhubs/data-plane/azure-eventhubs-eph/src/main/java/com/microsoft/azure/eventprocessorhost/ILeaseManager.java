// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/***
 * If you wish to have EventProcessorHost store leases somewhere other than Azure Storage,
 * you can write your own lease manager using this interface.  
 *
 * The Azure Storage managers use the same storage for both lease and checkpoints, so both
 * interfaces are implemented by the same class. You are free to do the same thing if you have
 * a unified store for both types of data.
 *
 * This interface does not specify initialization methods because we have no way of knowing what
 * information your implementation will require. If your implementation needs initialization, you
 * will have to initialize the instance before passing it to the EventProcessorHost constructor.
 */
public interface ILeaseManager {
    /**
     * The lease duration is mostly internal to the lease manager implementation but may be needed
     * by other parts of the event processor host.
     *
     * @return Duration of a lease before it expires unless renewed, specified in milliseconds.
     */
    int getLeaseDurationInMilliseconds();

    /**
     * Does the lease store exist?
     * <p>
     * The returned CompletableFuture completes with true if the checkpoint store exists or false if it
     * does not. It completes exceptionally on error.
     *
     * @return CompletableFuture {@literal ->} true if it exists, false if not
     */
    CompletableFuture<Boolean> leaseStoreExists();

    /**
     * Create the lease store if it does not exist, do nothing if it does exist.
     *
     * @return CompletableFuture {@literal ->} null on success, completes exceptionally on error.
     */
    CompletableFuture<Void> createLeaseStoreIfNotExists();

    /**
     * Deletes the lease store.
     *
     * @return CompletableFuture {@literal ->} null on success, completes exceptionally on error.
     */
    CompletableFuture<Void> deleteLeaseStore();

    /**
     * Returns the lease info for the given partition..
     *
     * @param partitionId  Get the lease info for this partition.
     * @return CompletableFuture {@literal ->} Lease, completes exceptionally on error.
     */
    CompletableFuture<CompleteLease> getLease(String partitionId);

    /**
     * Returns lightweight BaseLease for all leases, which includes name of owning host and whether lease
     * is expired. An implementation is free to return CompleteLease or its own class derived from CompleteLease,
     * but it is important that getAllLeases run as fast as possible. If it is faster to obtain only the
     * information required for a BaseLease, we heavily recommend doing that.
     * 
     * @return CompletableFuture {@literal ->} list of BaseLease, completes exceptionally on error.
     */
    CompletableFuture<List<BaseLease>> getAllLeases();
    

    /**
     * Create in the store a lease for each of the given partitions, if it does not exist. Do nothing for any
     * lease which exists in the store already.
     *
     * @param partitionIds ids of partitions to create lease info for
     * @return CompletableFuture {@literal ->} null on success, completes exceptionally on error
     */
    CompletableFuture<Void> createAllLeasesIfNotExists(List<String> partitionIds);

    /**
     * Delete the lease info for a partition from the store. If there is no stored lease for the given partition,
     * that is treated as success.
     *
     * @param lease the currently existing lease info for the partition
     * @return CompletableFuture {@literal ->} null on success, completes exceptionally on error.
     */
    CompletableFuture<Void> deleteLease(CompleteLease lease);

    /**
     * Acquire the lease on the desired partition for this EventProcessorHost.
     * <p>
     * Note that it is legal to acquire a lease that is currently owned by another host, which is called "stealing".
     * Lease-stealing is how partitions are redistributed when additional hosts are started.
     * <p>
     * The existing Azure Storage implementation can experience races between two host instances attempting to acquire or steal
     * the lease at the same time. To avoid situations where two host instances both believe that they own the lease, acquisition
     * can fail non-exceptionally by returning false and should do so when there is any doubt -- the worst that can happen is that
     * no host instance owns the lease for a short time. This is qualitatively different from, for example, the underlying store
     * throwing an access exception, which is an error and should complete exceptionally.
     *
     * @param lease Lease info for the desired partition
     * @return CompletableFuture {@literal ->} true if the lease was acquired, false if not, completes exceptionally on error.
     */
    CompletableFuture<Boolean> acquireLease(CompleteLease lease);

    /**
     * Renew a lease currently held by this host instance.
     * <p>
     * If the lease has been taken by another host instance (either stolen or after expiration) or explicitly released,
     * renewLease must return false. With the Azure Storage-based implementation, it IS possible to renew an expired lease
     * that has not been taken by another host, so your implementation can allow that or not, whichever is convenient. If
     * it does not, renewLease should return false.
     *
     * @param lease Lease to be renewed
     * @return true if the lease was renewed, false as described above, completes exceptionally on error.
     */
    CompletableFuture<Boolean> renewLease(CompleteLease lease);

    /**
     * Give up a lease currently held by this host.
     * <p>
     * If the lease has expired or been taken by another host, releasing it is unnecessary but will succeed since the intent
     * has been fulfilled.
     *
     * @param lease Lease to be given up
     * @return CompletableFuture {@literal ->} null on success, completes exceptionally on error.
     */
    CompletableFuture<Void> releaseLease(CompleteLease lease);

    /**
     * Update the store with the information in the provided lease.
     * <p>
     * It is necessary to currently hold a lease in order to update it. If the lease has been stolen, or expired, or
     * released, it cannot be updated. Lease manager implementations should renew the lease before performing the update to avoid lease
     * expiration during the process.
     *
     * @param lease New lease info to be stored
     * @return true if the update was successful, false if lease was lost and could not be updated, completes exceptionally on error.
     */
    CompletableFuture<Boolean> updateLease(CompleteLease lease);
}
