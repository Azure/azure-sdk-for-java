/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.DdosProtectionPlan;
import com.azure.management.network.DdosProtectionPlans;
import com.azure.management.network.models.DdosProtectionPlanInner;
import com.azure.management.network.models.DdosProtectionPlansInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * Implementation for DdosProtectionPlans.
 */
class DdosProtectionPlansImpl
        extends TopLevelModifiableResourcesImpl<
        DdosProtectionPlan,
        DdosProtectionPlanImpl,
        DdosProtectionPlanInner,
        DdosProtectionPlansInner,
        NetworkManager>
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
        return new DdosProtectionPlanImpl(inner.getName(), inner, this.manager());
    }
}

