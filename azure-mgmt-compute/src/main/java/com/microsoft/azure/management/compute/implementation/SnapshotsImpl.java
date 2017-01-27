/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.AccessLevel;
import com.microsoft.azure.management.compute.Snapshot;
import com.microsoft.azure.management.compute.Snapshots;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Completable;

/**
 * The implementation for {@link Snapshots}.
 */
@LangDefinition
class SnapshotsImpl
        extends GroupableResourcesImpl<
        Snapshot,
        SnapshotImpl,
        SnapshotInner,
        SnapshotsInner,
        ComputeManager>
        implements Snapshots {

    SnapshotsImpl(SnapshotsInner client,
              ComputeManager computeManager) {
        super(client, computeManager);
    }
    @Override
    public String grantAccess(String resourceGroupName,
                              String snapshotName,
                              AccessLevel accessLevel,
                              int accessDuration) {
        GrantAccessDataInner grantAccessDataInner = new GrantAccessDataInner();
        grantAccessDataInner.withAccess(accessLevel)
                .withDurationInSeconds(accessDuration);
        AccessUriInner accessUriInner = this.innerCollection.grantAccess(resourceGroupName,
                snapshotName,grantAccessDataInner);
        return accessUriInner.accessSAS();
    }

    @Override
    public void revokeAccess(String resourceGroupName, String diskName) {
        this.innerCollection.revokeAccess(resourceGroupName, diskName);
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name).toCompletable();
    }

    @Override
    public Snapshot getByGroup(String resourceGroupName, String name) {
        SnapshotInner inner = this.innerCollection.get(resourceGroupName, name);
        return wrapModel(inner);
    }

    @Override
    public PagedList<Snapshot> listByGroup(String resourceGroupName) {
        return wrapList(this.innerCollection.listByResourceGroup(resourceGroupName));
    }

    @Override
    public PagedList<Snapshot> list() {
        return wrapList(this.innerCollection.list());
    }

    @Override
    protected SnapshotImpl wrapModel(String name) {
        return new SnapshotImpl(name,
                new SnapshotInner(),
                this.innerCollection,
                myManager);
    }

    @Override
    protected SnapshotImpl wrapModel(SnapshotInner inner) {
        return new SnapshotImpl(inner.name(),
                inner,
                this.innerCollection,
                myManager);
    }

    @Override
    public Snapshot.DefinitionStages.Blank define(String name) {
        return this.wrapModel(name);
    }
}