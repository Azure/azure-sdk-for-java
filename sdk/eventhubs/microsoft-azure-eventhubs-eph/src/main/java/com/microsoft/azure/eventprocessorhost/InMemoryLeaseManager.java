// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/***
 * An ILeaseManager implementation based on an in-memory store.
 *
 * THIS CLASS IS PROVIDED AS A CONVENIENCE FOR TESTING ONLY. All data stored via this class is in memory
 * only and not persisted in any way. In addition, it is only visible within the same process: multiple
 * instances of EventProcessorHost in the same process will share the same in-memory store and leases
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
 * ILeaseManager as an argument. After the EventProcessorHost instance is constructed, be sure to
 * call initialize() on this object before starting processing with EventProcessorHost.registerEventProcessor()
 * or EventProcessorHost.registerEventProcessorFactory().
 */
public class InMemoryLeaseManager implements ILeaseManager {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(InMemoryLeaseManager.class);
    private HostContext hostContext;
    private long millisecondsLatency = 0;

    public InMemoryLeaseManager() {
    }

    // This object is constructed before the EventProcessorHost and passed as an argument to
    // EventProcessorHost's constructor. So it has to get context info later.
    public void initialize(HostContext hostContext) {
        this.hostContext = hostContext;
    }

    public void setLatency(long milliseconds) {
        this.millisecondsLatency = milliseconds;
    }

    private void latency(String caller) {
        if (this.millisecondsLatency > 0) {
            try {
                //TRACE_LOGGER.info("sleep " + caller);
                Thread.sleep(this.millisecondsLatency);
            } catch (InterruptedException e) {
                // Don't care
                TRACE_LOGGER.info("sleepFAIL " + caller);
            }
        }
    }

    @Override
    public int getLeaseDurationInMilliseconds() {
        return this.hostContext.getPartitionManagerOptions().getLeaseDurationInSeconds() * 1000;
    }

    @Override
    public CompletableFuture<Boolean> leaseStoreExists() {
        boolean exists = InMemoryLeaseStore.SINGLETON.existsMap();
        latency("leaseStoreExists");
        TRACE_LOGGER.debug(this.hostContext.withHost("leaseStoreExists() " + exists));
        return CompletableFuture.completedFuture(exists);
    }

    @Override
    public CompletableFuture<Void> createLeaseStoreIfNotExists() {
        TRACE_LOGGER.debug(this.hostContext.withHost("createLeaseStoreIfNotExists()"));
        InMemoryLeaseStore.SINGLETON.initializeMap(getLeaseDurationInMilliseconds());
        latency("createLeaseStoreIfNotExists");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteLeaseStore() {
        TRACE_LOGGER.debug(this.hostContext.withHost("deleteLeaseStore()"));
        InMemoryLeaseStore.SINGLETON.deleteMap();
        latency("deleteLeaseStore");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<CompleteLease> getLease(String partitionId) {
        TRACE_LOGGER.debug(this.hostContext.withHost("getLease()"));
        latency("getLease");
        InMemoryLease leaseInStore = InMemoryLeaseStore.SINGLETON.getLease(partitionId);
        return CompletableFuture.completedFuture(new InMemoryLease(leaseInStore));
    }

    @Override
    public CompletableFuture<List<BaseLease>> getAllLeases() {
        ArrayList<BaseLease> infos = new ArrayList<BaseLease>();
        for (String id : InMemoryLeaseStore.SINGLETON.getPartitionIds()) {
            InMemoryLease leaseInStore = InMemoryLeaseStore.SINGLETON.getLease(id);
            infos.add(new BaseLease(id, leaseInStore.getOwner(), !leaseInStore.isExpiredSync()));
        }
        latency("getAllLeasesStateInfo");
        return CompletableFuture.completedFuture(infos);
    }

    @Override
    public CompletableFuture<Void> createAllLeasesIfNotExists(List<String> partitionIds) {
        ArrayList<CompletableFuture<BaseLease>> createFutures = new ArrayList<CompletableFuture<BaseLease>>();

        // Implemented like this to provide an experience more similar to lease creation in the Storage-based manager.
        for (String id : partitionIds) {
            final String workingId = id;
            CompletableFuture<BaseLease> oneCreate = CompletableFuture.supplyAsync(() -> {
                InMemoryLease leaseInStore = InMemoryLeaseStore.SINGLETON.getLease(workingId);
                InMemoryLease returnLease = null;
                if (leaseInStore != null) {
                    TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(workingId,
                            "createLeaseIfNotExists() found existing lease, OK"));
                    returnLease = new InMemoryLease(leaseInStore);
                } else {
                    TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(workingId,
                            "createLeaseIfNotExists() creating new lease"));
                    InMemoryLease newStoreLease = new InMemoryLease(workingId);
                    InMemoryLeaseStore.SINGLETON.setOrReplaceLease(newStoreLease);
                    returnLease = new InMemoryLease(newStoreLease);
                }
                latency("createLeaseIfNotExists " + workingId);
                return returnLease;
            }, this.hostContext.getExecutor());
            createFutures.add(oneCreate);
        }

        CompletableFuture<?>[] dummy = new CompletableFuture<?>[createFutures.size()];
        return CompletableFuture.allOf(createFutures.toArray(dummy));
    }

    @Override
    public CompletableFuture<Void> deleteLease(CompleteLease lease) {
        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(lease, "deleteLease()"));
        InMemoryLeaseStore.SINGLETON.removeLease((InMemoryLease) lease);
        latency("deleteLease " + lease.getPartitionId());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> acquireLease(CompleteLease lease) {
        InMemoryLease leaseToAcquire = (InMemoryLease) lease;

        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(leaseToAcquire, "acquireLease()"));

        boolean retval = true;
        InMemoryLease leaseInStore = InMemoryLeaseStore.SINGLETON.getLease(leaseToAcquire.getPartitionId());
        if (leaseInStore != null) {
            InMemoryLease wasUnowned = InMemoryLeaseStore.SINGLETON.atomicAquireUnowned(leaseToAcquire.getPartitionId(), this.hostContext.getHostName());
            if (wasUnowned != null) {
                // atomicAcquireUnowned already set ownership of the persisted lease, just update the live lease.
                leaseToAcquire.setOwner(this.hostContext.getHostName());
                TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(leaseToAcquire,
                        "acquireLease() acquired lease"));
                leaseInStore = wasUnowned;
                leaseToAcquire.setExpirationTime(leaseInStore.getExpirationTime());
            } else {
                if (leaseInStore.isOwnedBy(this.hostContext.getHostName())) {
                    TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(leaseToAcquire,
                            "acquireLease() already hold lease"));
                } else {
                    String oldOwner = leaseInStore.getOwner();
                    // Make change in both persisted lease and live lease!
                    InMemoryLeaseStore.SINGLETON.stealLease(leaseInStore, this.hostContext.getHostName());
                    leaseToAcquire.setOwner(this.hostContext.getHostName());
                    TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(leaseToAcquire,
                            "acquireLease() stole lease from " + oldOwner));
                }
                long newExpiration = System.currentTimeMillis() + getLeaseDurationInMilliseconds();
                // Make change in both persisted lease and live lease!
                leaseInStore.setExpirationTime(newExpiration);
                leaseToAcquire.setExpirationTime(newExpiration);
            }
        } else {
            TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(leaseToAcquire,
                    "acquireLease() can't find lease"));
            retval = false;
        }

        latency("acquireLease " + lease.getPartitionId());
        return CompletableFuture.completedFuture(retval);
    }

    // Real partition pumps get "notified" when another host has stolen their lease because the receiver throws
    // a ReceiverDisconnectedException. It doesn't matter how many hosts try to steal the lease at the same time,
    // only one will end up with it and that one will kick the others off via the exclusivity of epoch receivers.
    // This mechanism simulates that for dummy partition pumps used in testing. If expectedOwner does not currently
    // own the lease for the given partition, then notifier is called immediately, otherwise it is called whenever
    // ownership of the lease changes.
    public void notifyOnSteal(String expectedOwner, String partitionId, Callable<?> notifier) {
        InMemoryLeaseStore.SINGLETON.notifyOnSteal(expectedOwner, partitionId, notifier);
    }

    @Override
    public CompletableFuture<Boolean> renewLease(CompleteLease lease) {
        InMemoryLease leaseToRenew = (InMemoryLease) lease;

        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(leaseToRenew, "renewLease()"));

        boolean retval = true;
        InMemoryLease leaseInStore = InMemoryLeaseStore.SINGLETON.getLease(leaseToRenew.getPartitionId());
        if (leaseInStore != null) {
            // MATCH BEHAVIOR OF AzureStorageCheckpointLeaseManager:
            // Renewing a lease that has expired succeeds unless some other host has grabbed it already.
            // So don't check expiration, just ownership.
            if (leaseInStore.isOwnedBy(this.hostContext.getHostName())) {
                long newExpiration = System.currentTimeMillis() + getLeaseDurationInMilliseconds();
                // Make change in both persisted lease and live lease!
                leaseInStore.setExpirationTime(newExpiration);
                leaseToRenew.setExpirationTime(newExpiration);
            } else {
                TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(leaseToRenew,
                        "renewLease() not renewed because we don't own lease"));
                retval = false;
            }
        } else {
            TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(leaseToRenew,
                    "renewLease() can't find lease"));
            retval = false;
        }

        latency("renewLease " + lease.getPartitionId());
        return CompletableFuture.completedFuture(retval);
    }

    @Override
    public CompletableFuture<Void> releaseLease(CompleteLease lease) {
        InMemoryLease leaseToRelease = (InMemoryLease) lease;

        CompletableFuture<Void> retval = CompletableFuture.completedFuture(null);

        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(leaseToRelease, "releaseLease()"));

        InMemoryLease leaseInStore = InMemoryLeaseStore.SINGLETON.getLease(leaseToRelease.getPartitionId());
        if (leaseInStore != null) {
            if (!leaseInStore.isExpiredSync() && leaseInStore.isOwnedBy(this.hostContext.getHostName())) {
                TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(leaseToRelease, "releaseLease() released OK"));
                // Make change in both persisted lease and live lease!
                leaseInStore.setOwner("");
                leaseToRelease.setOwner("");
                leaseInStore.setExpirationTime(0);
                leaseToRelease.setExpirationTime(0);
            }
        } else {
            TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(leaseToRelease, "releaseLease() can't find lease in store"));
            retval = new CompletableFuture<Void>();
            retval.completeExceptionally(new CompletionException(new RuntimeException("releaseLease can't find lease in store for " + leaseToRelease.getPartitionId())));
        }
        latency("releaseLease " + lease.getPartitionId());
        return retval;
    }

    @Override
    public CompletableFuture<Boolean> updateLease(CompleteLease lease) {
        InMemoryLease leaseToUpdate = (InMemoryLease) lease;

        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(leaseToUpdate, "updateLease()"));

        // Renew lease first so it doesn't expire in the middle.
        return renewLease(leaseToUpdate).thenApply((retval) -> {
            if (retval) {
                InMemoryLease leaseInStore = InMemoryLeaseStore.SINGLETON.getLease(leaseToUpdate.getPartitionId());
                if (leaseInStore != null) {
                    if (!leaseInStore.isExpiredSync() && leaseInStore.isOwnedBy(this.hostContext.getHostName())) {
                        // We are updating with values already in the live lease, so only need to set on the persisted lease.
                        leaseInStore.setEpoch(leaseToUpdate.getEpoch());
                        // Don't copy expiration time, that is managed directly by Acquire/Renew/Release
                    } else {
                        TRACE_LOGGER.debug(this.hostContext.withHostAndPartition(leaseToUpdate,
                                "updateLease() not updated because we don't own lease"));
                        retval = false;
                    }
                } else {
                    TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(leaseToUpdate,
                            "updateLease() can't find lease"));
                    retval = false;
                }
            }
            latency("updateLease " + lease.getPartitionId());
            return retval;
        });
    }


    private static class InMemoryLeaseStore {
        static final InMemoryLeaseStore SINGLETON = new InMemoryLeaseStore();
        private volatile int leaseDurationInMilliseconds;

        private ConcurrentHashMap<String, InMemoryLease> inMemoryLeasesPrivate = null;
        private ConcurrentHashMap<String, Callable<?>> notifiers = new ConcurrentHashMap<String, Callable<?>>();

        synchronized boolean existsMap() {
            return (this.inMemoryLeasesPrivate != null);
        }

        synchronized void initializeMap(int leaseDurationInMilliseconds) {
            if (this.inMemoryLeasesPrivate == null) {
                this.inMemoryLeasesPrivate = new ConcurrentHashMap<String, InMemoryLease>();
            }
            this.leaseDurationInMilliseconds = leaseDurationInMilliseconds;
        }

        synchronized void deleteMap() {
            this.inMemoryLeasesPrivate = null;
        }

        synchronized InMemoryLease getLease(String partitionId) {
            return this.inMemoryLeasesPrivate.get(partitionId);
        }

        synchronized List<String> getPartitionIds() {
            ArrayList<String> ids = new ArrayList<String>();
            this.inMemoryLeasesPrivate.keySet().forEach((key) -> {
                ids.add(key);
            });
            return ids;
        }

        synchronized InMemoryLease atomicAquireUnowned(String partitionId, String newOwner) {
            InMemoryLease leaseInStore = getLease(partitionId);
            if (leaseInStore.isExpiredSync() || (leaseInStore.getOwner() == null) || leaseInStore.getOwner().isEmpty()) {
                leaseInStore.setOwner(newOwner);
                leaseInStore.setExpirationTime(System.currentTimeMillis() + this.leaseDurationInMilliseconds);
            } else {
                // Return null if it was already owned
                leaseInStore = null;
            }
            return leaseInStore;
        }

        synchronized void notifyOnSteal(String expectedOwner, String partitionId, Callable<?> notifier) {
            InMemoryLease leaseInStore = getLease(partitionId);
            if (!leaseInStore.isOwnedBy(expectedOwner)) {
                // Already stolen.
                try {
                    notifier.call();
                } catch (Exception e) {
                    TRACE_LOGGER.warn("notifier call failed: " + e.getMessage());
                }
            } else {
                this.notifiers.put(partitionId, notifier);
            }
        }

        synchronized void stealLease(InMemoryLease stealee, String newOwner) {
            stealee.setOwner(newOwner);
            Callable<?> notifier = this.notifiers.get(stealee.getPartitionId());
            if (notifier != null) {
                try {
                    notifier.call();
                } catch (Exception e) {
                    TRACE_LOGGER.warn("notifier call failed: " + e.getMessage());
                }
            }
        }

        synchronized void setOrReplaceLease(InMemoryLease newLease) {
            this.inMemoryLeasesPrivate.put(newLease.getPartitionId(), newLease);
        }

        synchronized void removeLease(InMemoryLease goneLease) {
            this.inMemoryLeasesPrivate.remove(goneLease.getPartitionId());
        }
    }


    private static class InMemoryLease extends CompleteLease {
        private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(InMemoryLease.class);
        private long expirationTimeMillis = 0;

        InMemoryLease(String partitionId) {
            super(partitionId);
            this.epoch = 0;
        }

        InMemoryLease(InMemoryLease source) {
            super(source);
            this.expirationTimeMillis = source.expirationTimeMillis;
            this.epoch = source.epoch;
        }

        long getExpirationTime() {
            return this.expirationTimeMillis;
        }

        void setExpirationTime(long expireAtMillis) {
            this.expirationTimeMillis = expireAtMillis;
        }

        public boolean isExpiredSync() {
            boolean hasExpired = (System.currentTimeMillis() >= this.expirationTimeMillis);
            TRACE_LOGGER.debug("isExpired(" + this.getPartitionId() + (hasExpired ? ") expired " : ") leased ") + (this.expirationTimeMillis - System.currentTimeMillis()));
            return hasExpired;
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
}
