/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute.implementation;

import com.azure.management.compute.AccessLevel;
import com.azure.management.compute.GrantAccessData;
import com.azure.management.compute.Snapshot;
import com.azure.management.compute.Snapshots;
import com.azure.management.compute.models.SnapshotInner;
import com.azure.management.compute.models.SnapshotsInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation for Snapshots.
 */
class SnapshotsImpl
    extends TopLevelModifiableResourcesImpl<
        Snapshot,
        SnapshotImpl,
        SnapshotInner,
        SnapshotsInner,
        ComputeManager>
    implements Snapshots {

    SnapshotsImpl(ComputeManager computeManager) {
        super(computeManager.inner().snapshots(), computeManager);
    }

    @Override
    public Mono<String> grantAccessAsync(String resourceGroupName, String snapshotName, AccessLevel accessLevel, int accessDuration) {
        GrantAccessData grantAccessDataInner = new GrantAccessData();
        grantAccessDataInner.withAccess(accessLevel)
                .withDurationInSeconds(accessDuration);
        return inner().grantAccessAsync(resourceGroupName, snapshotName, grantAccessDataInner)
                .onErrorResume(e -> Mono.empty())
                .map(accessUriInner -> accessUriInner.accessSAS());
    }

    @Override
    public String grantAccess(String resourceGroupName,
                              String snapshotName,
                              AccessLevel accessLevel,
                              int accessDuration) {
        return this.grantAccessAsync(resourceGroupName, snapshotName, accessLevel, accessDuration)
                .block();
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
        return new SnapshotImpl(inner.getName(), inner, this.manager());
    }

    @Override
    public Snapshot.DefinitionStages.Blank define(String name) {
        return this.wrapModel(name);
    }
}