/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.AppServicePricingTier;
import com.microsoft.azure.management.website.AzureResourceType;
import com.microsoft.azure.management.website.CloningInfo;
import com.microsoft.azure.management.website.Domain;
import com.microsoft.azure.management.website.HostNameBinding;
import com.microsoft.azure.management.website.HostNameSslState;
import com.microsoft.azure.management.website.SiteAvailabilityState;
import com.microsoft.azure.management.website.SslState;
import com.microsoft.azure.management.website.UsageState;
import com.microsoft.azure.management.website.WebApp;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final WebAppsInner client;
    private Map<String, HostNameSslState> hostNameSslStateMap;
    private HostNameBindingImpl hostNameBinding;
    String domainName;

    WebAppImpl(String key, SiteInner innerObject, final WebAppsInner client, AppServiceManager manager) {
        super(key, innerObject, manager);
        this.client = client;
        this.hostNameSslStateMap = new HashMap<>();
        if (innerObject.hostNameSslStates() != null) {
            for (HostNameSslState hostNameSslState : innerObject.hostNameSslStates()) {
                // Server returns null sometimes, invalid on update, so we set default
                if (hostNameSslState.sslState() == null) {
                    hostNameSslState.withSslState(SslState.DISABLED);
                }
                hostNameSslStateMap.put(hostNameSslState.name(), hostNameSslState);
            }
        }
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
    public DateTime lastModifiedTime() {
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
    public CloningInfo cloningInfo() {
        return inner().cloningInfo();
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
    public List<HostNameBinding> getHostNameBindings() {
        //TODO: Use wrapList()
        List<HostNameBindingInner> collectionInner = client.listHostNameBindings(resourceGroupName(), name());
        List<HostNameBinding> hostNameBindings = new ArrayList<>();
        for (HostNameBindingInner inner : collectionInner) {
            hostNameBindings.add(new HostNameBindingImpl(inner.name(), inner, this, client));
        }
        return hostNameBindings;
    }

    @Override
    public WebAppImpl refresh() {
        this.setInner(client.get(resourceGroupName(), name()));
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
    public HostNameBindingImpl defineHostNameBinding(String hostname) {
        Pattern pattern = Pattern.compile("([.\\w]+)\\.(\\w+\\.\\w+)");
        Matcher matcher = pattern.matcher(hostname);
        matcher.matches();
        String subdomain =matcher.group(1);
        String domain = matcher.group(2);
        HostNameBindingInner inner = new HostNameBindingInner();
        inner.withSiteName(name());
        inner.withLocation(regionName());
        inner.withAzureResourceType(AzureResourceType.WEBSITE);
        inner.withAzureResourceName(name());
        return new HostNameBindingImpl(hostname, inner, this, client);
    }

    @Override
    public HostNameBindingImpl defineHostNameBinding(Domain domain, String hostname) {
        HostNameBindingInner inner = new HostNameBindingInner();
        inner.withSiteName(name());
        inner.withLocation(regionName());
        inner.withAzureResourceType(AzureResourceType.WEBSITE);
        inner.withAzureResourceName(name());
        inner.withDomainId(domain.id());
        return new HostNameBindingImpl(hostname, inner, this, client);
    }

    WebAppImpl withHostNameBinding(final HostNameBindingImpl hostNameBinding) {
        this.hostNameBinding = hostNameBinding;
        return this;
    }

    @Override
    public Observable<WebApp> createResourceAsync() {
        if (hostNameSslStateMap.size() > 0) {
            inner().withHostNameSslStates(new ArrayList<>(hostNameSslStateMap.values()));
        }
        return client.createOrUpdateAsync(resourceGroupName(), name(), inner())
                .map(innerToFluentMap(this))
                .flatMap(new Func1<WebApp, Observable<WebApp>>() {
                    @Override
                    public Observable<WebApp> call(final WebApp webApp) {
                        if (hostNameBinding != null) {
                            return hostNameBinding.createAsync()
                                    .map(new Func1<HostNameBinding, WebApp>() {
                                        @Override
                                        public WebApp call(HostNameBinding hostNameBinding) {
                                            return webApp;
                                        }
                                    });
                        } else {
                            return Observable.just(webApp);
                        }
                    }
                });
    }

    @Override
    public WebAppImpl withAppDisabledOnCreation() {
        inner().withEnabled(false);
        return this;
    }

    @Override
    public WebAppImpl withScmSiteAlsoStopped(boolean scmSiteAlsoStopped) {
        inner().withScmSiteAlsoStopped(scmSiteAlsoStopped);
        return this;
    }

    @Override
    public WebAppImpl withClientAffinityEnabled(boolean enabled) {
        inner().withClientAffinityEnabled(enabled);
        return this;
    }

    @Override
    public WebAppImpl withClientCertEnabled(boolean enabled) {
        inner().withClientCertEnabled(enabled);
        return this;
    }
}