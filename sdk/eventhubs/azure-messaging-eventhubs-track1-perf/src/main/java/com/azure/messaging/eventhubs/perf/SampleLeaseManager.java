// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventprocessorhost.BaseLease;
import com.microsoft.azure.eventprocessorhost.CompleteLease;
import com.microsoft.azure.eventprocessorhost.ILeaseManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SampleLeaseManager implements ILeaseManager {

    @Override
    public int getLeaseDurationInMilliseconds() {
        return 0;
    }

    @Override
    public CompletableFuture<Boolean> leaseStoreExists() {
        return null;
    }

    @Override
    public CompletableFuture<Void> createLeaseStoreIfNotExists() {
        return null;
    }

    @Override
    public CompletableFuture<Void> deleteLeaseStore() {
        return null;
    }

    @Override
    public CompletableFuture<CompleteLease> getLease(String partitionId) {
        return null;
    }

    @Override
    public CompletableFuture<List<BaseLease>> getAllLeases() {
        return null;
    }

    @Override
    public CompletableFuture<Void> createAllLeasesIfNotExists(List<String> partitionIds) {
        return null;
    }

    @Override
    public CompletableFuture<Void> deleteLease(CompleteLease lease) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> acquireLease(CompleteLease lease) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> renewLease(CompleteLease lease) {
        return null;
    }

    @Override
    public CompletableFuture<Void> releaseLease(CompleteLease lease) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> updateLease(CompleteLease lease) {
        return null;
    }
}
