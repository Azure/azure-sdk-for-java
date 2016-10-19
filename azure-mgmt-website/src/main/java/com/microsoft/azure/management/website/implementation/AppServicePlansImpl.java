/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.AppServicePlans;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for {@link AppServicePlans}.
 */
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
    public Observable<Void> deleteAsync(String groupName, String name) {
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
        return new AppServicePlanImpl(inner.name(), inner, innerCollection, myManager);
    }

    @Override
    public AppServicePlanImpl define(String name) {
        return wrapModel(name);
    }
}