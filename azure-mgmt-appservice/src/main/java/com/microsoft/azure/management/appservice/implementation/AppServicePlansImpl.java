/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.AppServicePlans;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * The implementation for AppServicePlans.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
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
        return new AppServicePlanImpl(inner.name(), inner, this.manager());
    }

    @Override
    public AppServicePlanImpl define(String name) {
        return wrapModel(name);
    }
}
