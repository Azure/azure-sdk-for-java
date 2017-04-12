/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.AccessLevel;
import com.microsoft.azure.management.compute.Disk;
import com.microsoft.azure.management.compute.Disks;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * The implementation for Disks.
 */
@LangDefinition
class DisksImpl
    extends TopLevelModifiableResourcesImpl<
        Disk,
        DiskImpl,
        DiskInner,
        DisksInner,
        ComputeManager>
    implements Disks {

    DisksImpl(ComputeManager computeManager) {
        super(computeManager.inner().disks(), computeManager);
    }

    @Override
    public String grantAccess(String resourceGroupName,
                              String diskName,
                              AccessLevel accessLevel,
                              int accessDuration) {
        GrantAccessDataInner grantAccessDataInner = new GrantAccessDataInner();
        grantAccessDataInner.withAccess(accessLevel)
                .withDurationInSeconds(accessDuration);
        AccessUriInner accessUriInner = this.inner().grantAccess(resourceGroupName,
                diskName, grantAccessDataInner);
        return accessUriInner.accessSAS();
    }

    @Override
    public void revokeAccess(String resourceGroupName, String diskName) {
        this.inner().revokeAccess(resourceGroupName, diskName);
    }

    @Override
    protected DiskImpl wrapModel(String name) {
        return new DiskImpl(name, new DiskInner(), this.manager());
    }

    @Override
    protected DiskImpl wrapModel(DiskInner inner) {
        if (inner != null) {
            return new DiskImpl(inner.name(), inner, this.manager());
        } else {
            return null;
        }
    }

    @Override
    public Disk.DefinitionStages.Blank define(String name) {
        return this.wrapModel(name);
    }
}