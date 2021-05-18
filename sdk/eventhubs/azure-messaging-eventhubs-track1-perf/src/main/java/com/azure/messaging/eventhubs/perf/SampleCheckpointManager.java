// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.perf.models.OwnershipInformation;
import com.microsoft.azure.eventprocessorhost.Checkpoint;
import com.microsoft.azure.eventprocessorhost.CompleteLease;
import com.microsoft.azure.eventprocessorhost.ICheckpointManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SampleCheckpointManager implements ICheckpointManager {
    /**
     * Ownership map. Key: Partition Value: Ownership.
     */
    private final ConcurrentHashMap<String, OwnershipInformation> partitionOwnershipMap;

    public SampleCheckpointManager(ConcurrentHashMap<String, OwnershipInformation> partitionOwnershipMap) {
        this.partitionOwnershipMap = partitionOwnershipMap;
    }

    @Override
    public CompletableFuture<Boolean> checkpointStoreExists() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Void> createCheckpointStoreIfNotExists() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteCheckpointStore() {
        return CompletableFuture.runAsync(() -> {
            partitionOwnershipMap.forEach((key, value) -> {
                value.setCheckpoint(null);
            });
        });
    }

    @Override
    public CompletableFuture<Checkpoint> getCheckpoint(String partitionId) {
        return CompletableFuture.supplyAsync(() -> {
            final OwnershipInformation ownershipInformation = partitionOwnershipMap.get(partitionId);
            return ownershipInformation != null ? ownershipInformation.getCheckpoint() : null;
        });
    }

    @Override
    public CompletableFuture<Void> createAllCheckpointsIfNotExists(List<String> partitionIds) {
        return CompletableFuture.runAsync(() -> {
            for (String partitionId : partitionIds) {
                partitionOwnershipMap.compute(partitionId, (key, existing) -> {
                    if (existing == null) {
                        existing = new OwnershipInformation().setCheckpoint(new Checkpoint(key));
                    } else if (existing.getCheckpoint() == null) {
                        existing.setCheckpoint(new Checkpoint(key));
                    }

                    return existing;
                });
            }
        });
    }

    @Override
    public CompletableFuture<Void> updateCheckpoint(CompleteLease lease, Checkpoint checkpoint) {
        final String partitionId = checkpoint.getPartitionId();
        return CompletableFuture.runAsync(() -> {
            partitionOwnershipMap.compute(partitionId, (key, existing) -> {
                // No one currently owns this.
                if (existing == null || existing.getLease() == null) {
                    return new OwnershipInformation().setCheckpoint(checkpoint).setLease(lease);
                }

                final CompleteLease existingLease = existing.getLease();
                if (existingLease.getOwner().equals(lease.getOwner())
                    && existingLease.getEpoch() == lease.getEpoch()) {
                    return new OwnershipInformation().setCheckpoint(checkpoint).setLease(lease);
                } else {
                    System.out.printf("Lease is invalid. Owned by someone else. Existing: %s[%d] Current: %s[%d]%n",
                        existingLease.getOwner(), existingLease.getEpoch(), lease.getOwner(), lease.getEpoch());
                    return existing;
                }
            });
        });
    }

    @Override
    public CompletableFuture<Void> deleteCheckpoint(String partitionId) {
        return CompletableFuture.runAsync(() -> {
            partitionOwnershipMap.computeIfPresent(partitionId, (key, existing) -> existing.setCheckpoint(null));
        });
    }
}
