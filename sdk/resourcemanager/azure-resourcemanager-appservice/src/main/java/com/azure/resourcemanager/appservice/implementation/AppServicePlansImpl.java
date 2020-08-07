// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.AppServicePlans;
import com.azure.resourcemanager.appservice.fluent.inner.AppServicePlanInner;
import com.azure.resourcemanager.appservice.fluent.AppServicePlansClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** The implementation for AppServicePlans. */
public class AppServicePlansImpl
    extends TopLevelModifiableResourcesImpl<
        AppServicePlan, AppServicePlanImpl, AppServicePlanInner, AppServicePlansClient, AppServiceManager>
    implements AppServicePlans {

    public AppServicePlansImpl(AppServiceManager manager) {
        super(manager.inner().getAppServicePlans(), manager);
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
        return new AppServicePlanImpl(inner.name(), inner, this.manager());
    }

    @Override
    public AppServicePlanImpl define(String name) {
        return wrapModel(name);
    }
}
