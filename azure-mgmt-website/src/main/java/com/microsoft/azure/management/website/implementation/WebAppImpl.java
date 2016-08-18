/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.AppServicePricingTier;
import com.microsoft.azure.management.website.AzureResourceType;
import com.microsoft.azure.management.website.CloningInfo;
import com.microsoft.azure.management.website.HostNameBinding;
import com.microsoft.azure.management.website.HostNameSslState;
import com.microsoft.azure.management.website.HostingEnvironmentProfile;
import com.microsoft.azure.management.website.SiteAvailabilityState;
import com.microsoft.azure.management.website.SslState;
import com.microsoft.azure.management.website.UsageState;
import com.microsoft.azure.management.website.WebApp;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The implementation for {@link WebApp}.
 */
class WebAppImpl
    extends GroupableResourceImpl<
        WebApp,
            SiteInner,
        WebAppImpl,
        AppServiceManager>
    implements
        WebApp,
        WebApp.Definition,
        WebApp.Update {

    private final SitesInner client;
    private Map<String, HostNameSslState> hostNameSslStateMap;

    WebAppImpl(String key, SiteInner innerObject, final SitesInner client, AppServiceManager manager) {
        super(key, innerObject, manager);
        this.client = client;
        this.hostNameSslStateMap = new HashMap<>();
        if (innerObject.hostNameSslStates() != null) {
            for (HostNameSslState hostNameSslState : innerObject.hostNameSslStates()) {
                hostNameSslStateMap.put(hostNameSslState.name(), hostNameSslState);
            }
        }
    }

    @Override
    public String siteName() {
        return inner().siteName();
    }

    @Override
    public String state() {
        return inner().state();
    }

    @Override
    public List<String> hostNames() {
        return inner().hostNames();
    }

    @Override
    public String repositorySiteName() {
        return inner().repositorySiteName();
    }

    @Override
    public UsageState usageState() {
        return inner().usageState();
    }

    @Override
    public boolean enabled() {
        return inner().enabled();
    }

    @Override
    public List<String> enabledHostNames() {
        return inner().enabledHostNames();
    }

    @Override
    public SiteAvailabilityState availabilityState() {
        return inner().availabilityState();
    }

    @Override
    public List<HostNameSslState> hostNameSslStates() {
        return inner().hostNameSslStates();
    }

    @Override
    public String serverFarmId() {
        return inner().serverFarmId();
    }

    @Override
    public DateTime lastModifiedTimeUtc() {
        return inner().lastModifiedTimeUtc();
    }

    @Override
    public SiteConfigInner siteConfig() {
        return inner().siteConfig();
    }

    @Override
    public List<String> trafficManagerHostNames() {
        return inner().trafficManagerHostNames();
    }

    @Override
    public boolean premiumAppDeployed() {
        return inner().premiumAppDeployed();
    }

    @Override
    public boolean scmSiteAlsoStopped() {
        return inner().scmSiteAlsoStopped();
    }

    @Override
    public String targetSwapSlot() {
        return inner().targetSwapSlot();
    }

    @Override
    public HostingEnvironmentProfile hostingEnvironmentProfile() {
        return inner().hostingEnvironmentProfile();
    }

    @Override
    public String microService() {
        return inner().microService();
    }

    @Override
    public String gatewaySiteName() {
        return inner().gatewaySiteName();
    }

    @Override
    public boolean clientAffinityEnabled() {
        return inner().clientAffinityEnabled();
    }

    @Override
    public boolean clientCertEnabled() {
        return inner().clientCertEnabled();
    }

    @Override
    public boolean hostNamesDisabled() {
        return false;
    }

    @Override
    public String outboundIpAddresses() {
        return inner().outboundIpAddresses();
    }

    @Override
    public int containerSize() {
        return inner().containerSize();
    }

    @Override
    public int maxNumberOfWorkers() {
        return inner().maxNumberOfWorkers();
    }

    @Override
    public CloningInfo cloningInfo() {
        return inner().cloningInfo();
    }

    @Override
    public String resourceGroup() {
        return inner().resourceGroup();
    }

    @Override
    public boolean isDefaultContainer() {
        return inner().isDefaultContainer();
    }

    @Override
    public String defaultHostName() {
        return inner().defaultHostName();
    }

    @Override
    public List<HostNameBinding> hostNameBindings() throws CloudException, IOException {
        //TODO: Use wrapList()
        HostNameBindingCollectionInner collectionInner = client.getSiteHostNameBindings(resourceGroupName(), name()).getBody();
        List<HostNameBinding> hostNameBindings = new ArrayList<>();
        for (HostNameBindingInner inner : collectionInner.value()) {
            hostNameBindings.add(new HostNameBindingImpl(inner.name(), inner, this, client));
        }
        return hostNameBindings;
    }

    @Override
    protected void createResource() throws Exception {
        if (hostNameSslStateMap.size() > 0) {
            inner().withHostNameSslStates(new ArrayList<>(hostNameSslStateMap.values()));
        }
        SiteInner site = client.createOrUpdateSite(resourceGroupName(), name(), inner()).getBody();
        this.setInner(site);
    }

    @Override
    protected ServiceCall createResourceAsync(ServiceCallback<Void> callback) {
        return client.createOrUpdateSiteAsync(resourceGroupName(), name(), inner(), Utils.fromVoidCallback(this, callback));
    }

    @Override
    public WebAppImpl refresh() throws Exception {
        this.setInner(client.getSite(resourceGroupName(), name()).getBody());
        return this;
    }

    @Override
    public WebAppImpl withNewAppServicePlan() {
        String appServicePlanName = ResourceNamer.randomResourceName(name(), 10);
        AppServicePlan.DefinitionStages.WithCreate creatable = myManager.appServicePlans().define(appServicePlanName)
                .withRegion(region())
                .withNewResourceGroup(resourceGroupName())
                .withPricingTier(AppServicePricingTier.FREE_F1);
        addCreatableDependency(creatable);
        inner().withServerFarmId(appServicePlanName);
        return this;
    }

    @Override
    public WebAppImpl withNewAppServicePlan(String name, AppServicePricingTier pricingTier) {
        AppServicePlan.DefinitionStages.WithCreate creatable = myManager.appServicePlans().define(name)
                .withRegion(region())
                .withNewResourceGroup(resourceGroupName())
                .withPricingTier(pricingTier);
        addCreatableDependency(creatable);
        inner().withServerFarmId(name);
        return this;
    }

    @Override
    public WebAppImpl withExistingAppServicePlan(String appServicePlanName) {
        inner().withServerFarmId(appServicePlanName);
        return this;
    }

    private boolean isUpdateSsl(String hostName) {
        boolean update = false;
        if (hostNameSslStates() != null) {
            for (HostNameSslState hostNameSslState : hostNameSslStates()) {
                if (hostName.equals(hostNameSslState.name())) {
                    update = true;
                }
            }
        }
        return update;
    }

    @Override
    public WebAppImpl disableSsl(String hostName) {
        if (hostName == null) {
            throw new IllegalArgumentException("Null host name");
        }
        hostNameSslStateMap.put(hostName, new HostNameSslState()
                .withName(hostName)
                .withSslState(SslState.DISABLED)
                .withToUpdate(isUpdateSsl(hostName)));
        return this;
    }

    @Override
    public WebAppImpl enableSniSsl(String hostName, String thumbprint) {
        if (hostName == null) {
            throw new IllegalArgumentException("Null host name");
        }
        hostNameSslStateMap.put(hostName, new HostNameSslState()
                .withName(hostName)
                .withSslState(SslState.SNI_ENABLED)
                .withThumbprint(thumbprint)
                .withToUpdate(isUpdateSsl(hostName)));
        return this;
    }

    @Override
    public WebAppImpl enableIpBasedSsl(String hostName, String thumbprint, String virtualIp) {
        if (hostName == null) {
            throw new IllegalArgumentException("Null host name");
        }
        hostNameSslStateMap.put(hostName, new HostNameSslState()
                .withName(hostName)
                .withSslState(SslState.SNI_ENABLED)
                .withThumbprint(thumbprint)
                .withVirtualIP(virtualIp)
                .withToUpdate(isUpdateSsl(hostName)));
        return this;
    }

    @Override
    public WebAppImpl apply() throws Exception {
        create();
        return this;
    }

    @Override
    public ServiceCall applyAsync(ServiceCallback<WebApp> callback) {
        return createAsync(callback);
    }

    @Override
    public HostNameBinding.DefinitionStages.Blank<WebApp.Update> defineHostNameBinding(String name) {
        HostNameBindingInner inner = new HostNameBindingInner();
        inner.withSiteName(name());
        inner.withLocation(regionName());
        inner.withAzureResourceType(AzureResourceType.WEBSITE);
        return new HostNameBindingImpl(name, inner, this, client);
    }

    WebAppImpl withHostNameBinding(HostNameBindingImpl hostNameBinding) {
        addCreatableDependency(hostNameBinding);
        return this;
    }

    AppServiceManager myManager() {
        return super.myManager;
    }
}
