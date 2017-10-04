/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/***
 * An ICheckpointManager implementation based on an in-memory store. 
 *
 * THIS CLASS IS PROVIDED AS A CONVENIENCE FOR TESTING ONLY. All data stored via this class is in memory
 * only and not persisted in any way. In addition, it is only visible within the same process: multiple
 * instances of EventProcessorHost in the same process will share the same in-memory store and checkpoints
 * created by one will be visible to the others, but that is not true across processes.
 * 
 * With an ordinary store, there is a clear and distinct line between the values that are persisted
 * and the values that are live in memory. With an in-memory store, that line gets blurry. If we
 * accidentally hand out a reference to the in-store object, then the calling code is operating on
 * the "persisted" values without going through the manager and behavior will be very different.
 * Hence, the implementation takes pains to distinguish between references to "live" and "persisted"
 * checkpoints.
 * 
 * To use this class, create a new instance and pass it to the EventProcessorHost constructor that takes
 * ICheckpointManager as an argument. After the EventProcessorHost instance is constructed, be sure to
 * call initialize() on this object before starting processing with EventProcessorHost.registerEventProcessor()
 * or EventProcessorHost.registerEventProcessorFactory().
 */
public class InMemoryCheckpointManager implements ICheckpointManager
{
    private EventProcessorHost host;
    private ExecutorService executor;

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(InMemoryCheckpointManager.class);

    public InMemoryCheckpointManager()
    {
    	this.executor = Executors.newCachedThreadPool();
    }

    // This object is constructed before the EventProcessorHost and passed as an argument to
    // EventProcessorHost's constructor. So it has to get a reference to the EventProcessorHost later.
    public void initialize(EventProcessorHost host)
    {
        this.host = host;
    }

    @Override
    public Future<Boolean> checkpointStoreExists()
    {
    	return this.executor.submit(() -> checkpointStoreExistsSync());
    }
    
    private Boolean checkpointStoreExistsSync()
    {
    	return InMemoryCheckpointStore.singleton.existsMap();
    }

    @Override
    public Future<Boolean> createCheckpointStoreIfNotExists()
    {
        return this.executor.submit(() -> createCheckpointStoreIfNotExistsSync());
    }

    private Boolean createCheckpointStoreIfNotExistsSync()
    {
        InMemoryCheckpointStore.singleton.initializeMap();
        return true;
    }
    
    @Override
    public Future<Boolean> deleteCheckpointStore()
    {
    	return this.executor.submit(() -> deleteCheckpointStoreSync());
    }
    
    private Boolean deleteCheckpointStoreSync()
    {
    	InMemoryCheckpointStore.singleton.deleteMap();
    	return true;
    }
    
    @Override
    public Future<Checkpoint> getCheckpoint(String partitionId)
    {
        return this.executor.submit(() -> getCheckpointSync(partitionId));
    }
    
    private Checkpoint getCheckpointSync(String partitionId)
    {
    	Checkpoint returnCheckpoint = null;
        Checkpoint checkpointInStore = InMemoryCheckpointStore.singleton.getCheckpoint(partitionId);
        if (checkpointInStore == null)
        {
        	TRACE_LOGGER.warn(LoggingUtils.withHostAndPartition(this.host.getHostName(), partitionId,
                    "getCheckpoint() no existing Checkpoint"));
        	returnCheckpoint = null;
        }
        else if (checkpointInStore.getSequenceNumber() == -1)
        {
        	// Uninitialized, so return null.
        	returnCheckpoint = null;
        }
        else
        {
        	returnCheckpoint = new Checkpoint(checkpointInStore);
        }
        return returnCheckpoint;
    }
    
    @Override
    public Future<Checkpoint> createCheckpointIfNotExists(String partitionId)
    {
    	return this.executor.submit(() -> createCheckpointIfNotExistsSync(partitionId));
    }
    
    private Checkpoint createCheckpointIfNotExistsSync(String partitionId)
    {
    	Checkpoint checkpointInStore = InMemoryCheckpointStore.singleton.getCheckpoint(partitionId);
    	Checkpoint returnCheckpoint = null;
    	if (checkpointInStore != null)
    	{
        	TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), partitionId,
                    "createCheckpointIfNotExists() found existing checkpoint, OK"));
        	if (checkpointInStore.getSequenceNumber() != -1)
        	{
        		returnCheckpoint = new Checkpoint(checkpointInStore);
        	}
        	else
        	{
        		// The checkpoint is uninitialized so return null to match the behavior of AzureStorageCheckpointLeaseMananger.
        		returnCheckpoint = null;
        	}
    	}
    	else
    	{
        	TRACE_LOGGER.info(LoggingUtils.withHostAndPartition(this.host.getHostName(), partitionId,
                    "createCheckpointIfNotExists() creating new checkpoint"));
        	Checkpoint newStoreCheckpoint = new Checkpoint(partitionId);
        	newStoreCheckpoint.setOffset(null);
        	newStoreCheckpoint.setSequenceNumber(-1);
            InMemoryCheckpointStore.singleton.setOrReplaceCheckpoint(newStoreCheckpoint);
        	// This API actually creates the holder, not the checkpoint itself. In this implementation, we do create a Checkpoint object
            // and put it in the store, but the values are set to indicate that it is not initialized. Meanwhile, return null to match the
            // behavior of AzureStorageCheckpointLeaseMananger.
            returnCheckpoint = null;
    	}
    	return returnCheckpoint;
    }

    @Deprecated
    @Override
    public Future<Void> updateCheckpoint(Checkpoint checkpoint)
    {
    	return null;
    }
    
    @Override
    public Future<Void> updateCheckpoint(Lease lease, Checkpoint checkpoint)
    {
        return this.executor.submit(() -> updateCheckpointSync(checkpoint.getPartitionId(), checkpoint.getOffset(), checkpoint.getSequenceNumber()));
    }

    private Void updateCheckpointSync(String partitionId, String offset, long sequenceNumber)
    {
    	Checkpoint checkpointInStore = InMemoryCheckpointStore.singleton.getCheckpoint(partitionId);
    	if (checkpointInStore != null)
    	{
    		// No live checkpoint is provided, so we can only update the persisted one.
    		checkpointInStore.setOffset(offset);
    		checkpointInStore.setSequenceNumber(sequenceNumber);
    	}
    	else
    	{
    		TRACE_LOGGER.warn(LoggingUtils.withHostAndPartition(this.host.getHostName(), partitionId,
                    "updateCheckpoint() can't find checkpoint"));
    	}
    	return null;
    }

    @Override
    public Future<Void> deleteCheckpoint(String partitionId)
    {
    	return this.executor.submit(() -> deleteCheckpointSync(partitionId));
    }
    
    private Void deleteCheckpointSync(String partitionId)
    {
    	InMemoryCheckpointStore.singleton.removeCheckpoint(partitionId);
    	return null;
    }


    private static class InMemoryCheckpointStore
    {
        final static InMemoryCheckpointStore singleton = new InMemoryCheckpointStore();

        private ConcurrentHashMap<String, Checkpoint> inMemoryCheckpointsPrivate = null;
        
        synchronized boolean existsMap()
        {
        	return (this.inMemoryCheckpointsPrivate != null);
        }
        
        synchronized void initializeMap()
        {
        	if (this.inMemoryCheckpointsPrivate == null)
        	{
        		this.inMemoryCheckpointsPrivate = new ConcurrentHashMap<String, Checkpoint>();
        	}
        }
        
        synchronized void deleteMap()
        {
        	this.inMemoryCheckpointsPrivate = null;
        }
        
        synchronized Checkpoint getCheckpoint(String partitionId)
        {
        	return this.inMemoryCheckpointsPrivate.get(partitionId);
        }
        
        synchronized void setOrReplaceCheckpoint(Checkpoint newCheckpoint)
        {
        	this.inMemoryCheckpointsPrivate.put(newCheckpoint.getPartitionId(), newCheckpoint);
        }
        
        synchronized void removeCheckpoint(String partitionId)
        {
        	this.inMemoryCheckpointsPrivate.remove(partitionId);
        }
    }
}
