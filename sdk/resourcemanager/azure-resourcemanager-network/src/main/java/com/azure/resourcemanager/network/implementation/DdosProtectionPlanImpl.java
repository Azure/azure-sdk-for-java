// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.DdosProtectionPlan;
import com.azure.resourcemanager.network.fluent.inner.DdosProtectionPlanInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Mono;

/** Implementation for DdosProtectionPlan and its create and update interfaces. */
class DdosProtectionPlanImpl
    extends GroupableResourceImpl<DdosProtectionPlan, DdosProtectionPlanInner, DdosProtectionPlanImpl, NetworkManager>
    implements DdosProtectionPlan, DdosProtectionPlan.Definition, DdosProtectionPlan.Update {

    DdosProtectionPlanImpl(
        final String name, final DdosProtectionPlanInner innerModel, final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    @Override
    protected Mono<DdosProtectionPlanInner> getInnerAsync() {
        return this
            .manager()
            .inner()
            .getDdosProtectionPlans()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<DdosProtectionPlan> createResourceAsync() {
        return this
            .manager()
            .inner()
            .getDdosProtectionPlans()
            .createOrUpdateAsync(resourceGroupName(), name(), inner())
            .map(innerToFluentMap(this));
    }

    @Override
    public String resourceGuid() {
        return inner().resourceGuid();
    }

    @Override
    public String provisioningState() {
        return inner().provisioningState().toString();
    }

    @Override
    public List<SubResource> virtualNetworks() {
        return Collections.unmodifiableList(inner().virtualNetworks());
    }
}
