// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.AccessLevel;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.Disks;
import com.azure.resourcemanager.compute.models.GrantAccessData;
import com.azure.resourcemanager.compute.fluent.inner.DiskInner;
import com.azure.resourcemanager.compute.fluent.DisksClient;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/** The implementation for Disks. */
public class DisksImpl extends TopLevelModifiableResourcesImpl<Disk, DiskImpl, DiskInner, DisksClient, ComputeManager>
    implements Disks {

    private final ClientLogger logger = new ClientLogger(this.getClass());

    public DisksImpl(ComputeManager computeManager) {
        super(computeManager.inner().getDisks(), computeManager);
    }

    @Override
    public String grantAccess(String resourceGroupName, String diskName, AccessLevel accessLevel, int accessDuration) {
        return this.grantAccessAsync(resourceGroupName, diskName, accessLevel, accessDuration).block();
    }

    @Override
    public Mono<String> grantAccessAsync(
        String resourceGroupName, String diskName, AccessLevel accessLevel, int accessDuration) {
        GrantAccessData grantAccessDataInner = new GrantAccessData();
        grantAccessDataInner.withAccess(accessLevel).withDurationInSeconds(accessDuration);
        return this
            .inner()
            .grantAccessAsync(resourceGroupName, diskName, grantAccessDataInner)
            .map(accessUriInner -> accessUriInner.accessSas());
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
    public Accepted<Void> beginDeleteById(String id) {
        return beginDeleteByResourceGroup(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public Accepted<Void> beginDeleteByResourceGroup(String resourceGroupName, String name) {
        return AcceptedImpl.newAccepted(logger,
            manager().inner(),
            () -> this.inner().deleteWithResponseAsync(resourceGroupName, name).block(),
            Function.identity(),
            Void.class,
            null);
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
        return new DiskImpl(inner.name(), inner, this.manager());
    }

    @Override
    public Disk.DefinitionStages.Blank define(String name) {
        return this.wrapModel(name);
    }
}
