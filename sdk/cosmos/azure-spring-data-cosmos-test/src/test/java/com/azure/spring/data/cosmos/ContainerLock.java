// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.time.Duration;
import java.time.OffsetDateTime;

public class ContainerLock {

    private LockStore lockStore;
    private Duration leaseDuration;
    private LockEntry acquiredLock;

    public ContainerLock(CosmosTemplate template, String containerName, Duration leaseDuration) {
        this.lockStore = new NonReactiveLockStore(template, containerName);
        this.leaseDuration = leaseDuration;
    }

    public ContainerLock(ReactiveCosmosTemplate reactiveTemplate, String containerName, Duration leaseDuration) {
        this.lockStore = new ReactiveLockStore(reactiveTemplate, containerName);
        this.leaseDuration = leaseDuration;
    }

    public void acquire(Duration tryForDuration) {
        long started = System.currentTimeMillis();
        LockEntry entry = new LockEntry(OffsetDateTime.now().plus(leaseDuration));
        while (acquiredLock == null) {
            try {
                acquiredLock = lockStore.insertLock(entry);
            } catch (Exception ex) {
                if (shouldKeepTryingToAcquire(started, tryForDuration)) {
                    sleep(500);
                    releaseIfLeaseExpired();
                } else {
                    throw new LockAcquisitionFailedException(tryForDuration);
                }
            }
        }
    }

    private boolean shouldKeepTryingToAcquire(long started, Duration tryForDuration) {
        long elapsedDuration = System.currentTimeMillis() - started;
        return elapsedDuration <= tryForDuration.toMillis();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignored
        }
    }

    private void releaseIfLeaseExpired() {
        LockEntry entry = lockStore.findActiveLock();
        if (entry != null && entry.isLeaseExpired()) {
            acquiredLock = entry;
            release();
        }
    }

    public void release() {
        if (acquiredLock != null) {
            lockStore.deleteLock(acquiredLock);
            acquiredLock = null;
        }
    }

    public void renew() {
        if (acquiredLock != null) {
            acquiredLock.leaseExpiration = OffsetDateTime.now().plus(leaseDuration);
            acquiredLock = lockStore.refreshLock(acquiredLock);
        }
    }

    public OffsetDateTime getLeaseExpiration() {
        if (acquiredLock == null) {
            return null;
        }
        return acquiredLock.leaseExpiration;
    }

    private interface LockStore {
        LockEntry insertLock(LockEntry entry);
        LockEntry findActiveLock();
        LockEntry refreshLock(LockEntry entry);
        void deleteLock(LockEntry entry);
    }

    private static class NonReactiveLockStore implements LockStore {

        private final CosmosTemplate template;
        private final String containerName;

        public NonReactiveLockStore(CosmosTemplate template, String containerName) {
            this.template = template;
            this.containerName = containerName;
        }

        @Override
        public LockEntry insertLock(LockEntry entry) {
            return template.insert(containerName, entry);
        }

        @Override
        public LockEntry findActiveLock() {
            return template.findById(containerName, LockEntry.ID, LockEntry.class);
        }

        @Override
        public LockEntry refreshLock(LockEntry entry) {
            return template.upsertAndReturnEntity(containerName, entry);
        }

        @Override
        public void deleteLock(LockEntry entry) {
            template.deleteEntity(containerName, entry);
        }
    }

    private static class ReactiveLockStore implements LockStore {

        private final ReactiveCosmosTemplate template;
        private final String containerName;

        public ReactiveLockStore(ReactiveCosmosTemplate template, String containerName) {
            this.template = template;
            this.containerName = containerName;
        }

        @Override
        public LockEntry insertLock(LockEntry entry) {
            return template.insert(containerName, entry).block();
        }

        @Override
        public LockEntry findActiveLock() {
            return template.findById(containerName, LockEntry.ID, LockEntry.class).block();
        }

        @Override
        public LockEntry refreshLock(LockEntry entry) {
            return template.upsert(containerName, entry).block();
        }

        @Override
        public void deleteLock(LockEntry entry) {
            template.deleteEntity(containerName, entry).block();
        }
    }

    static class LockEntry {
        static final String ID = "lock";

        @Id
        public String id = ID;
        @Version
        public String version;
        public OffsetDateTime leaseExpiration;

        public LockEntry() {
        }

        public LockEntry(OffsetDateTime leaseExpiration) {
            this.leaseExpiration = leaseExpiration;
        }

        @JsonIgnore
        public boolean isLeaseExpired() {
            return OffsetDateTime.now().isAfter(leaseExpiration);
        }

    }

    static class LockAcquisitionFailedException extends RuntimeException {
        public LockAcquisitionFailedException(Duration tryForDuration) {
            super("Failed to acquire lock within " + tryForDuration);
        }
    }

}
