// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.models.ConnectionStringDictionaryInner;
import com.azure.resourcemanager.appservice.fluent.models.HostnameBindingInner;
import com.azure.resourcemanager.appservice.fluent.models.MSDeployStatusInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteAuthSettingsInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteConfigInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteLogsConfigInner;
import com.azure.resourcemanager.appservice.fluent.models.SitePatchResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteSourceControlInner;
import com.azure.resourcemanager.appservice.fluent.models.SlotConfigNamesResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.StringDictionaryInner;
import com.azure.resourcemanager.appservice.models.AppServiceCertificate;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.appservice.models.AppSetting;
import com.azure.resourcemanager.appservice.models.AzureResourceType;
import com.azure.resourcemanager.appservice.models.CloningInfo;
import com.azure.resourcemanager.appservice.models.ConnStringValueTypePair;
import com.azure.resourcemanager.appservice.models.ConnectionString;
import com.azure.resourcemanager.appservice.models.ConnectionStringType;
import com.azure.resourcemanager.appservice.models.CustomHostnameDnsRecordType;
import com.azure.resourcemanager.appservice.models.FtpsState;
import com.azure.resourcemanager.appservice.models.HostingEnvironmentProfile;
import com.azure.resourcemanager.appservice.models.HostnameBinding;
import com.azure.resourcemanager.appservice.models.HostnameSslState;
import com.azure.resourcemanager.appservice.models.HostnameType;
import com.azure.resourcemanager.appservice.models.IpFilterTag;
import com.azure.resourcemanager.appservice.models.IpSecurityRestriction;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.MSDeploy;
import com.azure.resourcemanager.appservice.models.ManagedPipelineMode;
import com.azure.resourcemanager.appservice.models.ManagedServiceIdentity;
import com.azure.resourcemanager.appservice.models.NetFrameworkVersion;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PhpVersion;
import com.azure.resourcemanager.appservice.models.PlatformArchitecture;
import com.azure.resourcemanager.appservice.models.PublicNetworkAccess;
import com.azure.resourcemanager.appservice.models.PythonVersion;
import com.azure.resourcemanager.appservice.models.RedundancyMode;
import com.azure.resourcemanager.appservice.models.RemoteVisualStudioVersion;
import com.azure.resourcemanager.appservice.models.ScmType;
import com.azure.resourcemanager.appservice.models.SiteAvailabilityState;
import com.azure.resourcemanager.appservice.models.SlotSwapStatus;
import com.azure.resourcemanager.appservice.models.SslState;
import com.azure.resourcemanager.appservice.models.SupportedTlsVersions;
import com.azure.resourcemanager.appservice.models.UsageState;
import com.azure.resourcemanager.appservice.models.VirtualApplication;
import com.azure.resourcemanager.appservice.models.WebAppAuthentication;
import com.azure.resourcemanager.appservice.models.WebAppBase;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.appservice.models.WebSiteBase;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.utils.RoleAssignmentHelper;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpoint;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointConnectionProvisioningState;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.dag.FunctionalTaskItem;
import com.azure.resourcemanager.resources.fluentcore.dag.IndexableTaskItem;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.appservice.fluent.models.RemotePrivateEndpointConnectionArmResourceInner;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * The implementation for WebAppBase.
 *
 * @param <FluentT> the fluent interface of the web app or deployment slot or function app
 * @param <FluentImplT> the fluent implementation of the web app or deployment slot or function app
 */
abstract class WebAppBaseImpl<FluentT extends WebAppBase, FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends GroupableResourceImpl<FluentT, SiteInner, FluentImplT, AppServiceManager> implements WebAppBase,
    WebAppBase.Definition<FluentT>, WebAppBase.Update<FluentT>, WebAppBase.UpdateStages.WithWebContainer<FluentT> {

    private final ClientLogger logger = new ClientLogger(getClass());

    protected static final String SETTING_DOCKER_IMAGE = "DOCKER_CUSTOM_IMAGE_NAME";
    protected static final String SETTING_REGISTRY_SERVER = "DOCKER_REGISTRY_SERVER_URL";
    protected static final String SETTING_REGISTRY_USERNAME = "DOCKER_REGISTRY_SERVER_USERNAME";
    protected static final String SETTING_REGISTRY_PASSWORD = "DOCKER_REGISTRY_SERVER_PASSWORD";

    protected static final String SETTING_FUNCTIONS_WORKER_RUNTIME = "FUNCTIONS_WORKER_RUNTIME";
    protected static final String SETTING_FUNCTIONS_EXTENSION_VERSION = "FUNCTIONS_EXTENSION_VERSION";

    protected static final String IP_RESTRICTION_ACTION_ALLOW = "Allow";
    protected static final String IP_RESTRICTION_ACTION_DENY = "Deny";

    private static final Map<AzureEnvironment, String> DNS_MAP = new HashMap<AzureEnvironment, String>() {
        {
            put(AzureEnvironment.AZURE, "azurewebsites.net");
            put(AzureEnvironment.AZURE_CHINA, "chinacloudsites.cn");
            put(AzureEnvironment.AZURE_GERMANY, "azurewebsites.de");
            put(AzureEnvironment.AZURE_US_GOVERNMENT, "azurewebsites.us");
        }
    };

    SiteConfigResourceInner siteConfig;
    KuduClient kuduClient;
    WebSiteBase webSiteBase;

    private Map<String, HostnameSslState> hostNameSslStateMap;
    private TreeMap<String, HostnameBindingImpl<FluentT, FluentImplT>> hostNameBindingsToCreate;
    private List<String> hostNameBindingsToDelete;
    private TreeMap<String, HostnameSslBindingImpl<FluentT, FluentImplT>> sslBindingsToCreate;

    protected Map<String, String> appSettingsToAdd;
    protected List<String> appSettingsToRemove;
    private Map<String, Boolean> appSettingStickiness;
    private Map<String, ConnStringValueTypePair> connectionStringsToAdd;
    private List<String> connectionStringsToRemove;
    private Map<String, Boolean> connectionStringStickiness;
    private WebAppSourceControlImpl<FluentT, FluentImplT> sourceControl;
    private boolean sourceControlToDelete;
    private WebAppAuthenticationImpl<FluentT, FluentImplT> authentication;
    private boolean authenticationToUpdate;
    private WebAppDiagnosticLogsImpl<FluentT, FluentImplT> diagnosticLogs;
    private boolean diagnosticLogsToUpdate;
    private FunctionalTaskItem msiHandler;
    private boolean isInCreateMode;
    private WebAppMsiHandler<FluentT, FluentImplT> webAppMsiHandler;

    WebAppBaseImpl(String name, SiteInner innerObject, SiteConfigResourceInner siteConfig,
        SiteLogsConfigInner logConfig, AppServiceManager manager) {
        super(name, innerObject, manager);
        if (innerObject != null && innerObject.kind() != null) {
            innerObject.withKind(innerObject.kind().replace(";", ","));
        }
        this.siteConfig = siteConfig;
        if (logConfig != null) {
            this.diagnosticLogs = new WebAppDiagnosticLogsImpl<>(logConfig, this);
        }

        webAppMsiHandler = new WebAppMsiHandler<>(manager.authorizationManager(), this);
        normalizeProperties();
        isInCreateMode = innerModel() == null || innerModel().id() == null;
        if (!isInCreateMode) {
            initializeKuduClient();
        }
    }

    public boolean isInCreateMode() {
        return isInCreateMode;
    }

    private void initializeKuduClient() {
        if (kuduClient == null) {
            kuduClient = new KuduClient(this);
        }
    }

    @Override
    public void setInner(SiteInner innerObject) {
        if (innerObject.kind() != null) {
            innerObject.withKind(innerObject.kind().replace(";", ","));
        }
        super.setInner(innerObject);
    }

    RoleAssignmentHelper.IdProvider idProvider() {
        return new RoleAssignmentHelper.IdProvider() {
            @Override
            public String principalId() {
                if (innerModel() != null && innerModel().identity() != null) {
                    return innerModel().identity().principalId();
                } else {
                    return null;
                }
            }

            @Override
            public String resourceId() {
                if (innerModel() != null) {
                    return innerModel().id();
                } else {
                    return null;
                }
            }
        };
    }

    private void normalizeProperties() {
        this.hostNameBindingsToCreate = new TreeMap<>();
        this.hostNameBindingsToDelete = new ArrayList<>();
        this.appSettingsToAdd = new HashMap<>();
        this.appSettingsToRemove = new ArrayList<>();
        this.appSettingStickiness = new HashMap<>();
        this.connectionStringsToAdd = new HashMap<>();
        this.connectionStringsToRemove = new ArrayList<>();
        this.connectionStringStickiness = new HashMap<>();
        this.sourceControl = null;
        this.sourceControlToDelete = false;
        this.authenticationToUpdate = false;
        this.diagnosticLogsToUpdate = false;
        this.sslBindingsToCreate = new TreeMap<>();
        this.msiHandler = null;
        this.webSiteBase = new WebSiteBaseImpl(innerModel());
        this.hostNameSslStateMap = new HashMap<>(this.webSiteBase.hostnameSslStates());
        this.webAppMsiHandler.clear();
    }

    @Override
    public String state() {
        return webSiteBase.state();
    }

    @Override
    public Set<String> hostnames() {
        return webSiteBase.hostnames();
    }

    @Override
    public String repositorySiteName() {
        return webSiteBase.repositorySiteName();
    }

    @Override
    public UsageState usageState() {
        return webSiteBase.usageState();
    }

    @Override
    public boolean enabled() {
        return webSiteBase.enabled();
    }

    @Override
    public Set<String> enabledHostNames() {
        return webSiteBase.enabledHostNames();
    }

    @Override
    public SiteAvailabilityState availabilityState() {
        return webSiteBase.availabilityState();
    }

    @Override
    public Map<String, HostnameSslState> hostnameSslStates() {
        return Collections.unmodifiableMap(hostNameSslStateMap);
    }

    @Override
    public String appServicePlanId() {
        return webSiteBase.appServicePlanId();
    }

    @Override
    public OffsetDateTime lastModifiedTime() {
        return webSiteBase.lastModifiedTime();
    }

    @Override
    public Set<String> trafficManagerHostNames() {
        return webSiteBase.trafficManagerHostNames();
    }

    @Override
    public boolean scmSiteAlsoStopped() {
        return webSiteBase.scmSiteAlsoStopped();
    }

    @Override
    public String targetSwapSlot() {
        return webSiteBase.targetSwapSlot();
    }

    @Override
    public boolean clientAffinityEnabled() {
        return webSiteBase.clientAffinityEnabled();
    }

    @Override
    public boolean clientCertEnabled() {
        return webSiteBase.clientCertEnabled();
    }

    @Override
    public boolean hostnamesDisabled() {
        return webSiteBase.hostnamesDisabled();
    }

    @Override
    public Set<String> outboundIPAddresses() {
        return webSiteBase.outboundIPAddresses();
    }

    @Override
    public int containerSize() {
        return webSiteBase.containerSize();
    }

    @Override
    public CloningInfo cloningInfo() {
        return webSiteBase.cloningInfo();
    }

    @Override
    public boolean isDefaultContainer() {
        return webSiteBase.isDefaultContainer();
    }

    @Override
    public String defaultHostname() {
        if (innerModel().defaultHostname() != null) {
            return innerModel().defaultHostname();
        } else {
            AzureEnvironment environment = manager().environment();
            String dns = DNS_MAP.get(environment);
            String leaf = name();
            if (this instanceof DeploymentSlotBaseImpl<?, ?, ?, ?, ?>) {
                leaf = ((DeploymentSlotBaseImpl<?, ?, ?, ?, ?>) this).parent().name() + "-" + leaf;
            }
            return leaf + "." + dns;
        }
    }

    @Override
    public List<String> defaultDocuments() {
        if (siteConfig == null) {
            return null;
        }
        return Collections.unmodifiableList(siteConfig.defaultDocuments());
    }

    @Override
    public NetFrameworkVersion netFrameworkVersion() {
        if (siteConfig == null) {
            return null;
        }
        return NetFrameworkVersion.fromString(siteConfig.netFrameworkVersion());
    }

    @Override
    public PhpVersion phpVersion() {
        if (siteConfig == null || siteConfig.phpVersion() == null) {
            return PhpVersion.OFF;
        }
        return PhpVersion.fromString(siteConfig.phpVersion());
    }

    @Override
    public PythonVersion pythonVersion() {
        if (siteConfig == null || siteConfig.pythonVersion() == null) {
            return PythonVersion.OFF;
        }
        return PythonVersion.fromString(siteConfig.pythonVersion());
    }

    @Override
    public String nodeVersion() {
        if (siteConfig == null) {
            return null;
        }
        return siteConfig.nodeVersion();
    }

    @Override
    public boolean remoteDebuggingEnabled() {
        if (siteConfig == null) {
            return false;
        }
        return ResourceManagerUtils.toPrimitiveBoolean(siteConfig.remoteDebuggingEnabled());
    }

    @Override
    public RemoteVisualStudioVersion remoteDebuggingVersion() {
        if (siteConfig == null) {
            return null;
        }
        return RemoteVisualStudioVersion.fromString(siteConfig.remoteDebuggingVersion());
    }

    @Override
    public boolean webSocketsEnabled() {
        if (siteConfig == null) {
            return false;
        }
        return ResourceManagerUtils.toPrimitiveBoolean(siteConfig.webSocketsEnabled());
    }

    @Override
    public boolean alwaysOn() {
        if (siteConfig == null) {
            return false;
        }
        return ResourceManagerUtils.toPrimitiveBoolean(siteConfig.alwaysOn());
    }

    @Override
    public JavaVersion javaVersion() {
        if (siteConfig == null || siteConfig.javaVersion() == null) {
            return JavaVersion.OFF;
        }
        return JavaVersion.fromString(siteConfig.javaVersion());
    }

    @Override
    public String javaContainer() {
        if (siteConfig == null) {
            return null;
        }
        return siteConfig.javaContainer();
    }

    @Override
    public String javaContainerVersion() {
        if (siteConfig == null) {
            return null;
        }
        return siteConfig.javaContainerVersion();
    }

    @Override
    public ManagedPipelineMode managedPipelineMode() {
        if (siteConfig == null) {
            return null;
        }
        return siteConfig.managedPipelineMode();
    }

    @Override
    public PlatformArchitecture platformArchitecture() {
        if (siteConfig.use32BitWorkerProcess()) {
            return PlatformArchitecture.X86;
        } else {
            return PlatformArchitecture.X64;
        }
    }

    @Override
    public String linuxFxVersion() {
        if (siteConfig == null) {
            return null;
        }
        return siteConfig.linuxFxVersion();
    }

    @Override
    public String windowsFxVersion() {
        if (siteConfig == null) {
            return null;
        }
        return siteConfig.windowsFxVersion();
    }

    @Override
    public String autoSwapSlotName() {
        if (siteConfig == null) {
            return null;
        }
        return siteConfig.autoSwapSlotName();
    }

    @Override
    public boolean httpsOnly() {
        return webSiteBase.httpsOnly();
    }

    @Override
    public FtpsState ftpsState() {
        if (siteConfig == null) {
            return null;
        }
        return siteConfig.ftpsState();
    }

    @Override
    public List<VirtualApplication> virtualApplications() {
        if (siteConfig == null) {
            return null;
        }
        return siteConfig.virtualApplications();
    }

    @Override
    public boolean http20Enabled() {
        if (siteConfig == null) {
            return false;
        }
        return ResourceManagerUtils.toPrimitiveBoolean(siteConfig.http20Enabled());
    }

    @Override
    public boolean localMySqlEnabled() {
        if (siteConfig == null) {
            return false;
        }
        return ResourceManagerUtils.toPrimitiveBoolean(siteConfig.localMySqlEnabled());
    }

    @Override
    public ScmType scmType() {
        if (siteConfig == null) {
            return null;
        }
        return siteConfig.scmType();
    }

    @Override
    public String documentRoot() {
        if (siteConfig == null) {
            return null;
        }
        return siteConfig.documentRoot();
    }

    @Override
    public SupportedTlsVersions minTlsVersion() {
        if (siteConfig == null) {
            return null;
        }
        return siteConfig.minTlsVersion();
    }

    @Override
    public List<IpSecurityRestriction> ipSecurityRules() {
        if (this.siteConfig == null || this.siteConfig.ipSecurityRestrictions() == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(siteConfig.ipSecurityRestrictions());
    }

    @Override
    public OperatingSystem operatingSystem() {
        if (innerModel().kind() != null && innerModel().kind().toLowerCase(Locale.ROOT).contains("linux")) {
            return OperatingSystem.LINUX;
        } else {
            return OperatingSystem.WINDOWS;
        }
    }

    @Override
    public String systemAssignedManagedServiceIdentityTenantId() {
        if (innerModel().identity() == null) {
            return null;
        }
        return innerModel().identity().tenantId();
    }

    @Override
    public String systemAssignedManagedServiceIdentityPrincipalId() {
        if (innerModel().identity() == null) {
            return null;
        }
        return innerModel().identity().principalId();
    }

    @Override
    public Set<String> userAssignedManagedServiceIdentityIds() {
        if (innerModel().identity() == null) {
            return null;
        }
        return innerModel().identity().userAssignedIdentities().keySet();
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> diagnosticLogsConfig() {
        return diagnosticLogs;
    }

    @Override
    public InputStream streamApplicationLogs() {
        return pipeObservableToInputStream(streamApplicationLogsAsync());
    }

    @Override
    public Flux<String> streamApplicationLogsAsync() {
        return kuduClient.streamApplicationLogsAsync();
    }

    @Override
    public InputStream streamHttpLogs() {
        return pipeObservableToInputStream(streamHttpLogsAsync());
    }

    @Override
    public Flux<String> streamHttpLogsAsync() {
        return kuduClient.streamHttpLogsAsync();
    }

    @Override
    public InputStream streamTraceLogs() {
        return pipeObservableToInputStream(streamTraceLogsAsync());
    }

    @Override
    public Flux<String> streamTraceLogsAsync() {
        return kuduClient.streamTraceLogsAsync();
    }

    @Override
    public InputStream streamDeploymentLogs() {
        return pipeObservableToInputStream(streamDeploymentLogsAsync());
    }

    @Override
    public Flux<String> streamDeploymentLogsAsync() {
        return kuduClient.streamDeploymentLogsAsync();
    }

    @Override
    public InputStream streamAllLogs() {
        return pipeObservableToInputStream(streamAllLogsAsync());
    }

    @Override
    public Flux<String> streamAllLogsAsync() {
        return kuduClient.streamAllLogsAsync();
    }

    private InputStream pipeObservableToInputStream(Flux<String> observable) {
        PipedInputStreamWithCallback in = new PipedInputStreamWithCallback();
        final PipedOutputStream out = new PipedOutputStream();
        try {
            in.connect(out);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
        final Disposable subscription = observable
            // Do not block current thread
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(s -> {
                try {
                    out.write(s.getBytes(StandardCharsets.UTF_8));
                    out.write('\n');
                    out.flush();
                } catch (IOException e) {
                    throw logger.logExceptionAsError(new RuntimeException(e));
                }
            });
        in.addCallback(() -> {
            subscription.dispose();
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return in;
    }

    @Override
    public Map<String, AppSetting> getAppSettings() {
        return getAppSettingsAsync().block();
    }

    @Override
    public Mono<Map<String, AppSetting>> getAppSettingsAsync() {
        return Mono.zip(listAppSettings(), listSlotConfigurations(),
            (appSettingsInner, slotConfigs) -> appSettingsInner.properties()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new AppSettingImpl(entry.getKey(),
                    entry.getValue(),
                    slotConfigs.appSettingNames() != null && slotConfigs.appSettingNames().contains(entry.getKey())))));
    }

    @Override
    public Map<String, ConnectionString> getConnectionStrings() {
        return getConnectionStringsAsync().block();
    }

    @Override
    public Mono<Map<String, ConnectionString>> getConnectionStringsAsync() {
        return Mono.zip(listConnectionStrings(), listSlotConfigurations(),
            (connectionStringsInner, slotConfigs) -> connectionStringsInner.properties()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                    entry -> new ConnectionStringImpl(entry.getKey(), entry.getValue(),
                        slotConfigs.connectionStringNames() != null
                            && slotConfigs.connectionStringNames().contains(entry.getKey())))));
    }

    @Override
    public WebAppAuthentication getAuthenticationConfig() {
        return getAuthenticationConfigAsync().block();
    }

    @Override
    public Mono<WebAppAuthentication> getAuthenticationConfigAsync() {
        return getAuthentication()
            .map(siteAuthSettingsInner -> new WebAppAuthenticationImpl<>(siteAuthSettingsInner, WebAppBaseImpl.this));
    }

    abstract Mono<SiteInner> createOrUpdateInner(SiteInner site);

    abstract Mono<SiteInner> updateInner(SitePatchResourceInner siteUpdate);

    abstract Mono<SiteInner> getInner();

    abstract Mono<SiteConfigResourceInner> getConfigInner();

    abstract Mono<SiteConfigResourceInner> createOrUpdateSiteConfig(SiteConfigResourceInner siteConfig);

    abstract Mono<Void> deleteHostnameBinding(String hostname);

    abstract Mono<StringDictionaryInner> listAppSettings();

    abstract Mono<StringDictionaryInner> updateAppSettings(StringDictionaryInner inner);

    abstract Mono<ConnectionStringDictionaryInner> listConnectionStrings();

    abstract Mono<ConnectionStringDictionaryInner> updateConnectionStrings(ConnectionStringDictionaryInner inner);

    abstract Mono<SlotConfigNamesResourceInner> listSlotConfigurations();

    abstract Mono<SlotConfigNamesResourceInner> updateSlotConfigurations(SlotConfigNamesResourceInner inner);

    abstract Mono<SiteSourceControlInner> createOrUpdateSourceControl(SiteSourceControlInner inner);

    abstract Mono<Void> deleteSourceControl();

    abstract Mono<SiteAuthSettingsInner> updateAuthentication(SiteAuthSettingsInner inner);

    abstract Mono<SiteAuthSettingsInner> getAuthentication();

    abstract Mono<MSDeployStatusInner> createMSDeploy(MSDeploy msDeployInner);

    abstract Mono<SiteLogsConfigInner> updateDiagnosticLogsConfig(SiteLogsConfigInner siteLogsConfigInner);

    @Override
    public void beforeGroupCreateOrUpdate() {
        if (hostNameSslStateMap.size() > 0) {
            innerModel().withHostnameSslStates(new ArrayList<>(hostNameSslStateMap.values()));
        }
        // Hostname and SSL bindings
        IndexableTaskItem rootTaskItem = wrapTask(context -> {
            // Submit hostname bindings
            return submitHostNameBindings()
                // Submit SSL bindings
                .flatMap(fluentT -> submitSslBindings(fluentT.innerModel()));
        });
        IndexableTaskItem lastTaskItem = rootTaskItem;
        // Site config
        lastTaskItem = sequentialTask(lastTaskItem, context -> submitSiteConfig());
        // Metadata, app settings, and connection strings
        lastTaskItem = sequentialTask(lastTaskItem,
            context -> submitMetadata()
                .flatMap(ignored -> submitAppSettings().mergeWith(submitConnectionStrings()).last())
                .flatMap(ignored -> submitStickiness()));
        // Source control
        lastTaskItem = sequentialTask(lastTaskItem,
            context -> submitSourceControlToDelete().flatMap(ignored -> submitSourceControlToCreate()));
        // Authentication
        lastTaskItem = sequentialTask(lastTaskItem, context -> submitAuthentication());
        // Log configuration
        lastTaskItem = sequentialTask(lastTaskItem, context -> submitLogConfiguration());
        // MSI roles
        if (msiHandler != null) {
            sequentialTask(lastTaskItem, msiHandler);
        }

        addPostRunDependent(rootTaskItem);
    }

    private static IndexableTaskItem wrapTask(FunctionalTaskItem taskItem) {
        return IndexableTaskItem.create(taskItem);
    }

    private static IndexableTaskItem sequentialTask(IndexableTaskItem taskItem1, FunctionalTaskItem taskItem2) {
        IndexableTaskItem taskItem = IndexableTaskItem.create(taskItem2);
        taskItem1.addPostRunDependent(taskItem);
        return taskItem;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<FluentT> createResourceAsync() {
        this.webAppMsiHandler.processCreatedExternalIdentities();
        this.webAppMsiHandler.handleExternalIdentities();
        return submitSite(innerModel()).map(siteInner -> {
            setInner(siteInner);
            return (FluentT) WebAppBaseImpl.this;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<FluentT> updateResourceAsync() {
        SiteInner siteInner = this.innerModel();
        SitePatchResourceInner siteUpdate = new SitePatchResourceInner();
        siteUpdate.withHostnameSslStates(siteInner.hostnameSslStates());
        siteUpdate.withKind(siteInner.kind());
        siteUpdate.withEnabled(siteInner.enabled());
        siteUpdate.withServerFarmId(siteInner.serverFarmId());
        siteUpdate.withReserved(siteInner.reserved());
        siteUpdate.withIsXenon(siteInner.isXenon());
        siteUpdate.withHyperV(siteInner.hyperV());
        siteUpdate.withScmSiteAlsoStopped(siteInner.scmSiteAlsoStopped());
        siteUpdate.withHostingEnvironmentProfile(siteInner.hostingEnvironmentProfile());
        siteUpdate.withClientAffinityEnabled(siteInner.clientAffinityEnabled());
        siteUpdate.withClientCertEnabled(siteInner.clientCertEnabled());
        siteUpdate.withClientCertExclusionPaths(siteInner.clientCertExclusionPaths());
        siteUpdate.withHostNamesDisabled(siteInner.hostNamesDisabled());
        siteUpdate.withContainerSize(siteInner.containerSize());
        siteUpdate.withDailyMemoryTimeQuota(siteInner.dailyMemoryTimeQuota());
        siteUpdate.withCloningInfo(siteInner.cloningInfo());
        siteUpdate.withHttpsOnly(siteInner.httpsOnly());
        siteUpdate.withRedundancyMode(siteInner.redundancyMode());

        this.webAppMsiHandler.handleExternalIdentities(siteUpdate);
        return submitSite(siteUpdate).map(siteInner1 -> {
            setInner(siteInner1);
            webAppMsiHandler.clear();
            return (FluentT) WebAppBaseImpl.this;
        });
    }

    @Override
    public Mono<Void> afterPostRunAsync(final boolean isGroupFaulted) {
        if (!isGroupFaulted) {
            isInCreateMode = false;
            initializeKuduClient();
        }
        return Mono.fromCallable(() -> {
            normalizeProperties();
            return null;
        });
    }

    Mono<SiteInner> submitSite(final SiteInner site) {
        site.withSiteConfig(new SiteConfigInner());
        return submitSiteWithoutSiteConfig(site);
    }

    Mono<SiteInner> submitSiteWithoutSiteConfig(final SiteInner site) {
        // Construct web app observable
        return createOrUpdateInner(site).map(siteInner -> {
            site.withSiteConfig(null);
            return siteInner;
        });
    }

    Mono<SiteInner> submitSite(final SitePatchResourceInner siteUpdate) {
        // Construct web app observable
        return updateInner(siteUpdate).map(siteInner -> {
            siteInner.withSiteConfig(null);
            return siteInner;
        });
    }

    @SuppressWarnings("unchecked")
    Mono<FluentT> submitHostNameBindings() {
        final List<Mono<HostnameBinding>> bindingObservables = new ArrayList<>();
        for (HostnameBindingImpl<FluentT, FluentImplT> binding : hostNameBindingsToCreate.values()) {
            bindingObservables.add(binding.createAsync());
        }
        for (String binding : hostNameBindingsToDelete) {
            bindingObservables.add(deleteHostnameBinding(binding).then(Mono.empty()));
        }
        if (bindingObservables.isEmpty()) {
            return Mono.just((FluentT) this);
        } else {
            return Flux.zip(bindingObservables, ignored -> WebAppBaseImpl.this).last().onErrorResume(throwable -> {
                if (throwable instanceof HttpResponseException
                    && ((HttpResponseException) throwable).getResponse().getStatusCode() == 400) {
                    return submitSite(innerModel())
                        .flatMap(ignored -> Flux.zip(bindingObservables, ignored1 -> WebAppBaseImpl.this).last());
                } else {
                    return Mono.error(throwable);
                }
            }).flatMap(WebAppBaseImpl::refreshAsync);
        }
    }

    Mono<Indexable> submitSslBindings(final SiteInner site) {
        List<Mono<AppServiceCertificate>> certs = new ArrayList<>();
        for (final HostnameSslBindingImpl<FluentT, FluentImplT> binding : sslBindingsToCreate.values()) {
            certs.add(binding.newCertificate());
            hostNameSslStateMap.put(binding.innerModel().name(), binding.innerModel().withToUpdate(true));
        }
        if (certs.isEmpty()) {
            return Mono.just((Indexable) this);
        } else {
            site.withHostnameSslStates(new ArrayList<>(hostNameSslStateMap.values()));
            return Flux.zip(certs, ignored -> site).last().flatMap(this::createOrUpdateInner).map(siteInner -> {
                setInner(siteInner);
                return WebAppBaseImpl.this;
            });
        }
    }

    Mono<Indexable> submitSiteConfig() {
        if (siteConfig == null) {
            return Mono.just((Indexable) this);
        }
        if (siteConfig.azureStorageAccounts() != null) {
            // Remove azureStorageAccounts if any lack accessKey property. Service will reject if lack accessKey.
            if (siteConfig.azureStorageAccounts()
                .values()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(v -> v.accessKey() == null)) {

                siteConfig.withAzureStorageAccounts(null);
            }
        }
        return createOrUpdateSiteConfig(siteConfig).flatMap(returnedSiteConfig -> {
            siteConfig = returnedSiteConfig;
            return Mono.just((Indexable) WebAppBaseImpl.this);
        });
    }

    Mono<Indexable> submitAppSettings() {
        Mono<Indexable> observable = Mono.just((Indexable) this);
        if (!appSettingsToAdd.isEmpty() || !appSettingsToRemove.isEmpty()) {
            observable = listAppSettings().switchIfEmpty(Mono.just(new StringDictionaryInner()))
                .flatMap(stringDictionaryInner -> {
                    if (stringDictionaryInner.properties() == null) {
                        stringDictionaryInner.withProperties(new HashMap<String, String>());
                    }
                    for (String appSettingKey : appSettingsToRemove) {
                        stringDictionaryInner.properties().remove(appSettingKey);
                    }
                    stringDictionaryInner.properties().putAll(appSettingsToAdd);
                    return updateAppSettings(stringDictionaryInner);
                })
                .map(ignored -> WebAppBaseImpl.this);
        }
        return observable;
    }

    Mono<Indexable> submitMetadata() {
        // NOOP
        return Mono.just((Indexable) this);
    }

    Mono<Indexable> submitConnectionStrings() {
        Mono<Indexable> observable = Mono.just((Indexable) this);
        if (!connectionStringsToAdd.isEmpty() || !connectionStringsToRemove.isEmpty()) {
            observable = listConnectionStrings().switchIfEmpty(Mono.just(new ConnectionStringDictionaryInner()))
                .flatMap(dictionaryInner -> {
                    if (dictionaryInner.properties() == null) {
                        dictionaryInner.withProperties(new HashMap<String, ConnStringValueTypePair>());
                    }
                    for (String connectionString : connectionStringsToRemove) {
                        dictionaryInner.properties().remove(connectionString);
                    }
                    dictionaryInner.properties().putAll(connectionStringsToAdd);
                    return updateConnectionStrings(dictionaryInner);
                })
                .map(ignored -> WebAppBaseImpl.this);
        }
        return observable;
    }

    Mono<Indexable> submitStickiness() {
        Mono<Indexable> observable = Mono.just((Indexable) this);
        if (!appSettingStickiness.isEmpty() || !connectionStringStickiness.isEmpty()) {
            observable = listSlotConfigurations().switchIfEmpty(Mono.just(new SlotConfigNamesResourceInner()))
                .flatMap(slotConfigNamesResourceInner -> {
                    if (slotConfigNamesResourceInner.appSettingNames() == null) {
                        slotConfigNamesResourceInner.withAppSettingNames(new ArrayList<>());
                    }
                    if (slotConfigNamesResourceInner.connectionStringNames() == null) {
                        slotConfigNamesResourceInner.withConnectionStringNames(new ArrayList<>());
                    }
                    Set<String> stickyAppSettingKeys = new HashSet<>(slotConfigNamesResourceInner.appSettingNames());
                    Set<String> stickyConnectionStringNames
                        = new HashSet<>(slotConfigNamesResourceInner.connectionStringNames());
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
                    slotConfigNamesResourceInner
                        .withConnectionStringNames(new ArrayList<>(stickyConnectionStringNames));
                    return updateSlotConfigurations(slotConfigNamesResourceInner);
                })
                .map(ignored -> WebAppBaseImpl.this);
        }
        return observable;
    }

    Mono<Indexable> submitSourceControlToCreate() {
        if (sourceControl == null || sourceControlToDelete) {
            return Mono.just((Indexable) this);
        }
        return sourceControl.registerGithubAccessToken()
            .then(createOrUpdateSourceControl(sourceControl.innerModel()))
            .delayElement(ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(Duration.ofSeconds(30)))
            .map(ignored -> WebAppBaseImpl.this);
    }

    Mono<Indexable> submitSourceControlToDelete() {
        if (!sourceControlToDelete) {
            return Mono.just((Indexable) this);
        }
        return deleteSourceControl().map(ignored -> WebAppBaseImpl.this);
    }

    Mono<Indexable> submitAuthentication() {
        if (!authenticationToUpdate) {
            return Mono.just((Indexable) this);
        }
        return updateAuthentication(authentication.innerModel()).map(siteAuthSettingsInner -> {
            WebAppBaseImpl.this.authentication
                = new WebAppAuthenticationImpl<>(siteAuthSettingsInner, WebAppBaseImpl.this);
            return WebAppBaseImpl.this;
        });
    }

    Mono<Indexable> submitLogConfiguration() {
        if (!diagnosticLogsToUpdate) {
            return Mono.just((Indexable) this);
        }
        return updateDiagnosticLogsConfig(diagnosticLogs.innerModel()).map(siteLogsConfigInner -> {
            WebAppBaseImpl.this.diagnosticLogs
                = new WebAppDiagnosticLogsImpl<>(siteLogsConfigInner, WebAppBaseImpl.this);
            return WebAppBaseImpl.this;
        });
    }

    @Override
    public WebDeploymentImpl<FluentT, FluentImplT> deploy() {
        return new WebDeploymentImpl<>(this);
    }

    WebAppBaseImpl<FluentT, FluentImplT>
        withNewHostNameSslBinding(final HostnameSslBindingImpl<FluentT, FluentImplT> hostNameSslBinding) {
        sslBindingsToCreate.put(hostNameSslBinding.name(), hostNameSslBinding);
        return this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withManagedHostnameBindings(AppServiceDomain domain, String... hostnames) {
        for (String hostname : hostnames) {
            if ("@".equals(hostname) || hostname.equalsIgnoreCase(domain.name())) {
                defineHostnameBinding().withAzureManagedDomain(domain)
                    .withSubDomain(hostname)
                    .withDnsRecordType(CustomHostnameDnsRecordType.A)
                    .attach();
            } else {
                defineHostnameBinding().withAzureManagedDomain(domain)
                    .withSubDomain(hostname)
                    .withDnsRecordType(CustomHostnameDnsRecordType.CNAME)
                    .attach();
            }
        }
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public HostnameBindingImpl<FluentT, FluentImplT> defineHostnameBinding() {
        HostnameBindingInner inner = new HostnameBindingInner();
        inner.withSiteName(name());
        inner.withAzureResourceType(AzureResourceType.WEBSITE);
        inner.withAzureResourceName(name());
        inner.withHostnameType(HostnameType.VERIFIED);
        return new HostnameBindingImpl<>(inner, (FluentImplT) this);
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withThirdPartyHostnameBinding(String domain, String... hostnames) {
        for (String hostname : hostnames) {
            defineHostnameBinding().withThirdPartyDomain(domain)
                .withSubDomain(hostname)
                .withDnsRecordType(CustomHostnameDnsRecordType.CNAME)
                .attach();
        }
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withoutHostnameBinding(String hostname) {
        hostNameBindingsToDelete.add(hostname);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withoutSslBinding(String hostname) {
        if (hostNameSslStateMap.containsKey(hostname)) {
            hostNameSslStateMap.get(hostname).withSslState(SslState.DISABLED).withToUpdate(true);
        }
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    FluentImplT withHostNameBinding(final HostnameBindingImpl<FluentT, FluentImplT> hostNameBinding) {
        this.hostNameBindingsToCreate.put(hostNameBinding.name(), hostNameBinding);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withAppDisabledOnCreation() {
        innerModel().withEnabled(false);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withScmSiteAlsoStopped(boolean scmSiteAlsoStopped) {
        innerModel().withScmSiteAlsoStopped(scmSiteAlsoStopped);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withClientAffinityEnabled(boolean enabled) {
        innerModel().withClientAffinityEnabled(enabled);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withClientCertEnabled(boolean enabled) {
        innerModel().withClientCertEnabled(enabled);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public HostnameSslBindingImpl<FluentT, FluentImplT> defineSslBinding() {
        return new HostnameSslBindingImpl<>(new HostnameSslState(), (FluentImplT) this);
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withNetFrameworkVersion(NetFrameworkVersion version) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withNetFrameworkVersion(version.toString());
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withPhpVersion(PhpVersion version) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withPhpVersion(version.toString());
        return (FluentImplT) this;
    }

    public FluentImplT withoutPhp() {
        return withPhpVersion(PhpVersion.fromString(""));
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withJavaVersion(JavaVersion version) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withJavaVersion(version.toString());
        return (FluentImplT) this;
    }

    public FluentImplT withoutJava() {
        return withJavaVersion(JavaVersion.fromString("")).withWebContainer(WebContainer.fromString(""));
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withWebContainer(WebContainer webContainer) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        if (webContainer == null) {
            siteConfig.withJavaContainer(null);
            siteConfig.withJavaContainerVersion(null);
        } else if (webContainer.toString().isEmpty()) {
            siteConfig.withJavaContainer("");
            siteConfig.withJavaContainerVersion("");
        } else {
            String[] containerInfo = webContainer.toString().split(" ");
            siteConfig.withJavaContainer(containerInfo[0]);
            siteConfig.withJavaContainerVersion(containerInfo[1]);
        }
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withPythonVersion(PythonVersion version) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withPythonVersion(version.toString());
        return (FluentImplT) this;
    }

    public FluentImplT withoutPython() {
        return withPythonVersion(PythonVersion.fromString(""));
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withPlatformArchitecture(PlatformArchitecture platform) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withUse32BitWorkerProcess(platform.equals(PlatformArchitecture.X86));
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withWebSocketsEnabled(boolean enabled) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withWebSocketsEnabled(enabled);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withWebAppAlwaysOn(boolean alwaysOn) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withAlwaysOn(alwaysOn);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withManagedPipelineMode(ManagedPipelineMode managedPipelineMode) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withManagedPipelineMode(managedPipelineMode);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withAutoSwapSlotName(String slotName) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withAutoSwapSlotName(slotName);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withRemoteDebuggingEnabled(RemoteVisualStudioVersion remoteVisualStudioVersion) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withRemoteDebuggingEnabled(true);
        siteConfig.withRemoteDebuggingVersion(remoteVisualStudioVersion.toString());
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withRemoteDebuggingDisabled() {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withRemoteDebuggingEnabled(false);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withDefaultDocument(String document) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        if (siteConfig.defaultDocuments() == null) {
            siteConfig.withDefaultDocuments(new ArrayList<String>());
        }
        siteConfig.defaultDocuments().add(document);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withDefaultDocuments(List<String> documents) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        if (siteConfig.defaultDocuments() == null) {
            siteConfig.withDefaultDocuments(new ArrayList<>());
        }
        siteConfig.defaultDocuments().addAll(documents);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withoutDefaultDocument(String document) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        if (siteConfig.defaultDocuments() != null) {
            siteConfig.defaultDocuments().remove(document);
        }
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withHttpsOnly(boolean httpsOnly) {
        innerModel().withHttpsOnly(httpsOnly);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withHttp20Enabled(boolean http20Enabled) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withHttp20Enabled(http20Enabled);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withFtpsState(FtpsState ftpsState) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withFtpsState(ftpsState);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withVirtualApplications(List<VirtualApplication> virtualApplications) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withVirtualApplications(virtualApplications);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withMinTlsVersion(SupportedTlsVersions minTlsVersion) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withMinTlsVersion(minTlsVersion);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withAppSetting(String key, String value) {
        appSettingsToAdd.put(key, value);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withAppSettings(Map<String, String> settings) {
        appSettingsToAdd.putAll(settings);
        return (FluentImplT) this;
    }

    public FluentImplT withStickyAppSetting(String key, String value) {
        withAppSetting(key, value);
        return withAppSettingStickiness(key, true);
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withStickyAppSettings(Map<String, String> settings) {
        withAppSettings(settings);
        for (String key : settings.keySet()) {
            appSettingStickiness.put(key, true);
        }
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withoutAppSetting(String key) {
        appSettingsToRemove.add(key);
        appSettingStickiness.remove(key);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withAppSettingStickiness(String key, boolean sticky) {
        appSettingStickiness.put(key, sticky);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withConnectionString(String name, String value, ConnectionStringType type) {
        connectionStringsToAdd.put(name, new ConnStringValueTypePair().withValue(value).withType(type));
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withStickyConnectionString(String name, String value, ConnectionStringType type) {
        connectionStringsToAdd.put(name, new ConnStringValueTypePair().withValue(value).withType(type));
        connectionStringStickiness.put(name, true);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withoutConnectionString(String name) {
        connectionStringsToRemove.add(name);
        connectionStringStickiness.remove(name);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withConnectionStringStickiness(String name, boolean stickiness) {
        connectionStringStickiness.put(name, stickiness);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    void withSourceControl(WebAppSourceControlImpl<FluentT, FluentImplT> sourceControl) {
        this.sourceControl = sourceControl;
    }

    public WebAppSourceControlImpl<FluentT, FluentImplT> defineSourceControl() {
        SiteSourceControlInner sourceControlInner = new SiteSourceControlInner();
        return new WebAppSourceControlImpl<>(sourceControlInner, this);
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withLocalGitSourceControl() {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withScmType(ScmType.LOCAL_GIT);
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentImplT withoutSourceControl() {
        sourceControlToDelete = true;
        return (FluentImplT) this;
    }

    @SuppressWarnings("unchecked")
    void withAuthentication(WebAppAuthenticationImpl<FluentT, FluentImplT> authentication) {
        this.authentication = authentication;
        authenticationToUpdate = true;
    }

    void withDiagnosticLogs(WebAppDiagnosticLogsImpl<FluentT, FluentImplT> diagnosticLogs) {
        this.diagnosticLogs = diagnosticLogs;
        diagnosticLogsToUpdate = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<FluentT> refreshAsync() {
        return super.refreshAsync().flatMap(fluentT -> getConfigInner().map(returnedSiteConfig -> {
            siteConfig = returnedSiteConfig;
            return fluentT;
        }));
    }

    @Override
    protected Mono<SiteInner> getInnerAsync() {
        return getInner();
    }

    @Override
    public WebAppAuthenticationImpl<FluentT, FluentImplT> defineAuthentication() {
        return new WebAppAuthenticationImpl<>(new SiteAuthSettingsInner().withEnabled(true), this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withoutAuthentication() {
        this.authentication.innerModel().withEnabled(false);
        authenticationToUpdate = true;
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withContainerLoggingEnabled(int quotaInMB, int retentionDays) {
        return updateDiagnosticLogsConfiguration().withWebServerLogging()
            .withWebServerLogsStoredOnFileSystem()
            .withWebServerFileSystemQuotaInMB(quotaInMB)
            .withLogRetentionDays(retentionDays)
            .attach();
    }

    @Override
    public FluentImplT withContainerLoggingEnabled() {
        return withContainerLoggingEnabled(35, 0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withContainerLoggingDisabled() {
        return updateDiagnosticLogsConfiguration().withoutWebServerLogging().attach();
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withSystemAssignedManagedServiceIdentity() {
        this.webAppMsiHandler.withLocalManagedServiceIdentity();
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withoutSystemAssignedManagedServiceIdentity() {
        this.webAppMsiHandler.withoutLocalManagedServiceIdentity();
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withUserAssignedManagedServiceIdentity() {
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withSystemAssignedIdentityBasedAccessTo(final String resourceId, final BuiltInRole role) {
        this.webAppMsiHandler.withAccessTo(resourceId, role);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(final BuiltInRole role) {
        this.webAppMsiHandler.withAccessToCurrentResourceGroup(role);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withSystemAssignedIdentityBasedAccessTo(final String resourceId, final String roleDefinitionId) {
        this.webAppMsiHandler.withAccessTo(resourceId, roleDefinitionId);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(final String roleDefinitionId) {
        this.webAppMsiHandler.withAccessToCurrentResourceGroup(roleDefinitionId);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withNewUserAssignedManagedServiceIdentity(Creatable<Identity> creatableIdentity) {
        this.webAppMsiHandler.withNewExternalManagedServiceIdentity(creatableIdentity);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withExistingUserAssignedManagedServiceIdentity(Identity identity) {
        this.webAppMsiHandler.withExistingExternalManagedServiceIdentity(identity);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withoutUserAssignedManagedServiceIdentity(String identityId) {
        this.webAppMsiHandler.withoutExternalManagedServiceIdentity(identityId);
        return (FluentImplT) this;
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> defineDiagnosticLogsConfiguration() {
        if (diagnosticLogs == null) {
            return new WebAppDiagnosticLogsImpl<>(new SiteLogsConfigInner(), this);
        } else {
            return diagnosticLogs;
        }
    }

    @Override
    public WebAppDiagnosticLogsImpl<FluentT, FluentImplT> updateDiagnosticLogsConfiguration() {
        return defineDiagnosticLogsConfiguration();
    }

    @Override
    public ManagedServiceIdentity identity() {
        return webSiteBase.identity();
    }

    @Override
    public boolean hyperV() {
        return webSiteBase.hyperV();
    }

    @Override
    public HostingEnvironmentProfile hostingEnvironmentProfile() {
        return webSiteBase.hostingEnvironmentProfile();
    }

    @Override
    public Set<String> clientCertExclusionPaths() {
        return webSiteBase.clientCertExclusionPaths();
    }

    @Override
    public Set<String> possibleOutboundIpAddresses() {
        return webSiteBase.possibleOutboundIpAddresses();
    }

    @Override
    public int dailyMemoryTimeQuota() {
        return webSiteBase.dailyMemoryTimeQuota();
    }

    @Override
    public OffsetDateTime suspendedTill() {
        return webSiteBase.suspendedTill();
    }

    @Override
    public int maxNumberOfWorkers() {
        return webSiteBase.maxNumberOfWorkers();
    }

    @Override
    public SlotSwapStatus slotSwapStatus() {
        return webSiteBase.slotSwapStatus();
    }

    @Override
    public RedundancyMode redundancyMode() {
        return webSiteBase.redundancyMode();
    }

    private static class PipedInputStreamWithCallback extends PipedInputStream {
        private Runnable callback;

        private void addCallback(Runnable action) {
            this.callback = action;
        }

        @Override
        public void close() throws IOException {
            callback.run();
            super.close();
        }
    }

    protected void setAppFrameworkVersion(String fxVersion) {
        if (operatingSystem() == OperatingSystem.LINUX) {
            siteConfig.withLinuxFxVersion(fxVersion);
        } else {
            siteConfig.withWindowsFxVersion(fxVersion);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withAccessFromAllNetworks() {
        this.ensureIpSecurityRestrictions();
        this.siteConfig.withIpSecurityRestrictions(new ArrayList<>());
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withAccessFromNetworkSubnet(String subnetId, int priority) {
        this.ensureIpSecurityRestrictions();
        this.siteConfig.ipSecurityRestrictions()
            .add(new IpSecurityRestriction().withAction(IP_RESTRICTION_ACTION_ALLOW)
                .withPriority(priority)
                .withTag(IpFilterTag.DEFAULT)
                .withVnetSubnetResourceId(subnetId));
        return (FluentImplT) this;
    }

    @Override
    public FluentImplT withAccessFromIpAddress(String ipAddress, int priority) {
        String ipAddressCidr = ipAddress.contains("/") ? ipAddress : ipAddress + "/32";
        return withAccessFromIpAddressRange(ipAddressCidr, priority);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withAccessFromIpAddressRange(String ipAddressCidr, int priority) {
        this.ensureIpSecurityRestrictions();
        this.siteConfig.ipSecurityRestrictions()
            .add(new IpSecurityRestriction().withAction(IP_RESTRICTION_ACTION_ALLOW)
                .withPriority(priority)
                .withTag(IpFilterTag.DEFAULT)
                .withIpAddress(ipAddressCidr));
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withAccessRule(IpSecurityRestriction ipSecurityRule) {
        this.ensureIpSecurityRestrictions();
        this.siteConfig.ipSecurityRestrictions().add(ipSecurityRule);
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withoutNetworkSubnetAccess(String subnetId) {
        if (this.siteConfig != null && this.siteConfig.ipSecurityRestrictions() != null) {
            this.siteConfig.withIpSecurityRestrictions(this.siteConfig.ipSecurityRestrictions()
                .stream()
                .filter(r -> !(IP_RESTRICTION_ACTION_ALLOW.equalsIgnoreCase(r.action())
                    && IpFilterTag.DEFAULT == r.tag()
                    && subnetId.equalsIgnoreCase(r.vnetSubnetResourceId())))
                .collect(Collectors.toList()));
        }
        return (FluentImplT) this;
    }

    @Override
    public FluentImplT withoutIpAddressAccess(String ipAddress) {
        String ipAddressCidr = ipAddress.contains("/") ? ipAddress : ipAddress + "/32";
        return withoutIpAddressRangeAccess(ipAddressCidr);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withoutIpAddressRangeAccess(String ipAddressCidr) {
        if (this.siteConfig != null && this.siteConfig.ipSecurityRestrictions() != null) {
            this.siteConfig.withIpSecurityRestrictions(this.siteConfig.ipSecurityRestrictions()
                .stream()
                .filter(r -> !(IP_RESTRICTION_ACTION_ALLOW.equalsIgnoreCase(r.action())
                    && IpFilterTag.DEFAULT == r.tag()
                    && Objects.equals(ipAddressCidr, r.ipAddress())))
                .collect(Collectors.toList()));
        }
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT enablePublicNetworkAccess() {
        if (Objects.isNull(this.siteConfig)) {
            this.siteConfig = new SiteConfigResourceInner();
        }
        this.siteConfig.withPublicNetworkAccess("Enabled");
        return (FluentImplT) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT disablePublicNetworkAccess() {
        if (Objects.isNull(this.siteConfig)) {
            this.siteConfig = new SiteConfigResourceInner();
        }
        this.siteConfig.withPublicNetworkAccess("Disabled");
        return (FluentImplT) this;
    }

    @Override
    public PublicNetworkAccess publicNetworkAccess() {
        return Objects.isNull(innerModel().publicNetworkAccess())
            ? null
            : PublicNetworkAccess.fromString(innerModel().publicNetworkAccess());
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withContainerSize(int containerSize) {
        this.innerModel().withContainerSize(containerSize);
        return (FluentImplT) this;
    }

    @Override
    public Map<String, String> getSiteAppSettings() {
        return getSiteAppSettingsAsync().block();
    }

    @Override
    public Mono<Map<String, String>> getSiteAppSettingsAsync() {
        return kuduClient.settings();
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentImplT withoutAccessRule(IpSecurityRestriction ipSecurityRule) {
        if (this.siteConfig != null && this.siteConfig.ipSecurityRestrictions() != null) {
            this.siteConfig.withIpSecurityRestrictions(this.siteConfig.ipSecurityRestrictions()
                .stream()
                .filter(r -> !(Objects.equals(r.action(), ipSecurityRule.action())
                    && Objects.equals(r.tag(), ipSecurityRule.tag())
                    && (Objects.equals(r.ipAddress(), ipSecurityRule.ipAddress())
                        || Objects.equals(r.vnetSubnetResourceId(), ipSecurityRule.vnetSubnetResourceId()))))
                .collect(Collectors.toList()));
        }
        return (FluentImplT) this;
    }

    private void ensureIpSecurityRestrictions() {
        if (this.siteConfig == null) {
            this.siteConfig = new SiteConfigResourceInner();
        }
        if (this.siteConfig.ipSecurityRestrictions() == null) {
            this.siteConfig.withIpSecurityRestrictions(new ArrayList<>());
        }
    }

    protected static final class PrivateLinkResourceImpl implements PrivateLinkResource {
        private final com.azure.resourcemanager.appservice.models.PrivateLinkResource innerModel;

        protected PrivateLinkResourceImpl(com.azure.resourcemanager.appservice.models.PrivateLinkResource innerModel) {
            this.innerModel = innerModel;
        }

        @Override
        public String groupId() {
            return innerModel.properties().groupId();
        }

        @Override
        public List<String> requiredMemberNames() {
            return Collections.unmodifiableList(innerModel.properties().requiredMembers());
        }

        @Override
        public List<String> requiredDnsZoneNames() {
            return Collections.unmodifiableList(innerModel.properties().requiredZoneNames());
        }
    }

    protected static final class PrivateEndpointConnectionImpl implements PrivateEndpointConnection {
        private final RemotePrivateEndpointConnectionArmResourceInner innerModel;

        private final PrivateEndpoint privateEndpoint;
        private final com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkServiceConnectionState privateLinkServiceConnectionState;
        private final PrivateEndpointConnectionProvisioningState provisioningState;

        protected PrivateEndpointConnectionImpl(RemotePrivateEndpointConnectionArmResourceInner innerModel) {
            this.innerModel = innerModel;

            this.privateEndpoint
                = innerModel.privateEndpoint() == null ? null : new PrivateEndpoint(innerModel.privateEndpoint().id());
            this.privateLinkServiceConnectionState = innerModel.privateLinkServiceConnectionState() == null
                ? null
                : new com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkServiceConnectionState(
                    innerModel.privateLinkServiceConnectionState().status() == null
                        ? null
                        : com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointServiceConnectionStatus
                            .fromString(innerModel.privateLinkServiceConnectionState().status()),
                    innerModel.privateLinkServiceConnectionState().description(),
                    innerModel.privateLinkServiceConnectionState().actionsRequired());
            this.provisioningState = innerModel.provisioningState() == null
                ? null
                : PrivateEndpointConnectionProvisioningState.fromString(innerModel.provisioningState());
        }

        @Override
        public String id() {
            return innerModel.id();
        }

        @Override
        public String name() {
            return innerModel.name();
        }

        @Override
        public String type() {
            return innerModel.type();
        }

        @Override
        public PrivateEndpoint privateEndpoint() {
            return privateEndpoint;
        }

        @Override
        public com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkServiceConnectionState
            privateLinkServiceConnectionState() {
            return privateLinkServiceConnectionState;
        }

        @Override
        public PrivateEndpointConnectionProvisioningState provisioningState() {
            return provisioningState;
        }
    }
}
