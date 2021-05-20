// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventprocessorhost.BaseLease;
import com.microsoft.azure.eventprocessorhost.CompleteLease;
import com.microsoft.azure.eventprocessorhost.ILeaseManager;
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
    private final String hostname;

    public InMemoryLeaseManager(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public int getLeaseDurationInMilliseconds() {
        return 60 * 1000;
    }

    @Override
    public CompletableFuture<Boolean> leaseStoreExists() {
        boolean exists = InMemoryLeaseStore.SINGLETON.existsMap();
        return CompletableFuture.completedFuture(exists);
    }

    @Override
    public CompletableFuture<Void> createLeaseStoreIfNotExists() {
        InMemoryLeaseStore.SINGLETON.initializeMap(getLeaseDurationInMilliseconds());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteLeaseStore() {
        InMemoryLeaseStore.SINGLETON.deleteMap();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<CompleteLease> getLease(String partitionId) {
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
        return CompletableFuture.completedFuture(infos);
    }

    @Override
    public CompletableFuture<Void> createAllLeasesIfNotExists(List<String> partitionIds) {
        final ArrayList<CompletableFuture<BaseLease>> createFutures = new ArrayList<>();

        // Implemented like this to provide an experience more similar to lease creation in the Storage-based manager.
        for (String id : partitionIds) {
            final String workingId = id;
            CompletableFuture<BaseLease> oneCreate = CompletableFuture.supplyAsync(() -> {
                InMemoryLease leaseInStore = InMemoryLeaseStore.SINGLETON.getLease(workingId);
                InMemoryLease returnLease = null;
                if (leaseInStore != null) {
                    returnLease = new InMemoryLease(leaseInStore);
                } else {
                    InMemoryLease newStoreLease = new InMemoryLease(workingId);
                    InMemoryLeaseStore.SINGLETON.setOrReplaceLease(newStoreLease);
                    returnLease = new InMemoryLease(newStoreLease);
                }
                return returnLease;
            });
            createFutures.add(oneCreate);
        }

        CompletableFuture<?>[] dummy = new CompletableFuture<?>[createFutures.size()];
        return CompletableFuture.allOf(createFutures.toArray(dummy));
    }

    @Override
    public CompletableFuture<Void> deleteLease(CompleteLease lease) {
        InMemoryLeaseStore.SINGLETON.removeLease((InMemoryLease) lease);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> acquireLease(CompleteLease lease) {
        InMemoryLease leaseToAcquire = (InMemoryLease) lease;

        boolean retval = true;
        InMemoryLease leaseInStore = InMemoryLeaseStore.SINGLETON.getLease(leaseToAcquire.getPartitionId());
        if (leaseInStore != null) {
            InMemoryLease wasUnowned = InMemoryLeaseStore.SINGLETON.atomicAquireUnowned(leaseToAcquire.getPartitionId(), hostname);
            if (wasUnowned != null) {
                // atomicAcquireUnowned already set ownership of the persisted lease, just update the live lease.
                leaseToAcquire.setOwner(hostname);
                leaseInStore = wasUnowned;
                leaseToAcquire.setExpirationTime(leaseInStore.getExpirationTime());
            } else {
                if (leaseInStore.isOwnedBy(hostname)) {
                    TRACE_LOGGER.debug("acquireLease() already hold lease");
                } else {
                    String oldOwner = leaseInStore.getOwner();
                    // Make change in both persisted lease and live lease!
                    InMemoryLeaseStore.SINGLETON.stealLease(leaseInStore, hostname);
                    leaseToAcquire.setOwner(hostname);
                }
                long newExpiration = System.currentTimeMillis() + getLeaseDurationInMilliseconds();
                // Make change in both persisted lease and live lease!
                leaseInStore.setExpirationTime(newExpiration);
                leaseToAcquire.setExpirationTime(newExpiration);
            }
        } else {
            TRACE_LOGGER.warn("acquireLease() can't find lease");
            retval = false;
        }

        return CompletableFuture.completedFuture(retval);
    }

    @Override
    public CompletableFuture<Boolean> renewLease(CompleteLease lease) {
        InMemoryLease leaseToRenew = (InMemoryLease) lease;

        boolean retval = true;
        InMemoryLease leaseInStore = InMemoryLeaseStore.SINGLETON.getLease(leaseToRenew.getPartitionId());
        if (leaseInStore != null) {
            // MATCH BEHAVIOR OF AzureStorageCheckpointLeaseManager:
            // Renewing a lease that has expired succeeds unless some other host has grabbed it already.
            // So don't check expiration, just ownership.
            if (leaseInStore.isOwnedBy(hostname)) {
                long newExpiration = System.currentTimeMillis() + getLeaseDurationInMilliseconds();
                // Make change in both persisted lease and live lease!
                leaseInStore.setExpirationTime(newExpiration);
                leaseToRenew.setExpirationTime(newExpiration);
            } else {
                TRACE_LOGGER.debug(
                    "renewLease() not renewed because we don't own lease");
                retval = false;
            }
        } else {
            TRACE_LOGGER.warn("renewLease() can't find lease");
            retval = false;
        }

        return CompletableFuture.completedFuture(retval);
    }

    @Override
    public CompletableFuture<Void> releaseLease(CompleteLease lease) {
        InMemoryLease leaseToRelease = (InMemoryLease) lease;

        CompletableFuture<Void> retval = CompletableFuture.completedFuture(null);

        TRACE_LOGGER.debug("releaseLease()");

        InMemoryLease leaseInStore = InMemoryLeaseStore.SINGLETON.getLease(leaseToRelease.getPartitionId());
        if (leaseInStore != null) {
            if (!leaseInStore.isExpiredSync() && leaseInStore.isOwnedBy(hostname)) {
                TRACE_LOGGER.debug("releaseLease() released OK");
                // Make change in both persisted lease and live lease!
                leaseInStore.setOwner("");
                leaseToRelease.setOwner("");
                leaseInStore.setExpirationTime(0);
                leaseToRelease.setExpirationTime(0);
            }
        } else {
            TRACE_LOGGER.warn("releaseLease() can't find lease in store");
            retval = new CompletableFuture<>();
            retval.completeExceptionally(new CompletionException(new RuntimeException("releaseLease can't find lease in store for " + leaseToRelease.getPartitionId())));
        }
        return retval;
    }

    @Override
    public CompletableFuture<Boolean> updateLease(CompleteLease lease) {
        InMemoryLease leaseToUpdate = (InMemoryLease) lease;

        TRACE_LOGGER.debug("updateLease()");

        // Renew lease first so it doesn't expire in the middle.
        return renewLease(leaseToUpdate).thenApply((retval) -> {
            if (retval) {
                InMemoryLease leaseInStore = InMemoryLeaseStore.SINGLETON.getLease(leaseToUpdate.getPartitionId());
                if (leaseInStore != null) {
                    if (!leaseInStore.isExpiredSync() && leaseInStore.isOwnedBy(hostname)) {
                        // We are updating with values already in the live lease, so only need to set on the persisted lease.
                        leaseInStore.setEpoch(leaseToUpdate.getEpoch());
                        // Don't copy expiration time, that is managed directly by Acquire/Renew/Release
                    } else {
                        TRACE_LOGGER.debug("updateLease() not updated because we don't own lease");
                        retval = false;
                    }
                } else {
                    TRACE_LOGGER.warn("updateLease() can't find lease");
                    retval = false;
                }
            }
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
