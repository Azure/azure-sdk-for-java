/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.AppServicePricingTier;
import com.microsoft.azure.management.website.AzureResourceType;
import com.microsoft.azure.management.website.CloningInfo;
import com.microsoft.azure.management.website.CustomHostNameDnsRecordType;
import com.microsoft.azure.management.website.Domain;
import com.microsoft.azure.management.website.HostNameBinding;
import com.microsoft.azure.management.website.HostNameSslState;
import com.microsoft.azure.management.website.HostNameType;
import com.microsoft.azure.management.website.SiteAvailabilityState;
import com.microsoft.azure.management.website.SslState;
import com.microsoft.azure.management.website.UsageState;
import com.microsoft.azure.management.website.WebApp;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.FuncN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Map<String, HostNameBindingImpl> hostNameBindingsToCreate;
    private Set<String> enabledHostNamesSet;

    WebAppImpl(String key, SiteInner innerObject, final WebAppsInner client, AppServiceManager manager) {
        super(key, innerObject, manager);
        this.client = client;
        this.hostNameSslStateMap = new HashMap<>();
        this.hostNameBindingsToCreate = new HashMap<>();
        if (inner().enabledHostNames() != null) {
            this.enabledHostNamesSet = Sets.newHashSet(inner().enabledHostNames());
        }
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
    public Set<String> enabledHostNames() {
        if (enabledHostNamesSet == null) {
            return null;
        }
        return Collections.unmodifiableSet(enabledHostNamesSet);
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
    public Map<String, HostNameBinding> getHostNameBindings() {
        List<HostNameBindingInner> collectionInner = client.listHostNameBindings(resourceGroupName(), name());
        List<HostNameBinding> hostNameBindings = new ArrayList<>();
        for (HostNameBindingInner inner : collectionInner) {
            hostNameBindings.add(new HostNameBindingImpl(inner.name(), inner, this, client));
        }
        return Maps.uniqueIndex(hostNameBindings, new Function<HostNameBinding, String>() {
            @Override
            public String apply(HostNameBinding input) {
                return input.name().replaceAll("./", "");
            }
        });
    }

    @Override
    public WebAppImpl refresh() {
        this.setInner(client.get(resourceGroupName(), name()));
        return this;
    }

    @Override
    public WebAppImpl withNewFreeAppServicePlan() {
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

//    private boolean isUpdateSsl(String hostName) {
//        boolean update = false;
//        if (hostNameSslStates() != null) {
//            for (HostNameSslState hostNameSslState : hostNameSslStates()) {
//                if (hostName.equals(hostNameSslState.name())) {
//                    update = true;
//                }
//            }
//        }
//        return update;
//    }
//
//    @Override
//    public WebAppImpl disableSsl(String hostName) {
//        if (hostName == null) {
//            throw new IllegalArgumentException("Null host name");
//        }
//        hostNameSslStateMap.put(hostName, new HostNameSslState()
//                .withName(hostName)
//                .withSslState(SslState.DISABLED)
//                .withToUpdate(isUpdateSsl(hostName)));
//        return this;
//    }
//
//    @Override
//    public WebAppImpl enableSniSsl(String hostName, String thumbprint) {
//        if (hostName == null) {
//            throw new IllegalArgumentException("Null host name");
//        }
//        hostNameSslStateMap.put(hostName, new HostNameSslState()
//                .withName(hostName)
//                .withSslState(SslState.SNI_ENABLED)
//                .withThumbprint(thumbprint)
//                .withToUpdate(isUpdateSsl(hostName)));
//        return this;
//    }
//
//    @Override
//    public WebAppImpl enableIpBasedSsl(String hostName, String thumbprint, String virtualIp) {
//        if (hostName == null) {
//            throw new IllegalArgumentException("Null host name");
//        }
//        hostNameSslStateMap.put(hostName, new HostNameSslState()
//                .withName(hostName)
//                .withSslState(SslState.SNI_ENABLED)
//                .withThumbprint(thumbprint)
//                .withVirtualIP(virtualIp)
//                .withToUpdate(isUpdateSsl(hostName)));
//        return this;
//    }

//    private String normalizeHostNameBindingName(String hostname, String domainName) {
//        if (!hostname.endsWith(domainName)) {
//            hostname = hostname + "." + domainName;
//        }
//        if (hostname.startsWith("@")) {
//            hostname = hostname.replace("@.", "");
//        }
//        return hostname;
//    }

    @Override
    public WebAppImpl withManagedHostNameBindings(Domain domain, String... hostnames) {
        for(String hostname : hostnames) {
            if (hostname.equals("@") || hostname.equalsIgnoreCase(domain.name())) {
                defineNewHostNameBinding(hostname)
                        .withAzureManagedDomain(domain)
                        .withDnsRecordType(CustomHostNameDnsRecordType.A)
                        .attach();
            } else {
                defineNewHostNameBinding(hostname)
                        .withAzureManagedDomain(domain)
                        .withDnsRecordType(CustomHostNameDnsRecordType.CNAME)
                        .attach();
            }
        }
        return this;
    }

    @Override
    public HostNameBindingImpl defineNewHostNameBinding(String hostname) {
        HostNameBindingInner inner = new HostNameBindingInner();
        inner.withSiteName(name());
        inner.withLocation(regionName());
        inner.withAzureResourceType(AzureResourceType.WEBSITE);
        inner.withAzureResourceName(name());
        inner.withHostNameType(HostNameType.VERIFIED);
        return new HostNameBindingImpl(hostname, inner, this, client);
    }

    @Override
    public WebAppImpl withVerifiedHostNameBinding(String domain, String... hostnames) {
        for(String hostname : hostnames) {
            defineNewHostNameBinding(hostname)
                    .withThirdPartyDomain(domain)
                    .withDnsRecordType(CustomHostNameDnsRecordType.CNAME)
                    .attach();
        }
        return this;
    }

    WebAppImpl withHostNameBinding(final HostNameBindingImpl hostNameBinding) {
        this.hostNameBindingsToCreate.put(
                hostNameBinding.name(),
                hostNameBinding);
        return this;
    }

    @Override
    public Observable<WebApp> createResourceAsync() {
        if (hostNameSslStateMap.size() > 0) {
            inner().withHostNameSslStates(new ArrayList<>(hostNameSslStateMap.values()));
        }
        // Construct web app observable
        return client.createOrUpdateAsync(resourceGroupName(), name(), inner())
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(final SiteInner site) {
                        List<Observable<HostNameBinding>> bindingObservables = new ArrayList<>();
                        for (HostNameBindingImpl binding: hostNameBindingsToCreate.values()) {
                            bindingObservables.add(binding.createAsync());
                        }
                        hostNameBindingsToCreate.clear();
                        if (bindingObservables.isEmpty()) {
                            return Observable.just(site);
                        } else {
                            return Observable.zip(bindingObservables, new FuncN<SiteInner>() {
                                @Override
                                public SiteInner call(Object... args) {
                                    return site;
                                }
                            });
                        }
                    }
                })
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(SiteInner site) {
                        return client.getAsync(resourceGroupName(), site.name());
                    }
                })
                .map(innerToFluentMap(this));
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

//    private static class DomainInfo {
//        private String externalDomain;
//        private String domainId;
//        private Creatable<Domain> domainCreatable;
//
//        private DomainInfo() {
//        }
//
//        static DomainInfo existingDomain(String domainId) {
//            DomainInfo info = new DomainInfo();
//            info.domainId = domainId;
//            return info;
//        }
//
//        static DomainInfo newDomain(Creatable<Domain> domainCreatable) {
//            DomainInfo info = new DomainInfo();
//            info.domainCreatable = domainCreatable;
//            return info;
//        }
//
//        static DomainInfo thirdPartyDomain(String externalDomain) {
//            DomainInfo info = new DomainInfo();
//            info.externalDomain = externalDomain;
//            return info;
//        }
//
//        boolean isNewDomain() {
//            return domainCreatable != null;
//        }
//
//        boolean isAzureDomain() {
//            return externalDomain == null;
//        }
//
//        String name() {
//            if (isNewDomain()) {
//                return domainCreatable.name();
//            } else if (isAzureDomain()) {
//                return ResourceUtils.nameFromResourceId(domainId);
//            } else {
//                return externalDomain;
//            }
//        }
//
//        void addToMap(final Map<String, DomainInfo> domainMap) {
//            domainMap.put(name(), this);
//        }
//    }
}