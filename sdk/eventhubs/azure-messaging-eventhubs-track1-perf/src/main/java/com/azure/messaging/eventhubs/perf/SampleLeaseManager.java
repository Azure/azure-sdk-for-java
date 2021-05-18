// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.perf.models.OwnershipInformation;
import com.microsoft.azure.eventprocessorhost.BaseLease;
import com.microsoft.azure.eventprocessorhost.CompleteLease;
import com.microsoft.azure.eventprocessorhost.ILeaseManager;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SampleLeaseManager implements ILeaseManager {
    private static final int LEASE_DURATION = Long.valueOf(Duration.ofMinutes(10).toMillis()).intValue();

    private final ConcurrentHashMap<String, OwnershipInformation> partitionOwnershipMap;

    public SampleLeaseManager(ConcurrentHashMap<String, OwnershipInformation> partitionOwnershipMap) {
        this.partitionOwnershipMap = partitionOwnershipMap;
    }

    @Override
    public int getLeaseDurationInMilliseconds() {
        return LEASE_DURATION;
    }

    @Override
    public CompletableFuture<Boolean> leaseStoreExists() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Void> createLeaseStoreIfNotExists() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteLeaseStore() {
        return CompletableFuture.runAsync(() -> {
            partitionOwnershipMap.forEach((key, value) -> {
                if (value.getLease() != null) {
                    value.setLease(null);
                }
            });
        });
    }

    @Override
    public CompletableFuture<CompleteLease> getLease(String partitionId) {
        return CompletableFuture.supplyAsync(() -> {
            final OwnershipInformation ownershipInformation = partitionOwnershipMap.get(partitionId);
            return ownershipInformation != null ? ownershipInformation.getLease() : null;
        });
    }

    @Override
    public CompletableFuture<List<BaseLease>> getAllLeases() {
        return null;
    }

    @Override
    public CompletableFuture<Void> createAllLeasesIfNotExists(List<String> partitionIds) {
        return CompletableFuture.allOf(partitionIds.stream()
            .map(id -> CompletableFuture.completedFuture(
                partitionOwnershipMap.computeIfAbsent(id,
                    key -> new OwnershipInformation().setLease(new CompleteLease(id)))))
            .toArray(CompletableFuture<?>[]::new));
    }

    @Override
    public CompletableFuture<Void> deleteLease(CompleteLease lease) {
        partitionOwnershipMap.compute(lease.getPartitionId(), (key, existing) -> {
            if (existing != null && existing.getLease() != null
                && existing.getLease().getEpoch() == lease.getEpoch()) {

                existing.setLease(null);
            }

            return existing;
        });

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> acquireLease(CompleteLease lease) {
        final OwnershipInformation result = partitionOwnershipMap.compute(lease.getPartitionId(), (key, existing) -> {
            if (existing == null) {
                existing = new OwnershipInformation().setLease(lease);
                return existing;
            }

            final CompleteLease existingLease = existing.getLease();
            if (existingLease == null) {
                existing.setLease(lease);
            } else if (existingLease.getEpoch() < lease.getEpoch()) {
                existing.setLease(lease);
            }

            return existing;
        });

        return CompletableFuture.completedFuture(result.getLease() == lease);
    }

    @Override
    public CompletableFuture<Boolean> renewLease(CompleteLease lease) {
        final OwnershipInformation result = partitionOwnershipMap.compute(lease.getPartitionId(), (key, existing) -> {
            if (existing == null) {
                existing = new OwnershipInformation().setLease(lease);
                return existing;
            }

            final CompleteLease existingLease = existing.getLease();
            if (existingLease == null) {
                existing.setLease(lease);
            } else if (existingLease.getEpoch() == lease.getEpoch()
                && lease.getOwner().equals(existingLease.getOwner())) {
                existing.setLease(lease);
            }

            return existing;
        });

        return CompletableFuture.completedFuture(result.getLease() == lease);
    }

    @Override
    public CompletableFuture<Void> releaseLease(CompleteLease lease) {
        partitionOwnershipMap.compute(lease.getPartitionId(), (key, existing) -> {
            if (existing == null) {
                existing = new OwnershipInformation().setLease(lease);
                return existing;
            }

            final CompleteLease existingLease = existing.getLease();
            if (existingLease == null) {
                existing.setLease(lease);
            } else if (lease.getPartitionId().equals(existingLease.getPartitionId())) {
                existing.setLease(lease);
            }

            return existing;
        });

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> updateLease(CompleteLease lease) {
        final OwnershipInformation result = partitionOwnershipMap.compute(lease.getPartitionId(), (key, existing) -> {
            if (existing == null) {
                existing = new OwnershipInformation().setLease(lease);
                return existing;
            }

            final CompleteLease existingLease = existing.getLease();
            if (existingLease == null) {
                existing.setLease(lease);
            } else if (lease.getPartitionId().equals(existingLease.getPartitionId())) {
                existing.setLease(lease);
            }

            return existing;
        });

        return CompletableFuture.completedFuture(result.getLease() == lease);
    }
}
