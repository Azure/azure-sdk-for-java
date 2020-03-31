/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute.implementation;

import com.azure.management.compute.AccessLevel;
import com.azure.management.compute.Disk;
import com.azure.management.compute.Disks;
import com.azure.management.compute.GrantAccessData;
import com.azure.management.compute.models.DiskInner;
import com.azure.management.compute.models.DisksInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation for Disks.
 */
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
        return this.grantAccessAsync(resourceGroupName, diskName, accessLevel, accessDuration)
                .block();
    }

    @Override
    public Mono<String> grantAccessAsync(String resourceGroupName, String diskName, AccessLevel accessLevel, int accessDuration) {
        GrantAccessData grantAccessDataInner = new GrantAccessData();
        grantAccessDataInner.withAccess(accessLevel)
                .withDurationInSeconds(accessDuration);
        return this.inner().grantAccessAsync(resourceGroupName, diskName, grantAccessDataInner)
                .map(accessUriInner -> accessUriInner.accessSAS());
    }

    @Override
    public void revokeAccess(String resourceGroupName, String diskName) {
        this.inner().revokeAccess(resourceGroupName, diskName);
    }

    @Override
    public Mono<Void> revokeAccessAsync(String resourceGroupName, String diskName) {
        return this.inner().revokeAccessAsync(resourceGroupName, diskName);
    }

    @Override
    protected DiskImpl wrapModel(String name) {
        return new DiskImpl(name, new DiskInner(), this.manager());
    }

    @Override
    protected DiskImpl wrapModel(DiskInner inner) {
        if (inner == null) {
            return null;
        }
        return new DiskImpl(inner.getName(), inner, this.manager());
    }

    @Override
    public Disk.DefinitionStages.Blank define(String name) {
        return this.wrapModel(name);
    }
}