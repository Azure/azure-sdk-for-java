// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.time.Duration;
import java.time.OffsetDateTime;

public class ContainerLock {

    private static CosmosEntityInformation<LockEntry, String> lockEntityInfo;

    private LockStore lockStore;
    private Duration leaseDuration;
    private String lockName;
    private LockEntry acquiredLock;

    public ContainerLock(CosmosTemplate template, String lockName, Duration leaseDuration) {
        this.lockStore = new NonReactiveLockStore(template);
        this.lockName = lockName;
        this.leaseDuration = leaseDuration;
        initLockContainer(lockStore, template);
    }

    public ContainerLock(ReactiveCosmosTemplate reactiveTemplate, String lockName, Duration leaseDuration) {
        this.lockStore = new ReactiveLockStore(reactiveTemplate);
        this.lockName = lockName;
        this.leaseDuration = leaseDuration;
        initLockContainer(lockStore, reactiveTemplate);
    }

    private static synchronized void initLockContainer(LockStore lockStore, Object template) {
        if (lockEntityInfo == null) {
            CosmosEntityInformation<LockEntry, String> info = new CosmosEntityInformation<>(LockEntry.class);
            lockStore.createContainerIfNotExists(info);
            AbstractIntegrationTestCollectionManager.registerContainerForCleanup(template, info.getContainerName());
            lockEntityInfo = info;
        }
    }

    public void acquire(Duration tryForDuration) {
        long started = System.currentTimeMillis();
        LockEntry entry = new LockEntry(lockName, OffsetDateTime.now().plus(leaseDuration));
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
        LockEntry entry = lockStore.findActiveLock(lockName);
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
        LockEntry findActiveLock(String id);
        LockEntry refreshLock(LockEntry entry);
        void deleteLock(LockEntry entry);
        void createContainerIfNotExists(CosmosEntityInformation entityInfo);
    }

    private static class NonReactiveLockStore implements LockStore {

        private final CosmosTemplate template;

        public NonReactiveLockStore(CosmosTemplate template) {
            this.template = template;
        }

        @Override
        public LockEntry insertLock(LockEntry entry) {
            return template.insert(lockEntityInfo.getContainerName(), entry);
        }

        @Override
        public LockEntry findActiveLock(String id) {
            return template.findById(lockEntityInfo.getContainerName(), id, LockEntry.class);
        }

        @Override
        public LockEntry refreshLock(LockEntry entry) {
            return template.upsertAndReturnEntity(lockEntityInfo.getContainerName(), entry);
        }

        @Override
        public void deleteLock(LockEntry entry) {
            template.deleteEntity(lockEntityInfo.getContainerName(), entry);
        }

        @Override
        public void createContainerIfNotExists(CosmosEntityInformation entityInfo) {
            template.createContainerIfNotExists(entityInfo);
        }
    }

    private static class ReactiveLockStore implements LockStore {

        private final ReactiveCosmosTemplate template;

        public ReactiveLockStore(ReactiveCosmosTemplate template) {
            this.template = template;
        }

        @Override
        public LockEntry insertLock(LockEntry entry) {
            return template.insert(lockEntityInfo.getContainerName(), entry).block();
        }

        @Override
        public LockEntry findActiveLock(String id) {
            return template.findById(lockEntityInfo.getContainerName(), id, LockEntry.class).block();
        }

        @Override
        public LockEntry refreshLock(LockEntry entry) {
            return template.upsert(lockEntityInfo.getContainerName(), entry).block();
        }

        @Override
        public void deleteLock(LockEntry entry) {
            template.deleteEntity(lockEntityInfo.getContainerName(), entry).block();
        }

        @Override
        public void createContainerIfNotExists(CosmosEntityInformation entityInfo) {
            template.createContainerIfNotExists(entityInfo).block();
        }
    }

    static class LockEntry {
        @Id
        @PartitionKey
        public String id;
        @Version
        public String version;
        public OffsetDateTime leaseExpiration;

        public LockEntry() {
        }

        public LockEntry(String id, OffsetDateTime leaseExpiration) {
            this.id = id;
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
