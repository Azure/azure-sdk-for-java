package com.azure.messaging.eventhubs.perf;

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
    private final ConcurrentHashMap<String, OwnershipInformation> partitionOwnershipMap = new ConcurrentHashMap<>();

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
            partitionOwnershipMap.keySet().forEach(key -> {
                partitionOwnershipMap.computeIfPresent(key, (existing, value) -> {
                    value.setCheckpoint(new Checkpoint(key));
                    return value;
                });
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
        return CompletableFuture.allOf(partitionIds.stream()
            .map(id -> CompletableFuture.completedFuture(
                partitionOwnershipMap.computeIfAbsent(id,
                    key -> new OwnershipInformation().setCheckpoint(new Checkpoint(id)))))
            .toArray(CompletableFuture<?>[]::new));
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
            partitionOwnershipMap.computeIfPresent(partitionId,
                (key, existing) -> existing.setCheckpoint(new Checkpoint(partitionId)));
        });
    }

    private static class OwnershipInformation {
        private CompleteLease lease;
        private Checkpoint checkpoint;

        public CompleteLease getLease() {
            synchronized (this) {
                return lease;
            }
        }

        public OwnershipInformation setLease(CompleteLease lease) {
            synchronized (this) {
                this.lease = lease;
            }
            return this;
        }

        public Checkpoint getCheckpoint() {
            synchronized (this) {
                return checkpoint;
            }
        }

        public OwnershipInformation setCheckpoint(Checkpoint checkpoint) {
            synchronized (this) {
                this.checkpoint = checkpoint;
            }

            return this;
        }
    }
}
