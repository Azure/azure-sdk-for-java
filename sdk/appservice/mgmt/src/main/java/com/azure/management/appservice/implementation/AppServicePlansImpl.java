/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.management.appservice.AppServicePlan;
import com.azure.management.appservice.AppServicePlans;
import com.azure.management.appservice.models.AppServicePlanInner;
import com.azure.management.appservice.models.AppServicePlansInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * The implementation for AppServicePlans.
 */
class AppServicePlansImpl
        extends TopLevelModifiableResourcesImpl<
        AppServicePlan,
                AppServicePlanImpl,
                AppServicePlanInner,
                AppServicePlansInner,
                AppServiceManager>
        implements AppServicePlans {

    AppServicePlansImpl(AppServiceManager manager) {
        super(manager.inner().appServicePlans(), manager);
    }

    @Override
    protected AppServicePlanImpl wrapModel(String name) {
        return new AppServicePlanImpl(name, new AppServicePlanInner(), this.manager());
    }

    @Override
    protected AppServicePlanImpl wrapModel(AppServicePlanInner inner) {
        if (inner == null) {
            return null;
        }
        return new AppServicePlanImpl(inner.getName(), inner, this.manager());
    }

    @Override
    public AppServicePlanImpl define(String name) {
        return wrapModel(name);
    }
}
