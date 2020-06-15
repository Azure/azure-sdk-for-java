// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.fluent.inner.AppServicePlanInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

import java.util.Locale;

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
            .inner()
            .getAppServicePlans()
            .createOrUpdateAsync(resourceGroupName(), name(), inner())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<AppServicePlanInner> getInnerAsync() {
        return this.manager().inner().getAppServicePlans().getByResourceGroupAsync(resourceGroupName(), name());
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
        if (inner().kind().toLowerCase(Locale.ROOT).contains("linux")) {
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
            throw logger.logExceptionAsError(new IllegalArgumentException("pricingTier == null"));
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
            throw logger.logExceptionAsError(new IllegalArgumentException("Capacity is at least 1."));
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
