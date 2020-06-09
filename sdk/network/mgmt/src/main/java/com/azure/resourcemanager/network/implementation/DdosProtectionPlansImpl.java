// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.DdosProtectionPlan;
import com.azure.resourcemanager.network.DdosProtectionPlans;
import com.azure.resourcemanager.network.fluent.inner.DdosProtectionPlanInner;
import com.azure.resourcemanager.network.fluent.DdosProtectionPlansInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** Implementation for DdosProtectionPlans. */
class DdosProtectionPlansImpl
    extends TopLevelModifiableResourcesImpl<
        DdosProtectionPlan, DdosProtectionPlanImpl, DdosProtectionPlanInner, DdosProtectionPlansInner, NetworkManager>
    implements DdosProtectionPlans {

    DdosProtectionPlansImpl(final NetworkManager networkManager) {
        super(networkManager.inner().ddosProtectionPlans(), networkManager);
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
