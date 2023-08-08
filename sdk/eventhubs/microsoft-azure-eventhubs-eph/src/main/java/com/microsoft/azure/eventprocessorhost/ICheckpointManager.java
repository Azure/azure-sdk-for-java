// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/***
 * If you wish to have EventProcessorHost store checkpoints somewhere other than Azure Storage,
 * you can write your own checkpoint manager using this interface.  
 *
 * The Azure Storage managers use the same storage for both lease and checkpoints, so both
 * interfaces are implemented by the same class. You are free to do the same thing if you have
 * a unified store for both types of data.
 *
 * This interface does not specify initialization methods because we have no way of knowing what
 * information your implementation will require. If your implementation needs initialization, you
 * will have to initialize the instance before passing it to the EventProcessorHost constructor.
 */
public interface ICheckpointManager {
    /***
     * Does the checkpoint store exist?
     *
     * The returned CompletableFuture completes with true if the checkpoint store exists or false if it
     * does not. It completes exceptionally on error.
     *
     * @return CompletableFuture {@literal ->} true if it exists, false if not
     */
    CompletableFuture<Boolean> checkpointStoreExists();

    /***
     * Create the checkpoint store if it doesn't exist. Do nothing if it does exist.
     *
     * @return CompletableFuture {@literal ->} null on success, completes exceptionally on error.
     */
    CompletableFuture<Void> createCheckpointStoreIfNotExists();

    /**
     * Deletes the checkpoint store.
     *
     * @return CompletableFuture {@literal ->} null on success, completes exceptionally on error.
     */
    CompletableFuture<Void> deleteCheckpointStore();

    /***
     * Get the checkpoint data associated with the given partition. Could return null if no checkpoint has
     * been created for that partition.
     *
     * @param partitionId  Id of partition to get checkpoint info for.
     *
     * @return CompletableFuture {@literal ->} checkpoint info, or null. Completes exceptionally on error.
     */
    CompletableFuture<Checkpoint> getCheckpoint(String partitionId);

    /***
     * Creates the checkpoint HOLDERs for the given partitions. Does nothing for any checkpoint HOLDERs
     * that already exist.
     *
     * The semantics of this are complicated because it is possible to use the same store for both
     * leases and checkpoints (the Azure Storage implementation does so) and it is required to
     * have a lease for every partition but it is not required to have a checkpoint for a partition.
     * It is a valid scenario to never use checkpoints at all, so it is important for the store to
     * distinguish between creating the structure(s) that will hold a checkpoint and actually creating
     * a checkpoint (storing an offset/sequence number pair in the structure).
     *
     * @param partitionIds  List of partitions to create checkpoint HOLDERs for.
     * @return CompletableFuture {@literal ->} null on success, completes exceptionally on error.
     */
    CompletableFuture<Void> createAllCheckpointsIfNotExists(List<String> partitionIds);

    /***
     * Update the checkpoint in the store with the offset/sequenceNumber in the provided checkpoint.
     *
     * The lease argument is necessary to make the Azure Storage implementation work correctly: the
     * Azure Storage implementation stores the checkpoint as part of the lease and we cannot completely
     * hide the connection between the two. If your implementation does not have this limitation, you are
     * free to ignore the lease argument.
     *
     * @param lease          lease for the partition to be checkpointed.
     * @param checkpoint  offset/sequenceNumber and partition id to update the store with.
     * @return CompletableFuture {@literal ->} null on success. Completes exceptionally on error.
     */
    CompletableFuture<Void> updateCheckpoint(CompleteLease lease, Checkpoint checkpoint);

    /***
     * Delete the stored checkpoint data for the given partition. If there is no stored checkpoint for the
     * given partition, that is treated as success. Deleting the checkpoint HOLDER is allowed but not required;
     * your implementation is free to do whichever is more convenient. 
     *
     * @param partitionId  id of partition to delete checkpoint from store
     * @return CompletableFuture {@literal ->} null on success. Completes exceptionally on error.
     */
    CompletableFuture<Void> deleteCheckpoint(String partitionId);
}
