/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.AppServicePricingTier;
import com.microsoft.azure.management.website.HostingEnvironmentProfile;
import com.microsoft.azure.management.website.SkuDescription;
import com.microsoft.azure.management.website.StatusOptions;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;

/**
 * The implementation for {@link AppServicePlan}.
 */
class AppServicePlanImpl
    extends
        GroupableResourceImpl<
            AppServicePlan,
            ServerFarmWithRichSkuInner,
            AppServicePlanImpl,
                AppServiceManager>
    implements
        AppServicePlan,
        AppServicePlan.Definition,
        AppServicePlan.Update {

    private final ServerFarmsInner client;

    AppServicePlanImpl(String key, ServerFarmWithRichSkuInner innerObject, final ServerFarmsInner client, AppServiceManager manager) {
        super(key, innerObject, manager);
        this.client = client;
    }

    @Override
    protected void createResource() throws Exception {
        this.setInner(client.createOrUpdateServerFarm(resourceGroupName(), name(), inner()).getBody());
    }

    @Override
    protected ServiceCall createResourceAsync(ServiceCallback<Void> callback) {
        return client.createOrUpdateServerFarmAsync(resourceGroupName(), name(), inner(), Utils.fromVoidCallback(this, callback));
    }

    @Override
    public AppServicePlan refresh() throws Exception {
        this.setInner(client.getServerFarm(resourceGroupName(), name()).getBody());
        return this;
    }

    @Override
    public String serverFarmWithRichSkuName() {
        return inner().serverFarmWithRichSkuName();
    }

    @Override
    public String workerTierName() {
        return inner().workerTierName();
    }

    @Override
    public StatusOptions status() {
        return inner().status();
    }

    @Override
    public String subscription() {
        return inner().subscription();
    }

    @Override
    public String adminSiteName() {
        return inner().adminSiteName();
    }

    @Override
    public HostingEnvironmentProfile hostingEnvironmentProfile() {
        return inner().hostingEnvironmentProfile();
    }

    @Override
    public int maximumNumberOfWorkers() {
        return inner().maximumNumberOfWorkers();
    }

    @Override
    public String geoRegion() {
        return inner().geoRegion();
    }

    @Override
    public boolean perSiteScaling() {
        return inner().perSiteScaling();
    }

    @Override
    public int numberOfSites() {
        return inner().numberOfSites();
    }

    @Override
    public String resourceGroup() {
        return inner().resourceGroup();
    }

    @Override
    public SkuDescription sku() {
        return inner().sku();
    }

    @Override
    public AppServicePlanImpl withPricingTier(AppServicePricingTier pricingTier) {
        inner().withSku(pricingTier.toSkuDescription());
        return this;
    }
}
