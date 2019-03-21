// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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
public class InMemoryCheckpointManager implements ICheckpointManager {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(InMemoryCheckpointManager.class);
    private HostContext hostContext;

    public InMemoryCheckpointManager() {
    }

    // This object is constructed before the EventProcessorHost and passed as an argument to
    // EventProcessorHost's constructor. So it has to get context info later.
    public void initialize(HostContext hostContext) {
        this.hostContext = hostContext;
    }

    @Override
    public CompletableFuture<Boolean> checkpointStoreExists() {
        boolean exists = InMemoryCheckpointStore.SINGLETON.existsMap();
        TRACE_LOGGER.debug(this.hostContext.withHost("checkpointStoreExists() " + exists));
        return CompletableFuture.completedFuture(exists);
    }

    @Override
    public CompletableFuture<Void> createCheckpointStoreIfNotExists() {
        TRACE_LOGGER.debug(this.hostContext.withHost("createCheckpointStoreIfNotExists()"));
        InMemoryCheckpointStore.SINGLETON.initializeMap();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteCheckpointStore() {
        TRACE_LOGGER.debug(this.hostContext.withHost("deleteCheckpointStore()"));
        InMemoryCheckpointStore.SINGLETON.deleteMap();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Checkpoint> getCheckpoint(String partitionId) {
        Checkpoint returnCheckpoint = null;
        Checkpoint checkpointInStore = InMemoryCheckpointStore.SINGLETON.getCheckpoint(partitionId);
        if (checkpointInStore == null) {
            TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(partitionId,
                    "getCheckpoint() no existing Checkpoint"));
            returnCheckpoint = null;
        } else if (checkpointInStore.getSequenceNumber() == -1) {
            // Uninitialized, so return null.
            TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(partitionId, "getCheckpoint() uninitalized"));
            returnCheckpoint = null;
        } else {
            TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(partitionId,
                    "getCheckpoint() found " + checkpointInStore.getOffset() + "//" + checkpointInStore.getSequenceNumber()));
            returnCheckpoint = new Checkpoint(checkpointInStore);
        }
        return CompletableFuture.completedFuture(returnCheckpoint);
    }

    @Override
    public CompletableFuture<Void> createAllCheckpointsIfNotExists(List<String> partitionIds) {
        for (String id : partitionIds) {
            Checkpoint checkpointInStore = InMemoryCheckpointStore.SINGLETON.getCheckpoint(id);
            if (checkpointInStore != null) {
                TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(id,
                        "createCheckpointIfNotExists() found existing checkpoint, OK"));
            } else {
                TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(id,
                        "createCheckpointIfNotExists() creating new checkpoint"));
                Checkpoint newStoreCheckpoint = new Checkpoint(id);
                // This API actually creates the holder, not the checkpoint itself. In this implementation, we do create a Checkpoint object
                // and put it in the store, but the values are set to indicate that it is not initialized.
                newStoreCheckpoint.setOffset(null);
                newStoreCheckpoint.setSequenceNumber(-1);
                InMemoryCheckpointStore.SINGLETON.setOrReplaceCheckpoint(newStoreCheckpoint);
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> updateCheckpoint(CompleteLease lease, Checkpoint checkpoint) {
        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(checkpoint.getPartitionId(),
                "updateCheckpoint() " + checkpoint.getOffset() + "//" + checkpoint.getSequenceNumber()));
        Checkpoint checkpointInStore = InMemoryCheckpointStore.SINGLETON.getCheckpoint(checkpoint.getPartitionId());
        if (checkpointInStore != null) {
            checkpointInStore.setOffset(checkpoint.getOffset());
            checkpointInStore.setSequenceNumber(checkpoint.getSequenceNumber());
        } else {
            TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(checkpoint.getPartitionId(),
                    "updateCheckpoint() can't find checkpoint"));
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteCheckpoint(String partitionId) {
        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(partitionId, "deleteCheckpoint()"));
        InMemoryCheckpointStore.SINGLETON.removeCheckpoint(partitionId);
        return CompletableFuture.completedFuture(null);
    }


    private static class InMemoryCheckpointStore {
        static final InMemoryCheckpointStore SINGLETON = new InMemoryCheckpointStore();

        private ConcurrentHashMap<String, Checkpoint> inMemoryCheckpointsPrivate = null;

        synchronized boolean existsMap() {
            return (this.inMemoryCheckpointsPrivate != null);
        }

        synchronized void initializeMap() {
            if (this.inMemoryCheckpointsPrivate == null) {
                this.inMemoryCheckpointsPrivate = new ConcurrentHashMap<String, Checkpoint>();
            }
        }

        synchronized void deleteMap() {
            this.inMemoryCheckpointsPrivate = null;
        }

        synchronized Checkpoint getCheckpoint(String partitionId) {
            return this.inMemoryCheckpointsPrivate.get(partitionId);
        }

        synchronized void setOrReplaceCheckpoint(Checkpoint newCheckpoint) {
            this.inMemoryCheckpointsPrivate.put(newCheckpoint.getPartitionId(), newCheckpoint);
        }

        synchronized void removeCheckpoint(String partitionId) {
            this.inMemoryCheckpointsPrivate.remove(partitionId);
        }
    }
}
