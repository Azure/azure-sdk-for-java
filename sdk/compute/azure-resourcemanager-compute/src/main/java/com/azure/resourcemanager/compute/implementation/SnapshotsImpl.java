// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.AccessLevel;
import com.azure.resourcemanager.compute.models.GrantAccessData;
import com.azure.resourcemanager.compute.models.Snapshot;
import com.azure.resourcemanager.compute.models.Snapshots;
import com.azure.resourcemanager.compute.fluent.models.SnapshotInner;
import com.azure.resourcemanager.compute.fluent.SnapshotsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/** The implementation for Snapshots. */
public class SnapshotsImpl
    extends TopLevelModifiableResourcesImpl<Snapshot, SnapshotImpl, SnapshotInner, SnapshotsClient, ComputeManager>
    implements Snapshots {

    private final ClientLogger logger = new ClientLogger(this.getClass());

    public SnapshotsImpl(ComputeManager computeManager) {
        super(computeManager.serviceClient().getSnapshots(), computeManager);
    }

    @Override
    public Mono<String> grantAccessAsync(String resourceGroupName, String snapshotName, AccessLevel accessLevel,
        int accessDuration) {
        GrantAccessData grantAccessDataInner = new GrantAccessData();
        grantAccessDataInner.withAccess(accessLevel).withDurationInSeconds(accessDuration);
        return inner().grantAccessAsync(resourceGroupName, snapshotName, grantAccessDataInner)
            .map(accessUriInner -> accessUriInner.accessSas());
    }

    @Override
    public String grantAccess(String resourceGroupName, String snapshotName, AccessLevel accessLevel,
        int accessDuration) {
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
    public Accepted<Void> beginDeleteById(String id) {
        return beginDeleteById(id, Context.NONE);
    }

    @Override
    public Accepted<Void> beginDeleteById(String id, Context context) {
        return beginDeleteByResourceGroup(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id),
            context);
    }

    @Override
    public Accepted<Void> beginDeleteByResourceGroup(String resourceGroupName, String name) {
        return beginDeleteByResourceGroup(resourceGroupName, name, Context.NONE);
    }

    @Override
    public Accepted<Void> beginDeleteByResourceGroup(String resourceGroupName, String name, Context context) {
        return AcceptedImpl.newAccepted(logger, this.manager().serviceClient().getHttpPipeline(),
            this.manager().serviceClient().getDefaultPollInterval(),
            () -> this.inner()
                .deleteWithResponseAsync(resourceGroupName, name)
                .contextWrite(c -> c.putAll(FluxUtil.toReactorContext(context).readOnly()))
                .block(),
            Function.identity(), Void.class, null, context);
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
