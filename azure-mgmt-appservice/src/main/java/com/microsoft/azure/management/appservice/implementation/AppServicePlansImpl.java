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
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for {@link AppServicePlans}.
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

    AppServicePlansImpl(AppServicePlansInner innerCollection, AppServiceManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public AppServicePlan getByGroup(String groupName, String name) {
        return wrapModel(innerCollection.get(groupName, name));
    }

    @Override
    public Observable<Void> deleteByGroupAsync(String groupName, String name) {
        return innerCollection.deleteAsync(groupName, name)
                .map(new Func1<Object, Void>() {
                    @Override
                    public Void call(Object o) {
                        return null;
                    }
                });
    }

    @Override
    public PagedList<AppServicePlan> listByGroup(String resourceGroupName) {
        return wrapList(innerCollection.listByResourceGroup(resourceGroupName));
    }

    @Override
    protected AppServicePlanImpl wrapModel(String name) {
        return new AppServicePlanImpl(name, new AppServicePlanInner(), innerCollection, myManager);
    }

    @Override
    protected AppServicePlanImpl wrapModel(AppServicePlanInner inner) {
        if (inner == null) {
            return null;
        }
        return new AppServicePlanImpl(inner.name(), inner, innerCollection, myManager);
    }

    @Override
    public AppServicePlanImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<AppServicePlan> getByIdAsync(String id) {
        return innerCollection.getAsync(
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