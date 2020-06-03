// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.compute.implementation;

import com.azure.management.compute.ComputeManager;
import com.azure.management.compute.models.AccessLevel;
import com.azure.management.compute.models.GrantAccessData;
import com.azure.management.compute.models.Snapshot;
import com.azure.management.compute.models.Snapshots;
import com.azure.management.compute.fluent.inner.SnapshotInner;
import com.azure.management.compute.fluent.SnapshotsClient;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import reactor.core.publisher.Mono;

/** The implementation for Snapshots. */
public class SnapshotsImpl
    extends TopLevelModifiableResourcesImpl<Snapshot, SnapshotImpl, SnapshotInner, SnapshotsClient, ComputeManager>
    implements Snapshots {

    public SnapshotsImpl(ComputeManager computeManager) {
        super(computeManager.inner().getSnapshots(), computeManager);
    }

    @Override
    public Mono<String> grantAccessAsync(
        String resourceGroupName, String snapshotName, AccessLevel accessLevel, int accessDuration) {
        GrantAccessData grantAccessDataInner = new GrantAccessData();
        grantAccessDataInner.withAccess(accessLevel).withDurationInSeconds(accessDuration);
        return inner()
            .grantAccessAsync(resourceGroupName, snapshotName, grantAccessDataInner)
            .onErrorResume(e -> Mono.empty())
            .map(accessUriInner -> accessUriInner.accessSas());
    }

    @Override
    public String grantAccess(
        String resourceGroupName, String snapshotName, AccessLevel accessLevel, int accessDuration) {
        return this.grantAccessAsync(resourceGroupName, snapshotName, accessLevel, accessDuration).block();
    }

    @Override
    public Mono<Void> revokeAccessAsync(String resourceGroupName, String snapName) {
        return this.inner().revokeAccessAsync(resourceGroupName, snapName);
    }

    @Override
    public void revokeAccess(String resourceGroupName, String snapName) {
        this.revokeAccessAsync(resourceGroupName, snapName).block();
    }

    @Override
    protected SnapshotImpl wrapModel(String name) {
        return new SnapshotImpl(name, new SnapshotInner(), this.manager());
    }

    @Override
    protected SnapshotImpl wrapModel(SnapshotInner inner) {
        if (inner == null) {
            return null;
        }
        return new SnapshotImpl(inner.name(), inner, this.manager());
    }

    @Override
    public Snapshot.DefinitionStages.Blank define(String name) {
        return this.wrapModel(name);
    }
}
