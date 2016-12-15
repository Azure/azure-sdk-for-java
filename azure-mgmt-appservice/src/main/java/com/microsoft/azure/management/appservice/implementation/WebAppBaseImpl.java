/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.AppServiceCertificate;
import com.microsoft.azure.management.appservice.AppServiceDomain;
import com.microsoft.azure.management.appservice.AppSetting;
import com.microsoft.azure.management.appservice.AzureResourceType;
import com.microsoft.azure.management.appservice.CloningInfo;
import com.microsoft.azure.management.appservice.ConnStringValueTypePair;
import com.microsoft.azure.management.appservice.ConnectionString;
import com.microsoft.azure.management.appservice.ConnectionStringType;
import com.microsoft.azure.management.appservice.CustomHostNameDnsRecordType;
import com.microsoft.azure.management.appservice.HostNameBinding;
import com.microsoft.azure.management.appservice.HostNameSslState;
import com.microsoft.azure.management.appservice.HostNameType;
import com.microsoft.azure.management.appservice.JavaVersion;
import com.microsoft.azure.management.appservice.ManagedPipelineMode;
import com.microsoft.azure.management.appservice.NetFrameworkVersion;
import com.microsoft.azure.management.appservice.PhpVersion;
import com.microsoft.azure.management.appservice.PlatformArchitecture;
import com.microsoft.azure.management.appservice.PythonVersion;
import com.microsoft.azure.management.appservice.RemoteVisualStudioVersion;
import com.microsoft.azure.management.appservice.SiteAvailabilityState;
import com.microsoft.azure.management.appservice.SslState;
import com.microsoft.azure.management.appservice.UsageState;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azure.management.appservice.WebContainer;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func3;
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
 *  @param <FluentT> the fluent interface of the web app or deployment slot
 *  @param <FluentImplT> the fluent implementation of the web app or deployment slot
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
abstract class WebAppBaseImpl<
        FluentT extends WebAppBase,
        FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
        extends GroupableResourceImpl<
            FluentT,
            SiteInner,
            FluentImplT,
            AppServiceManager>
        implements
            WebAppBase,
            WebAppBase.Definition<FluentT>,
            WebAppBase.Update<FluentT>,
            WebAppBase.UpdateStages.WithWebContainer<FluentT> {

    final WebAppsInner client;
    final WebSiteManagementClientImpl serviceClient;
    Map<String, AppSetting> cachedAppSettings;
    Map<String, ConnectionString> cachedConnectionStrings;

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
    private WebAppSourceControlImpl<FluentT, FluentImplT> sourceControl;
    private boolean sourceControlToDelete;

    WebAppBaseImpl(String name, SiteInner innerObject, SiteConfigInner configObject, final WebAppsInner client, AppServiceManager manager, WebSiteManagementClientImpl serviceClient) {
        super(name, innerObject, manager);
        this.client = client;
        this.serviceClient = serviceClient;
        this.inner().withSiteConfig(configObject);
        normalizeProperties();
    }

    @SuppressWarnings("unchecked")
    private FluentT normalizeProperties() {
        this.hostNameBindingsToCreate = new HashMap<>();
        this.hostNameBindingsToDelete = new ArrayList<>();
        this.appSettingsToAdd = new HashMap<>();
        this.appSettingsToRemove = new ArrayList<>();
        this.appSettingStickiness = new HashMap<>();
        this.connectionStringsToAdd = new HashMap<>();
        this.connectionStringsToRemove = new ArrayList<>();
        this.connectionStringStickiness = new HashMap<>();
        this.sourceControl = null;
        this.sourceControlToDelete = false;
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
        return Utils.toPrimitiveBoolean(inner().premiumAppDeployed());
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
        return Utils.toPrimitiveBoolean(inner().hostNamesDisabled());
    }

    @Override
    public Set<String> outboundIpAddresses() {
        return Collections.unmodifiableSet(outboundIpAddressesSet);
    }

    @Override
    public int containerSize() {
        return Utils.toPrimitiveInt(inner().containerSize());
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
        if (inner().siteConfig() == null || inner().siteConfig().phpVersion() == null) {
            return PhpVersion.OFF;
        }
        return new PhpVersion(inner().siteConfig().phpVersion());
    }

    @Override
    public PythonVersion pythonVersion() {
        if (inner().siteConfig() == null || inner().siteConfig().pythonVersion() == null) {
            return PythonVersion.OFF;
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
        if (inner().siteConfig() == null || inner().siteConfig().javaVersion() == null) {
            return JavaVersion.OFF;
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
    public Map<String, AppSetting> appSettings() {
        return cachedAppSettings;
    }

    @Override
    public Map<String, ConnectionString> connectionStrings() {
        return cachedConnectionStrings;
    }

    @SuppressWarnings("unchecked")
    Observable<FluentT> cacheAppSettingsAndConnectionStrings() {
        final FluentT self = (FluentT) this;
        return Observable.zip(listAppSettings(), listConnectionStrings(), listSlotConfigurations(), new Func3<StringDictionaryInner, ConnectionStringDictionaryInner, SlotConfigNamesResourceInner, FluentT>() {
            @Override
            public FluentT call(final StringDictionaryInner appSettingsInner, final ConnectionStringDictionaryInner connectionStringsInner, final SlotConfigNamesResourceInner slotConfigs) {
                cachedAppSettings = new HashMap<>();
                cachedConnectionStrings = new HashMap<>();
                if (appSettingsInner != null && appSettingsInner.properties() != null) {
                    cachedAppSettings = Maps.asMap(appSettingsInner.properties().keySet(), new Function<String, AppSetting>() {
                        @Override
                        public AppSetting apply(String input) {
                            return new AppSettingImpl(input, appSettingsInner.properties().get(input),
                                    slotConfigs.appSettingNames() != null && slotConfigs.appSettingNames().contains(input));
                        }
                    });
                }
                if (connectionStringsInner != null && connectionStringsInner.properties() != null) {
                    cachedConnectionStrings = Maps.asMap(connectionStringsInner.properties().keySet(), new Function<String, ConnectionString>() {
                        @Override
                        public ConnectionString apply(String input) {
                            return new ConnectionStringImpl(input, connectionStringsInner.properties().get(input),
                                    slotConfigs.connectionStringNames() != null && slotConfigs.connectionStringNames().contains(input));
                        }
                    });
                }
                return self;
            }
        });
    }

    abstract Observable<SiteInner> createOrUpdateInner(SiteInner site);

    abstract Observable<SiteInner> getInner();

    abstract Observable<SiteConfigInner> getConfigInner();

    abstract Observable<SiteConfigInner> createOrUpdateSiteConfig(SiteConfigInner siteConfig);

    abstract Observable<Void> deleteHostNameBinding(String hostname);

    abstract Observable<StringDictionaryInner> listAppSettings();

    abstract Observable<StringDictionaryInner> updateAppSettings(StringDictionaryInner inner);

    abstract Observable<ConnectionStringDictionaryInner> listConnectionStrings();

    abstract Observable<ConnectionStringDictionaryInner> updateConnectionStrings(ConnectionStringDictionaryInner inner);

    abstract Observable<SlotConfigNamesResourceInner> listSlotConfigurations();

    abstract Observable<SlotConfigNamesResourceInner> updateSlotConfigurations(SlotConfigNamesResourceInner inner);

    abstract Observable<SiteSourceControlInner> createOrUpdateSourceControl(SiteSourceControlInner inner);

    abstract Observable<Void> deleteSourceControl();

    @Override
    public Observable<FluentT> createResourceAsync() {
        if (hostNameSslStateMap.size() > 0) {
            inner().withHostNameSslStates(new ArrayList<>(hostNameSslStateMap.values()));
        }
        final boolean emptyConfig = inner().siteConfig() == null;
        if (emptyConfig) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        inner().siteConfig().withLocation(inner().location());
        // Construct web app observable
        return createOrUpdateInner(inner())
        .map(new Func1<SiteInner, SiteInner>() {
            @Override
            public SiteInner call(SiteInner siteInner) {
                if (emptyConfig) {
                    inner().withSiteConfig(null);
                }
                return siteInner;
            }
        })
        // Submit hostname bindings
        .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
            @Override
            public Observable<SiteInner> call(final SiteInner site) {
                List<Observable<HostNameBinding>> bindingObservables = new ArrayList<>();
                for (HostNameBindingImpl<FluentT, FluentImplT> binding: hostNameBindingsToCreate.values()) {
                    bindingObservables.add(Utils.<HostNameBinding>rootResource(binding.createAsync()));
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
        // Submit SSL bindings
        .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
            @Override
            public Observable<SiteInner> call(final SiteInner siteInner) {
                List<Observable<AppServiceCertificate>> certs = new ArrayList<>();
                for (final HostNameSslBindingImpl<FluentT, FluentImplT> binding : sslBindingsToCreate.values()) {
                    certs.add(binding.newCertificate());
                    hostNameSslStateMap.put(binding.inner().name(), binding.inner().withToUpdate(true));
                }
                siteInner.withHostNameSslStates(new ArrayList<>(hostNameSslStateMap.values()));
                if (certs.isEmpty()) {
                    return Observable.just(siteInner);
                } else {
                    return Observable.zip(certs, new FuncN<SiteInner>() {
                        @Override
                        public SiteInner call(Object... args) {
                            return siteInner;
                        }
                    }).flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
                        @Override
                        public Observable<SiteInner> call(SiteInner inner) {
                            return createOrUpdateInner(inner);
                        }
                    });
                }
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
        // create source control
        .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
            @Override
            public Observable<SiteInner> call(final SiteInner inner) {
                if (sourceControl == null || sourceControlToDelete) {
                    return Observable.just(inner);
                }
                return sourceControl.registerGithubAccessToken()
                .flatMap(new Func1<SourceControlInner, Observable<SiteSourceControlInner>>() {
                    @Override
                    public Observable<SiteSourceControlInner> call(SourceControlInner sourceControlInner) {
                        return createOrUpdateSourceControl(sourceControl.inner());
                    }
                })
                .map(new Func1<SiteSourceControlInner, SiteInner>() {
                    @Override
                    public SiteInner call(SiteSourceControlInner siteSourceControlInner) {
                        return inner;
                    }
                });
            }
        })
        // delete source control
        .flatMap(new Func1<SiteInner, Observable<SiteInner>>() {
            @Override
            public Observable<SiteInner> call(final SiteInner inner) {
                if (!sourceControlToDelete) {
                    return Observable.just(inner);
                }
                return deleteSourceControl().map(new Func1<Void, SiteInner>() {
                    @Override
                    public SiteInner call(Void aVoid) {
                        return inner;
                    }
                });
            }
        })
        // convert from inner
        .map(new Func1<SiteInner, FluentT>() {
            @Override
            public FluentT call(SiteInner siteInner) {
                setInner(siteInner);
                return normalizeProperties();
            }
        }).flatMap(new Func1<FluentT, Observable<FluentT>>() {
            @Override
            public Observable<FluentT> call(FluentT fluentT) {
                return cacheAppSettingsAndConnectionStrings();
            }
        });
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
        for (String hostname : hostnames) {
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
        for (String hostname : hostnames) {
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
    public FluentImplT withoutPhp() {
        return withPhpVersion(new PhpVersion(""));
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
    public FluentImplT withoutJava() {
        return withJavaVersion(new JavaVersion("")).withWebContainer(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withWebContainer(WebContainer webContainer) {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        if (webContainer == null) {
            inner().siteConfig().withJavaContainer(null);
            inner().siteConfig().withJavaContainerVersion(null);
        } else {
            String[] containerInfo = webContainer.toString().split(" ");
            inner().siteConfig().withJavaContainer(containerInfo[0]);
            inner().siteConfig().withJavaContainerVersion(containerInfo[1]);
        }
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
    public FluentImplT withoutPython() {
        return withPythonVersion(new PythonVersion(""));
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

    @SuppressWarnings("unchecked")
    FluentImplT withSourceControl(WebAppSourceControlImpl<FluentT, FluentImplT> sourceControl) {
        this.sourceControl = sourceControl;
        return (FluentImplT) this;
    }

    @Override
    public WebAppSourceControlImpl<FluentT, FluentImplT> defineSourceControl() {
        SiteSourceControlInner sourceControlInner = new SiteSourceControlInner();
        sourceControlInner.withLocation(regionName());
        return new WebAppSourceControlImpl<>(sourceControlInner, this, serviceClient);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withLocalGitSourceControl() {
        if (inner().siteConfig() == null) {
            inner().withSiteConfig(new SiteConfigInner());
        }
        inner().siteConfig().withScmType("LocalGit");
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withoutSourceControl() {
        sourceControlToDelete = true;
        return (FluentImplT) this;
    }

    @Override
    public FluentT refresh() {
        SiteInner inner = getInner().toBlocking().single();
        inner.withSiteConfig(getConfigInner().toBlocking().single());
        setInner(inner);
        return this.cacheAppSettingsAndConnectionStrings().toBlocking().single();
    }
}