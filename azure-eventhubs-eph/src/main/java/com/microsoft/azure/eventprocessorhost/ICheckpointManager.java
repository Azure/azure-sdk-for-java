/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

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
 * information your implementation will require.
 */
public interface ICheckpointManager
{
	/***
	 * Does the checkpoint store exist?
	 * 
	 * @return true if it exists, false if not
	 * @throws ExceptionWithAction with action EventProcessorHostActionStrings.CHECKING_CHECKPOINT_STORE on error
	 */
    public CompletableFuture<Boolean> checkpointStoreExists();

    /***
     * Create the checkpoint store if it doesn't exist. Do nothing if it does exist.
     * 
     * @return Void
     * @throws ExceptionWithAction with action EventProcessorHostActionStrings.CREATING_CHECKPOINT_STORE on error
     */
    public CompletableFuture<Void> createCheckpointStoreIfNotExists();
    
    /**
     * Not used by EventProcessorHost, but a convenient function to have for testing.
     * 
     * @return true if the checkpoint store was deleted successfully, false if not
     */
    public CompletableFuture<Boolean> deleteCheckpointStore();

    /***
     * Get the checkpoint data associated with the given partition. Could return null if no checkpoint has
     * been created for that partition.
     * 
     * @param partitionId  Id of partition to get checkpoint info for.
     * 
     * @return  Checkpoint info for the given partition, or null if none has been previously stored.
     * @throws ExceptionWithAction with action EventProcessorHostActionStrings.GETTING_CHECKPOINT on error
     */
    public CompletableFuture<Checkpoint> getCheckpoint(String partitionId);
    
    /***
     * Create the checkpoint HOLDER for the given partition if it doesn't exist. This method is about
     * initializing the store by ensuring that a place exists to put a checkpoint if the user creates
     * one. This method will always return null if the checkpoint holder did not previously exist, but
     * can return null at other times if the user has not created a checkpoint for given partition. It
     * is legal to never create a checkpoint for a partition.
     * 
     * The offset/sequenceNumber for a freshly-created checkpoint should be set to START_OF_STREAM/0.
     * 
     * @param partitionId  Id of partition to create the checkpoint for.
     *  
     * @return  The checkpoint for the given partition, if one exists, or null.
     * @throws ExceptionWithAction with action EventProcessorHostActionStrings.CREATING_CHECKPOINT on error
     */
    public CompletableFuture<Checkpoint> createCheckpointIfNotExists(String partitionId);

    /***
     * Update the checkpoint in the store with the offset/sequenceNumber in the provided checkpoint.
     * 
     * The lease argument is necessary to make the Azure Storage implementation work correctly. The
     * Azure Storage implementation stores the checkpoint as part of the lease and we cannot completely
     * hide the connection between the two. If you are doing an implementation which does not have this
     * limitation, you are free to ignore the lease argument.
     * 
     * @param lease		  lease for the partition to be checkpointed.
     * @param checkpoint  offset/sequenceNumber and partition id to update the store with.
     *   
     * @return  void
     * @throws ExceptionWithAction with action EventProcessorHostActionStrings.UPDATING_CHECKPOINT on error
     */
    public CompletableFuture<Void> updateCheckpoint(Lease lease, Checkpoint checkpoint);
    
    /***
     * Delete the stored checkpoint for the given partition. If there is no stored checkpoint for the
     * given partition, that is treated as success. Not currently used by EventProcessorHost.
     * 
     * @param partitionId  id of partition to delete checkpoint from store
     * @return  void
     */
    public CompletableFuture<Void> deleteCheckpoint(String partitionId);
}
