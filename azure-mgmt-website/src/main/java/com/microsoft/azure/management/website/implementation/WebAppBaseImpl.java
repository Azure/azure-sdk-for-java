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
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.website.AppServiceCertificate;
import com.microsoft.azure.management.website.AppServiceDomain;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.AppServicePricingTier;
import com.microsoft.azure.management.website.AppSetting;
import com.microsoft.azure.management.website.AzureResourceType;
import com.microsoft.azure.management.website.CloningInfo;
import com.microsoft.azure.management.website.ConnStringValueTypePair;
import com.microsoft.azure.management.website.ConnectionString;
import com.microsoft.azure.management.website.ConnectionStringType;
import com.microsoft.azure.management.website.CustomHostNameDnsRecordType;
import com.microsoft.azure.management.website.HostNameBinding;
import com.microsoft.azure.management.website.HostNameSslState;
import com.microsoft.azure.management.website.HostNameType;
import com.microsoft.azure.management.website.JavaVersion;
import com.microsoft.azure.management.website.ManagedPipelineMode;
import com.microsoft.azure.management.website.NetFrameworkVersion;
import com.microsoft.azure.management.website.PhpVersion;
import com.microsoft.azure.management.website.PlatformArchitecture;
import com.microsoft.azure.management.website.PythonVersion;
import com.microsoft.azure.management.website.RemoteVisualStudioVersion;
import com.microsoft.azure.management.website.SiteAvailabilityState;
import com.microsoft.azure.management.website.SslState;
import com.microsoft.azure.management.website.UsageState;
import com.microsoft.azure.management.website.WebAppBase;
import com.microsoft.azure.management.website.WebContainer;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.FuncN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The implementation for {@link WebAppBase}.
 */
abstract class WebAppBaseImpl<
        FluentT extends WebAppBase<FluentT>,
        FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
        extends GroupableResourceImpl<
            FluentT,
            SiteInner,
            FluentImplT,
            AppServiceManager>
        implements
            WebAppBase<FluentT>,
            WebAppBase.Definition<FluentT>,
            WebAppBase.Update<FluentT> {

    final WebAppsInner client;

    private Set<String> hostNamesSet;
    private Set<String> enabledHostNamesSet;
    private Set<String> trafficManagerHostNamesSet;
    private Set<String> outboundIpAddressesSet;
    private Map<String, HostNameSslState> hostNameSslStateMap;
    private Map<String, HostNameBindingImpl<FluentT, FluentImplT>> hostNameBindingsToCreate;
    private List<String> hostNameBindingsToDelete;
    private Map<String, HostNameSslBindingImpl<FluentT, FluentImplT>> sslBindingsToCreate;

    private Map<String, String> appSettingsToAdd;
    private List<String> appSettingsToRemove;
    private Map<String, Boolean> appSettingStickiness;
    private Map<String, ConnStringValueTypePair> connectionStringsToAdd;
    private List<String> connectionStringsToRemove;
    private Map<String, Boolean> connectionStringStickiness;

    WebAppBaseImpl(String name, SiteInner innerObject, SiteConfigInner configObject, final WebAppsInner client, AppServiceManager manager) {
        super(name, innerObject, manager);
        this.client = client;
        this.inner().withSiteConfig(configObject);
        normalizeProperties();
    }

    @SuppressWarnings("unchecked")
    private FluentT normalizeProperties() {
        this.hostNameBindingsToCreate = new HashMap<>();
        this.hostNameBindingsToDelete = new ArrayList<>();
        appSettingsToAdd = new HashMap<>();
        appSettingsToRemove = new ArrayList<>();
        appSettingStickiness = new HashMap<>();
        connectionStringsToAdd = new HashMap<>();
        connectionStringsToRemove = new ArrayList<>();
        connectionStringStickiness = new HashMap<>();
        this.sslBindingsToCreate = new HashMap<>();
        if (inner().hostNames() != null) {
            this.hostNamesSet = Sets.newHashSet(inner().hostNames());
        }
        if (inner().enabledHostNames() != null) {
            this.enabledHostNamesSet = Sets.newHashSet(inner().enabledHostNames());
        }
        if (inner().trafficManagerHostNames() != null) {
            this.trafficManagerHostNamesSet = Sets.newHashSet(inner().trafficManagerHostNames());
        }
        if (inner().outboundIpAddresses() != null) {
            this.outboundIpAddressesSet = Sets.newHashSet(inner().outboundIpAddresses().split(",[ ]*"));
        }
        this.hostNameSslStateMap = new HashMap<>();
        if (inner().hostNameSslStates() != null) {
            for (HostNameSslState hostNameSslState : inner().hostNameSslStates()) {
                // Server returns null sometimes, invalid on update, so we set default
                if (hostNameSslState.sslState() == null) {
                    hostNameSslState.withSslState(SslState.DISABLED);
                }
                hostNameSslStateMap.put(hostNameSslState.name(), hostNameSslState);
            }
        }
        return (FluentT) this;
    }

    @Override
    public String state() {
        return inner().state();
    }

    @Override
    public Set<String> hostNames() {
        return Collections.unmodifiableSet(hostNamesSet);
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
    public Map<String, HostNameSslState> hostNameSslStates() {
        return Collections.unmodifiableMap(hostNameSslStateMap);
    }

    @Override
    public String appServicePlanId() {
        return inner().serverFarmId();
    }

    @Override
    public DateTime lastModifiedTime() {
        return inner().lastModifiedTimeUtc();
    }

    @Override
    public Set<String> trafficManagerHostNames() {
        return Collections.unmodifiableSet(trafficManagerHostNamesSet);
    }

    @Override
    public boolean isPremiumApp() {
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
    public Set<String> outboundIpAddresses() {
        return Collections.unmodifiableSet(outboundIpAddressesSet);
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
    public List<String> defaultDocuments() {
        if (inner().siteConfig() == null) {
            return null;
        }
        return Collections.unmodifiableList(inner().siteConfig().defaultDocuments());
    }

    @Override
    public NetFrameworkVersion netFrameworkVersion() {
        if (inner().siteConfig() == null) {
            return null;
        }
        return new NetFrameworkVersion(inner().siteConfig().netFrameworkVersion());
    }

    @Override
    public PhpVersion phpVersion() {
        if (inner().siteConfig() == null) {
            return null;
        }
        return new PhpVersion(inner().siteConfig().phpVersion());
    }

    @Override
    public PythonVersion pythonVersion() {
        if (inner().siteConfig() == null) {
            return null;
        }
        return new PythonVersion(inner().siteConfig().pythonVersion());
    }

    @Override
    public String nodeVersion() {
        if (inner().siteConfig() == null) {
            return null;
        }
        return inner().siteConfig().nodeVersion();
    }

    @Override
    public boolean remoteDebuggingEnabled() {
        if (inner().siteConfig() == null) {
            return false;
        }
        return Utils.toPrimitiveBoolean(inner().siteConfig().remoteDebuggingEnabled());
    }

    @Override
    public RemoteVisualStudioVersion remoteDebuggingVersion() {
        if (inner().siteConfig() == null) {
            return null;
        }
        return new RemoteVisualStudioVersion(inner().siteConfig().remoteDebuggingVersion());
    }

    @Override
    public boolean webSocketsEnabled() {
        if (inner().siteConfig() == null) {
            return false;
        }
        return Utils.toPrimitiveBoolean(inner().siteConfig().webSocketsEnabled());
    }

    @Override
    public boolean alwaysOn() {
        if (inner().siteConfig() == null) {
            return false;
        }
        return Utils.toPrimitiveBoolean(inner().siteConfig().alwaysOn());
    }

    @Override
    public JavaVersion javaVersion() {
        if (inner().siteConfig() == null) {
            return null;
        }
        return new JavaVersion(inner().siteConfig().javaVersion());
    }

    @Override
    public String javaContainer() {
        if (inner().siteConfig() == null) {
            return null;
        }
        return inner().siteConfig().javaContainer();
    }

    @Override
    public String javaContainerVersion() {
        if (inner().siteConfig() == null) {
            return null;
        }
        return inner().siteConfig().javaContainerVersion();
    }

    @Override
    public ManagedPipelineMode managedPipelineMode() {
        if (inner().siteConfig() == null) {
            return null;
        }
        return inner().siteConfig().managedPipelineMode();
    }

    @Override
    public String autoSwapSlotName() {
        if (inner().siteConfig() == null) {
            return null;
        }
        return inner().siteConfig().autoSwapSlotName();
    }

    @Override
    public Map<String, AppSetting> getAppSettings() {
        final StringDictionaryInner inner = listAppSettings().toBlocking().single();
        final SlotConfigNamesResourceInner slotConfigs = listSlotConfigurations().toBlocking().single();
        if (inner == null || inner.properties() == null) {
            return null;
        }
        return Maps.asMap(inner.properties().keySet(), new Function<String, AppSetting>() {
            @Override
            public AppSetting apply(String input) {
                return new AppSettingImpl(input, inner.properties().get(input), slotConfigs.appSettingNames().contains(input));
            }
        });
    }

    @Override
    public Map<String, ConnectionString> getConnectionStrings() {
        final ConnectionStringDictionaryInner inner = listConnectionStrings().toBlocking().single();
        final SlotConfigNamesResourceInner slotConfigs = listSlotConfigurations().toBlocking().single();
        if (inner == null || inner.properties() == null) {
            return null;
        }
        return Maps.asMap(inner.properties().keySet(), new Function<String, ConnectionString>() {
            @Override
            public ConnectionString apply(String input) {
                return new ConnectionStringImpl(input, inner.properties().get(input), slotConfigs.connectionStringNames().contains(input));
            }
        });
    }

    abstract Observable<SiteInner> createOrUpdateInner(SiteInner site);

    abstract Observable<SiteInner> getInner();

    abstract Observable<SiteConfigInner> createOrUpdateSiteConfig(SiteConfigInner siteConfig);

    abstract Observable<Object> deleteHostNameBinding(String hostname);

    abstract Observable<StringDictionaryInner> listAppSettings();

    abstract Observable<StringDictionaryInner> updateAppSettings(StringDictionaryInner inner);

    abstract Observable<ConnectionStringDictionaryInner> listConnectionStrings();

    abstract Observable<ConnectionStringDictionaryInner> updateConnectionStrings(ConnectionStringDictionaryInner inner);

    abstract Observable<SlotConfigNamesResourceInner> listSlotConfigurations();

    abstract Observable<SlotConfigNamesResourceInner> updateSlotConfigurations(SlotConfigNamesResourceInner inner);

    @Override
    public Observable<FluentT> createResourceAsync() {
        if (hostNameSslStateMap.size() > 0) {
            inner().withHostNameSslStates(new ArrayList<>(hostNameSslStateMap.values()));
        }
        if (inner().siteConfig() != null & inner().siteConfig().location() == null) {
            inner().siteConfig().withLocation(inner().location());
        }
        // Construct web app observable
        return createOrUpdateInner(inner())
                // Submit hostname bindings
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(final SiteInner site) {
                        List<Observable<HostNameBinding>> bindingObservables = new ArrayList<>();
                        for (HostNameBindingImpl<FluentT, FluentImplT> binding: hostNameBindingsToCreate.values()) {
                            bindingObservables.add(binding.createAsync());
                        }
                        for (String binding: hostNameBindingsToDelete) {
                            bindingObservables.add(deleteHostNameBinding(binding).map(new Func1<Object, HostNameBinding>() {
                                @Override
                                public HostNameBinding call(Object o) {
                                    return null;
                                }
                            }));
                        }
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
                // refresh after hostname bindings
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(SiteInner site) {
                        return getInner();
                    }
                })
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(final SiteInner siteInner) {
                        List<Observable<AppServiceCertificate>> certs = new ArrayList<>();
                        for (final HostNameSslBindingImpl<FluentT, FluentImplT> binding : sslBindingsToCreate.values()) {
                            certs.add(binding.newCertificate());
                            siteInner.hostNameSslStates().add(binding.inner().withToUpdate(true));
                        }
                        if (certs.isEmpty()) {
                            return Observable.just(siteInner);
                        } else {
                            return Observable.zip(certs, new FuncN<SiteInner>() {
                                @Override
                                public SiteInner call(Object... args) {
                                    return siteInner;
                                }
                            });
                        }
                    }
                })
                // refresh after hostname SSL bindings
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(SiteInner site) {
                        return createOrUpdateInner(inner());
                    }
                })
                // submit config
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(final SiteInner siteInner) {
                        if (inner().siteConfig() == null) {
                            return Observable.just(siteInner);
                        }
                        return createOrUpdateSiteConfig(inner().siteConfig())
                                .flatMap(new Func1<SiteConfigInner, Observable<SiteInner>>() {
                                    @Override
                                    public Observable<SiteInner> call(SiteConfigInner siteConfigInner) {
                                        siteInner.withSiteConfig(siteConfigInner);
                                        return Observable.just(siteInner);
                                    }
                                });
                    }
                })
                // app settings
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(final SiteInner inner) {
                        Observable<SiteInner> observable = Observable.just(inner);
                        if (!appSettingsToAdd.isEmpty() || !appSettingsToRemove.isEmpty()) {
                            observable = listAppSettings()
                                    .flatMap(new Func1<StringDictionaryInner, Observable<StringDictionaryInner>>() {
                                        @Override
                                        public Observable<StringDictionaryInner> call(StringDictionaryInner stringDictionaryInner) {
                                            if (stringDictionaryInner == null) {
                                                stringDictionaryInner = new StringDictionaryInner();
                                                stringDictionaryInner.withLocation(regionName());
                                            }
                                            if (stringDictionaryInner.properties() == null) {
                                                stringDictionaryInner.withProperties(new HashMap<String, String>());
                                            }
                                            stringDictionaryInner.properties().putAll(appSettingsToAdd);
                                            for (String appSettingKey : appSettingsToRemove) {
                                                stringDictionaryInner.properties().remove(appSettingKey);
                                            }
                                            return updateAppSettings(stringDictionaryInner);
                                        }
                                    }).map(new Func1<StringDictionaryInner, SiteInner>() {
                                        @Override
                                        public SiteInner call(StringDictionaryInner stringDictionaryInner) {
                                            return inner;
                                        }
                                    });
                        }
                        return observable;
                    }
                })
                // connection strings
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(final SiteInner inner) {
                        Observable<SiteInner> observable = Observable.just(inner);
                        if (!connectionStringsToAdd.isEmpty() || !connectionStringsToRemove.isEmpty()) {
                            observable = listConnectionStrings()
                                    .flatMap(new Func1<ConnectionStringDictionaryInner, Observable<ConnectionStringDictionaryInner>>() {
                                        @Override
                                        public Observable<ConnectionStringDictionaryInner> call(ConnectionStringDictionaryInner dictionaryInner) {
                                            if (dictionaryInner == null) {
                                                dictionaryInner = new ConnectionStringDictionaryInner();
                                                dictionaryInner.withLocation(regionName());
                                            }
                                            if (dictionaryInner.properties() == null) {
                                                dictionaryInner.withProperties(new HashMap<String, ConnStringValueTypePair>());
                                            }
                                            dictionaryInner.properties().putAll(connectionStringsToAdd);
                                            for (String connectionString : connectionStringsToRemove) {
                                                dictionaryInner.properties().remove(connectionString);
                                            }
                                            return updateConnectionStrings(dictionaryInner);
                                        }
                                    }).map(new Func1<ConnectionStringDictionaryInner, SiteInner>() {
                                        @Override
                                        public SiteInner call(ConnectionStringDictionaryInner stringDictionaryInner) {
                                            return inner;
                                        }
                                    });
                        }
                        return observable;
                    }
                })
                // app setting & connection string stickiness
                .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                    @Override
                    public Observable<SiteInner> call(final SiteInner inner) {
                        Observable<SiteInner> observable = Observable.just(inner);
                        if (!appSettingStickiness.isEmpty() || !connectionStringStickiness.isEmpty()) {
                            observable = listSlotConfigurations()
                                    .flatMap(new Func1<SlotConfigNamesResourceInner, Observable<SlotConfigNamesResourceInner>>() {
                                        @Override
                                        public Observable<SlotConfigNamesResourceInner> call(SlotConfigNamesResourceInner slotConfigNamesResourceInner) {
                                            if (slotConfigNamesResourceInner == null) {
                                                slotConfigNamesResourceInner = new SlotConfigNamesResourceInner();
                                                slotConfigNamesResourceInner.withLocation(regionName());
                                            }
                                            if (slotConfigNamesResourceInner.appSettingNames() == null) {
                                                slotConfigNamesResourceInner.withAppSettingNames(new ArrayList<String>());
                                            }
                                            if (slotConfigNamesResourceInner.connectionStringNames() == null) {
                                                slotConfigNamesResourceInner.withConnectionStringNames(new ArrayList<String>());
                                            }
                                            Set<String> stickyAppSettingKeys = new HashSet<>(slotConfigNamesResourceInner.appSettingNames());
                                            Set<String> stickyConnectionStringNames = new HashSet<>(slotConfigNamesResourceInner.connectionStringNames());
                                            for (Map.Entry<String, Boolean> stickiness : appSettingStickiness.entrySet()) {
                                                if (stickiness.getValue()) {
                                                    stickyAppSettingKeys.add(stickiness.getKey());
                                                } else {
                                                    stickyAppSettingKeys.remove(stickiness.getKey());
                                                }
                                            }
                                            for (Map.Entry<String, Boolean> stickiness : connectionStringStickiness.entrySet()) {
                                                if (stickiness.getValue()) {
                                                    stickyConnectionStringNames.add(stickiness.getKey());
                                                } else {
                                                    stickyConnectionStringNames.remove(stickiness.getKey());
                                                }
                                            }
                                            slotConfigNamesResourceInner.withAppSettingNames(new ArrayList<>(stickyAppSettingKeys));
                                            slotConfigNamesResourceInner.withConnectionStringNames(new ArrayList<>(stickyConnectionStringNames));
                                            return updateSlotConfigurations(slotConfigNamesResourceInner);
                                        }
                                    }).map(new Func1<SlotConfigNamesResourceInner, SiteInner>() {
                                        @Override
                                        public SiteInner call(SlotConfigNamesResourceInner slotConfigNamesResourceInner) {
                                            return inner;
                                        }
                                    });
                        }
                        return observable;
                    }
                })
                // convert from inner
                .map(new Func1<SiteInner, FluentT>() {
                    @Override
                    public FluentT call(SiteInner siteInner) {
                        setInner(siteInner);
                        return normalizeProperties();
                    }
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withNewFreeAppServicePlan() {
        String appServicePlanName = ResourceNamer.randomResourceName(name(), 10);
        AppServicePlan.DefinitionStages.WithCreate creatable = myManager.appServicePlans().define(appServicePlanName)
                .withRegion(region())
                .withNewResourceGroup(resourceGroupName())
                .withPricingTier(AppServicePricingTier.FREE_F1);
        addCreatableDependency(creatable);
        inner().withServerFarmId(appServicePlanName);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withNewAppServicePlan(String name, AppServicePricingTier pricingTier) {
        AppServicePlan.DefinitionStages.WithCreate creatable = myManager.appServicePlans().define(name)
                .withRegion(region())
                .withNewResourceGroup(resourceGroupName())
                .withPricingTier(pricingTier);
        addCreatableDependency(creatable);
        inner().withServerFarmId(name);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withExistingAppServicePlan(String appServicePlanName) {
        inner().withServerFarmId(appServicePlanName);
        return (FluentImplT) this;
    }

    WebAppBaseImpl<FluentT, FluentImplT> withNewHostNameSslBinding(final HostNameSslBindingImpl<FluentT, FluentImplT> hostNameSslBinding) {
        if (hostNameSslBinding.newCertificate() != null) {
            sslBindingsToCreate.put(hostNameSslBinding.name(), hostNameSslBinding);
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withManagedHostnameBindings(AppServiceDomain domain, String... hostnames) {
        for(String hostname : hostnames) {
            if (hostname.equals("@") || hostname.equalsIgnoreCase(domain.name())) {
                defineHostnameBinding()
                        .withAzureManagedDomain(domain)
                        .withSubDomain(hostname)
                        .withDnsRecordType(CustomHostNameDnsRecordType.A)
                        .attach();
            } else {
                defineHostnameBinding()
                        .withAzureManagedDomain(domain)
                        .withSubDomain(hostname)
                        .withDnsRecordType(CustomHostNameDnsRecordType.CNAME)
                        .attach();
            }
        }
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HostNameBindingImpl<FluentT, FluentImplT> defineHostnameBinding() {
        HostNameBindingInner inner = new HostNameBindingInner();
        inner.withSiteName(name());
        inner.withLocation(regionName());
        inner.withAzureResourceType(AzureResourceType.WEBSITE);
        inner.withAzureResourceName(name());
        inner.withHostNameType(HostNameType.VERIFIED);
        return new HostNameBindingImpl<>(inner, (FluentImplT) this, client);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withThirdPartyHostnameBinding(String domain, String... hostnames) {
        for(String hostname : hostnames) {
            defineHostnameBinding()
                    .withThirdPartyDomain(domain)
                    .withSubDomain(hostname)
                    .withDnsRecordType(CustomHostNameDnsRecordType.CNAME)
                    .attach();
        }
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withoutHostnameBinding(String hostname) {
        hostNameBindingsToDelete.add(hostname);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withoutSslBinding(String hostname) {
        if (hostNameSslStateMap.containsKey(hostname)) {
            hostNameSslStateMap.get(hostname).withSslState(SslState.DISABLED).withToUpdate(true);
        }
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    FluentImplT withHostNameBinding(final HostNameBindingImpl<FluentT, FluentImplT> hostNameBinding) {
        this.hostNameBindingsToCreate.put(
                hostNameBinding.name(),
                hostNameBinding);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withAppDisabledOnCreation() {
        inner().withEnabled(false);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withScmSiteAlsoStopped(boolean scmSiteAlsoStopped) {
        inner().withScmSiteAlsoStopped(scmSiteAlsoStopped);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withClientAffinityEnabled(boolean enabled) {
        inner().withClientAffinityEnabled(enabled);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withClientCertEnabled(boolean enabled) {
        inner().withClientCertEnabled(enabled);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HostNameSslBindingImpl<FluentT, FluentImplT> defineSslBinding() {
        return new HostNameSslBindingImpl<>(new HostNameSslState(), (FluentImplT) this, myManager);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withNetFrameworkVersion(NetFrameworkVersion version) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        inner().siteConfig().withNetFrameworkVersion(version.toString());
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withPhpVersion(PhpVersion version) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        inner().siteConfig().withPhpVersion(version.toString());
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withJavaVersion(JavaVersion version) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        inner().siteConfig().withJavaVersion(version.toString());
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withWebContainer(WebContainer webContainer) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        String[] containerInfo = webContainer.toString().split(" ");
        inner().siteConfig().withJavaContainer(containerInfo[0]);
        inner().siteConfig().withJavaContainerVersion(containerInfo[1]);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withPythonVersion(PythonVersion version) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        inner().siteConfig().withPythonVersion(version.toString());
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withPlatformArchitecture(PlatformArchitecture platform) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        inner().siteConfig().withUse32BitWorkerProcess(platform.equals(PlatformArchitecture.X86));
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withWebSocketsEnabled(boolean enabled) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        inner().siteConfig().withWebSocketsEnabled(enabled);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withWebAppAlwaysOn(boolean alwaysOn) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        inner().siteConfig().withAlwaysOn(alwaysOn);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withManagedPipelineMode(ManagedPipelineMode managedPipelineMode) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        inner().siteConfig().withManagedPipelineMode(managedPipelineMode);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withAutoSwapSlotName(String slotName) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        inner().siteConfig().withAutoSwapSlotName(slotName);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withRemoteDebuggingEnabled(RemoteVisualStudioVersion remoteVisualStudioVersion) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        inner().siteConfig().withRemoteDebuggingEnabled(true);
        inner().siteConfig().withRemoteDebuggingVersion(remoteVisualStudioVersion.toString());
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withRemoteDebuggingDisabled() {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        inner().siteConfig().withRemoteDebuggingEnabled(false);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withDefaultDocument(String document) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        if (inner().siteConfig().defaultDocuments() == null) {
            inner().siteConfig().withDefaultDocuments(new ArrayList<String>());
        }
        inner().siteConfig().defaultDocuments().add(document);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withDefaultDocuments(List<String> documents) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        if (inner().siteConfig().defaultDocuments() == null) {
            inner().siteConfig().withDefaultDocuments(new ArrayList<String>());
        }
        inner().siteConfig().defaultDocuments().addAll(documents);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withoutDefaultDocument(String document) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        if (inner().siteConfig().defaultDocuments() != null) {
            inner().siteConfig().defaultDocuments().remove(document);
        }
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withAppSetting(String key, String value) {
        appSettingsToAdd.put(key, value);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withAppSettings(Map<String, String> settings) {
        appSettingsToAdd.putAll(settings);
        return (FluentImplT) this;
    }

    @Override
    public FluentImplT withStickyAppSetting(String key, String value) {
        withAppSetting(key, value);
        return withAppSettingStickiness(key, true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withStickyAppSettings(Map<String, String> settings) {
        withAppSettings(settings);
        appSettingStickiness.putAll(Maps.asMap(settings.keySet(), new Function<String, Boolean>() {
            @Override
            public Boolean apply(String input) {
                return true;
            }
        }));
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withoutAppSetting(String key) {
        appSettingsToRemove.add(key);
        appSettingStickiness.remove(key);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withAppSettingStickiness(String key, boolean sticky) {
        appSettingStickiness.put(key, sticky);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withConnectionString(String name, String value, ConnectionStringType type) {
        connectionStringsToAdd.put(name, new ConnStringValueTypePair().withValue(value).withType(type));
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withStickyConnectionString(String name, String value, ConnectionStringType type) {
        connectionStringsToAdd.put(name, new ConnStringValueTypePair().withValue(value).withType(type));
        connectionStringStickiness.put(name, true);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withoutConnectionString(String name) {
        connectionStringsToRemove.add(name);
        connectionStringStickiness.remove(name);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withConnectionStringStickiness(String name, boolean stickiness) {
        connectionStringStickiness.put(name, stickiness);
        return (FluentImplT) this;
    }
}