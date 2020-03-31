/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.management.appservice.AppServicePlan;
import com.azure.management.appservice.OperatingSystem;
import com.azure.management.appservice.PricingTier;
import com.azure.management.appservice.models.AppServicePlanInner;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.management.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

/**
 * The implementation for AppServicePlan.
 */
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

    AppServicePlanImpl(String name, AppServicePlanInner innerObject, AppServiceManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public Mono<AppServicePlan> createResourceAsync() {
        return this.manager().inner().appServicePlans().createOrUpdateAsync(resourceGroupName(), name(), inner())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<AppServicePlanInner> getInnerAsync() {
        return this.manager().inner().appServicePlans().getByResourceGroupAsync(resourceGroupName(), name());
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
    public PricingTier pricingTier() {
        return PricingTier.fromSkuDescription(inner().sku());
    }

    @Override
    public OperatingSystem operatingSystem() {
        if (inner().kind().toLowerCase().contains("linux")) {
            return OperatingSystem.LINUX;
        } else {
            return OperatingSystem.WINDOWS;
        }
    }

    @Override
    public AppServicePlanImpl withFreePricingTier() {
        return withPricingTier(PricingTier.FREE_F1);
    }

    @Override
    public AppServicePlanImpl withSharedPricingTier() {
        return withPricingTier(PricingTier.SHARED_D1);
    }

    @Override
    public AppServicePlanImpl withPricingTier(PricingTier pricingTier) {
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

    @Override
    public AppServicePlanImpl withOperatingSystem(OperatingSystem operatingSystem) {
        if (OperatingSystem.LINUX.equals(operatingSystem)) {
            inner().withReserved(true);
            inner().withKind("linux");
        } else {
            inner().withKind("app");
        }
        return this;
    }
}