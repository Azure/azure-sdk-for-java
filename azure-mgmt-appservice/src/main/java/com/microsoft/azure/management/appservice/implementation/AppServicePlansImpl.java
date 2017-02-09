/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.AppServicePlans;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for AppServicePlans.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class AppServicePlansImpl
        extends GroupableResourcesImpl<
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
    public AppServicePlan getByGroup(String groupName, String name) {
        return wrapModel(this.inner().get(groupName, name));
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name).toCompletable();
    }

    @Override
    public PagedList<AppServicePlan> listByGroup(String resourceGroupName) {
        return wrapList(this.inner().listByResourceGroup(resourceGroupName));
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

    @Override
    public Observable<AppServicePlan> getByIdAsync(String id) {
        return this.inner().getAsync(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(id))
                .map(new Func1<AppServicePlanInner, AppServicePlan>() {
                    @Override
                    public AppServicePlan call(AppServicePlanInner appServicePlanInner) {
                        return wrapModel(appServicePlanInner);
                    }
                });
    }
}