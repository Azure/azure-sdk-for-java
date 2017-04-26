/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.AccessLevel;
import com.microsoft.azure.management.compute.Snapshot;
import com.microsoft.azure.management.compute.Snapshots;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * The implementation for Snapshots.
 */
@LangDefinition
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
    public String grantAccess(String resourceGroupName,
                              String snapshotName,
                              AccessLevel accessLevel,
                              int accessDuration) {
        GrantAccessDataInner grantAccessDataInner = new GrantAccessDataInner();
        grantAccessDataInner.withAccess(accessLevel)
                .withDurationInSeconds(accessDuration);
        AccessUriInner accessUriInner = this.inner().grantAccess(resourceGroupName, snapshotName, grantAccessDataInner);
        return accessUriInner.accessSAS();
    }

    @Override
    public void revokeAccess(String resourceGroupName, String diskName) {
        this.inner().revokeAccess(resourceGroupName, diskName);
    }

    @Override
    protected SnapshotImpl wrapModel(String name) {
        return new SnapshotImpl(name, new SnapshotInner(), this.manager());
    }

    @Override
    protected SnapshotImpl wrapModel(SnapshotInner inner) {
        return new SnapshotImpl(inner.name(), inner, this.manager());
    }

    @Override
    public Snapshot.DefinitionStages.Blank define(String name) {
        return this.wrapModel(name);
    }
}