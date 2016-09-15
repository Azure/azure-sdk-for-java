/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.concurrent.Future;

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
	 */
    public Future<Boolean> checkpointStoreExists();

    /***
     * Create the checkpoint store if it doesn't exist. Do nothing if it does exist.
     * 
     * @return true if the checkpoint store already exists or was created OK, false if there was a failure
     */
    public Future<Boolean> createCheckpointStoreIfNotExists();
    
    /**
     * Not used by EventProcessorHost, but a convenient function to have for testing.
     * 
     * @return true if the checkpoint store was deleted successfully, false if not
     */
    public Future<Boolean> deleteCheckpointStore();

    /***
     * Get the checkpoint data associated with the given partition. Could return null if no checkpoint has
     * been created for that partition.
     * 
     * @param partitionId  Id of partition to get checkpoint info for.
     * 
     * @return  Checkpoint info for the given partition, or null if none has been previously stored.
     */
    public Future<Checkpoint> getCheckpoint(String partitionId);
    
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
     */
    public Future<Checkpoint> createCheckpointIfNotExists(String partitionId);

    /***
     * Update the checkpoint in the store with the offset/sequenceNumber in the provided checkpoint.
     * 
     * @param checkpoint  offset/sequeceNumber to update the store with.
     *   
     * @return  Void
     */
    public Future<Void> updateCheckpoint(Checkpoint checkpoint);

    /***
     * Delete the stored checkpoint for the given partition. If there is no stored checkpoint for the
     * given partition, that is treated as success.
     * 
     * @param partitionId  id of partition to delete checkpoint from store
     * @return  Void
     */
    public Future<Void> deleteCheckpoint(String partitionId);
}
