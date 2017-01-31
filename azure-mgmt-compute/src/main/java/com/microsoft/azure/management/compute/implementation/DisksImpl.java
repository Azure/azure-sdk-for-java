/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.AccessLevel;
import com.microsoft.azure.management.compute.Disk;
import com.microsoft.azure.management.compute.Disks;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Completable;

/**
 * The implementation for {@link Disks}.
 */
@LangDefinition
class DisksImpl
        extends GroupableResourcesImpl<
        Disk,
        DiskImpl,
        DiskInner,
        DisksInner,
        ComputeManager>
        implements Disks {

    DisksImpl(DisksInner client,
              ComputeManager computeManager) {
        super(client, computeManager);
    }
    @Override
    public String grantAccess(String resourceGroupName,
                              String diskName,
                              AccessLevel accessLevel,
                              int accessDuration) {
        GrantAccessDataInner grantAccessDataInner = new GrantAccessDataInner();
        grantAccessDataInner.withAccess(accessLevel)
                .withDurationInSeconds(accessDuration);
        AccessUriInner accessUriInner = this.innerCollection.grantAccess(resourceGroupName,
                diskName, grantAccessDataInner);
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
    public Disk getByGroup(String resourceGroupName, String name) {
        DiskInner inner = this.innerCollection.get(resourceGroupName, name);
        return wrapModel(inner);
    }

    @Override
    public PagedList<Disk> listByGroup(String resourceGroupName) {
        return wrapList(this.innerCollection.listByResourceGroup(resourceGroupName));
    }

    @Override
    public PagedList<Disk> list() {
        return wrapList(this.innerCollection.list());
    }

    @Override
    protected DiskImpl wrapModel(String name) {
        return new DiskImpl(name,
                new DiskInner(),
                this.innerCollection,
                myManager);
    }

    @Override
    protected DiskImpl wrapModel(DiskInner inner) {
        return new DiskImpl(inner.name(),
                inner,
                this.innerCollection,
                myManager);
    }

    @Override
    public Disk.DefinitionStages.Blank define(String name) {
        return this.wrapModel(name);
    }
}