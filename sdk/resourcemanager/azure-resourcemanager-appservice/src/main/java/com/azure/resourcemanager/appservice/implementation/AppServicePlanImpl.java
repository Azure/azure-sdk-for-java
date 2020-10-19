// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.fluent.models.AppServicePlanInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

/** The implementation for AppServicePlan. */
class AppServicePlanImpl
    extends GroupableResourceImpl<AppServicePlan, AppServicePlanInner, AppServicePlanImpl, AppServiceManager>
    implements AppServicePlan, AppServicePlan.Definition, AppServicePlan.Update {

    private final ClientLogger logger = new ClientLogger(getClass());

    AppServicePlanImpl(String name, AppServicePlanInner innerObject, AppServiceManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public Mono<AppServicePlan> createResourceAsync() {
        return this
            .manager()
            .serviceClient()
            .getAppServicePlans()
            .createOrUpdateAsync(resourceGroupName(), name(), innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<AppServicePlanInner> getInnerAsync() {
        return this.manager().serviceClient().getAppServicePlans().getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    public int maxInstances() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().maximumNumberOfWorkers());
    }

    @Override
    public int capacity() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().sku().capacity());
    }

    @Override
    public boolean perSiteScaling() {
        return innerModel().perSiteScaling();
    }

    @Override
    public int numberOfWebApps() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().numberOfSites());
    }

    @Override
    public PricingTier pricingTier() {
        return PricingTier.fromSkuDescription(innerModel().sku());
    }

    @Override
    public OperatingSystem operatingSystem() {
        return (innerModel().reserved() == null || !innerModel().reserved())
            ? OperatingSystem.WINDOWS
            : OperatingSystem.LINUX;
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
            throw logger.logExceptionAsError(new IllegalArgumentException("pricingTier == null"));
        }
        innerModel().withSku(pricingTier.toSkuDescription());
        return this;
    }

    @Override
    public AppServicePlanImpl withPerSiteScaling(boolean perSiteScaling) {
        innerModel().withPerSiteScaling(perSiteScaling);
        return this;
    }

    @Override
    public AppServicePlanImpl withCapacity(int capacity) {
        if (capacity < 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Capacity is at least 1."));
        }
        innerModel().sku().withCapacity(capacity);
        return this;
    }

    @Override
    public AppServicePlanImpl withOperatingSystem(OperatingSystem operatingSystem) {
        if (OperatingSystem.LINUX.equals(operatingSystem)) {
            innerModel().withReserved(true);
            innerModel().withKind("linux");
        } else {
            innerModel().withKind("app");
        }
        return this;
    }
}
