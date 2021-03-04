// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.cosmos.implementation.guava25.base.Stopwatch;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.time.Duration;
import java.time.OffsetDateTime;

public class ContainerLock {

    private CosmosTemplate template;
    private String containerName;
    private Duration leaseDuration;
    private LockEntry acquiredLock;

    public ContainerLock(CosmosTemplate template, String containerName, Duration leaseDuration) {
        this.template = template;
        this.containerName = containerName;
        this.leaseDuration = leaseDuration;
    }

    public void acquire(Duration tryForDuration) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LockEntry entry = new LockEntry(OffsetDateTime.now().plus(leaseDuration));
        while (acquiredLock == null) {
            try {
                acquiredLock = template.insert(containerName, entry);
            } catch (Exception ex) {
                if (!tryForDuration.minus(stopwatch.elapsed()).isNegative()) {
                    sleep(500);
                    releaseIfLeaseExpired();
                } else {
                    throw new LockAcquisitionFailedException(tryForDuration);
                }
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignored
        }
    }

    private void releaseIfLeaseExpired() {
        LockEntry entry = template.findById(containerName, new LockEntry().id, LockEntry.class);
        if (entry != null && entry.isLeaseExpired()) {
            acquiredLock = entry;
            release();
        }
    }

    public void release() {
        if (acquiredLock != null) {
            template.deleteEntity(containerName, acquiredLock);
            acquiredLock = null;
        }
    }

    public void renew() {
        if (acquiredLock != null) {
            acquiredLock.leaseExpiration = OffsetDateTime.now().plus(leaseDuration);
            acquiredLock = template.upsertAndReturnEntity(containerName, acquiredLock);
        }
    }

    public OffsetDateTime getLeaseExpiration() {
        if (acquiredLock == null) {
            return null;
        }
        return acquiredLock.leaseExpiration;
    }

    static class LockEntry {
        @Id
        public String id = "lock";
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
