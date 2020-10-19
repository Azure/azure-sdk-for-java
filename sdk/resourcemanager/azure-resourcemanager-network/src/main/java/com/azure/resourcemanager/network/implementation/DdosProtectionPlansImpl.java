// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.DdosProtectionPlansClient;
import com.azure.resourcemanager.network.fluent.models.DdosProtectionPlanInner;
import com.azure.resourcemanager.network.models.DdosProtectionPlan;
import com.azure.resourcemanager.network.models.DdosProtectionPlans;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** Implementation for DdosProtectionPlans. */
public class DdosProtectionPlansImpl
    extends TopLevelModifiableResourcesImpl<
        DdosProtectionPlan, DdosProtectionPlanImpl, DdosProtectionPlanInner, DdosProtectionPlansClient, NetworkManager>
    implements DdosProtectionPlans {

    public DdosProtectionPlansImpl(final NetworkManager networkManager) {
        super(networkManager.serviceClient().getDdosProtectionPlans(), networkManager);
    }

    @Override
    public DdosProtectionPlanImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected DdosProtectionPlanImpl wrapModel(String name) {
        DdosProtectionPlanInner inner = new DdosProtectionPlanInner();
        return new DdosProtectionPlanImpl(name, inner, super.manager());
    }

    @Override
    protected DdosProtectionPlanImpl wrapModel(DdosProtectionPlanInner inner) {
        if (inner == null) {
            return null;
        }
        return new DdosProtectionPlanImpl(inner.name(), inner, this.manager());
    }
}
