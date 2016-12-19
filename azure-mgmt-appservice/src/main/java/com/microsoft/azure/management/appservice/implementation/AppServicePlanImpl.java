/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.AppServicePricingTier;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;

/**
 * The implementation for {@link AppServicePlan}.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class AppServicePlanImpl
        extends
        GroupableResourceImpl<
                AppServicePlan,
                AppServicePlanInner,
                AppServicePlanImpl,
                AppServiceManager>
        implements
        AppServicePlan,
        AppServicePlan.Definition,
        AppServicePlan.Update {

    private final AppServicePlansInner client;

    AppServicePlanImpl(String name, AppServicePlanInner innerObject, final AppServicePlansInner client, AppServiceManager manager) {
        super(name, innerObject, manager);
        this.client = client;
    }

    @Override
    public Observable<AppServicePlan> createResourceAsync() {
        return client.createOrUpdateAsync(resourceGroupName(), name(), inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public AppServicePlanImpl refresh() {
        this.setInner(client.get(resourceGroupName(), name()));
        return this;
    }

    @Override
    public int maxInstances() {
        return Utils.toPrimitiveInt(inner().maximumNumberOfWorkers());
    }

    @Override
    public int capacity() {
        return Utils.toPrimitiveInt(inner().sku().capacity());
    }

    @Override
    public boolean perSiteScaling() {
        return inner().perSiteScaling();
    }

    @Override
    public int numberOfWebApps() {
        return Utils.toPrimitiveInt(inner().numberOfSites());
    }

    @Override
    public AppServicePricingTier pricingTier() {
        return AppServicePricingTier.fromSkuDescription(inner().sku());
    }

    @Override
    public AppServicePlanImpl withPricingTier(AppServicePricingTier pricingTier) {
        if (pricingTier == null) {
            throw new IllegalArgumentException("pricingTier == null");
        }
        inner().withSku(pricingTier.toSkuDescription());
        return this;
    }

    @Override
    public AppServicePlanImpl withPerSiteScaling(boolean perSiteScaling) {
        inner().withPerSiteScaling(perSiteScaling);
        return this;
    }

    @Override
    public AppServicePlanImpl withCapacity(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity is at least 1.");
        }
        inner().sku().withCapacity(capacity);
        return this;
    }
}