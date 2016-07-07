/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.AppServicePlans;

import java.io.IOException;

/**
 * The implementation for {@link AppServicePlans}.
 */
class AppServicePlansImpl
    extends GroupableResourcesImpl<
        AppServicePlan,
        AppServicePlanImpl,
            ServerFarmWithRichSkuInner,
            ServerFarmsInner,
            WebsiteManager>
    implements AppServicePlans {

    AppServicePlansImpl(ServerFarmsInner innerCollection, WebsiteManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public AppServicePlan getByGroup(String groupName, String name) throws CloudException, IOException {
        return wrapModel(innerCollection.getServerFarm(groupName, name).getBody());
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        innerCollection.deleteServerFarm(groupName, name);
    }

    @Override
    public PagedList<AppServicePlan> listByGroup(String resourceGroupName) throws CloudException, IOException {
        return wrapList(innerCollection.getServerFarms(resourceGroupName).getBody().value());
    }

    @Override
    protected AppServicePlanImpl wrapModel(String name) {
        return new AppServicePlanImpl(name, new ServerFarmWithRichSkuInner(), innerCollection, myManager);
    }

    @Override
    protected AppServicePlanImpl wrapModel(ServerFarmWithRichSkuInner inner) {
        return new AppServicePlanImpl(inner.name(), inner, innerCollection, myManager);
    }

    @Override
    public AppServicePlan.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public void delete(String id) throws Exception {
        innerCollection.deleteServerFarm(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }
}
