// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Headers;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.models.ConnectionStringDictionaryInner;
import com.azure.resourcemanager.appservice.fluent.models.HostKeysInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteConfigInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteLogsConfigInner;
import com.azure.resourcemanager.appservice.fluent.models.SitePatchResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.StringDictionaryInner;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.AppSetting;
import com.azure.resourcemanager.appservice.models.CsmDeploymentStatus;
import com.azure.resourcemanager.appservice.models.DeployOptions;
import com.azure.resourcemanager.appservice.models.DeployType;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionAuthenticationPolicy;
import com.azure.resourcemanager.appservice.models.FunctionDeploymentSlots;
import com.azure.resourcemanager.appservice.models.FunctionEnvelope;
import com.azure.resourcemanager.appservice.models.FunctionRuntimeStack;
import com.azure.resourcemanager.appservice.models.KuduDeploymentResult;
import com.azure.resourcemanager.appservice.models.NameValuePair;
import com.azure.resourcemanager.appservice.models.OperatingSystem;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.SkuDescription;
import com.azure.resourcemanager.appservice.models.SkuName;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.policy.AuthenticationPolicy;
import com.azure.resourcemanager.resources.fluentcore.policy.AuxiliaryAuthenticationPolicy;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** The implementation for FunctionApp. */
class FunctionAppImpl
    extends AppServiceBaseImpl<
        FunctionApp, FunctionAppImpl, FunctionApp.DefinitionStages.WithCreate, FunctionApp.Update>
    implements FunctionApp,
        FunctionApp.Definition,
        FunctionApp.DefinitionStages.NewAppServicePlanWithGroup,
        FunctionApp.DefinitionStages.ExistingLinuxPlanWithGroup,
        FunctionApp.Update {

    private static final ClientLogger LOGGER = new ClientLogger(FunctionAppImpl.class);

    private static final String SETTING_WEBSITE_CONTENTAZUREFILECONNECTIONSTRING =
        "WEBSITE_CONTENTAZUREFILECONNECTIONSTRING";
    private static final String SETTING_WEBSITE_CONTENTSHARE = "WEBSITE_CONTENTSHARE";
    private static final String SETTING_WEB_JOBS_STORAGE = "AzureWebJobsStorage";
    private static final String SETTING_WEB_JOBS_DASHBOARD = "AzureWebJobsDashboard";

    private Creatable<StorageAccount> storageAccountCreatable;
    private StorageAccount storageAccountToSet;
    private StorageAccount currentStorageAccount;
    private FunctionService functionService;
    private FunctionDeploymentSlots deploymentSlots;

    private String functionServiceHost;

    private Boolean appServicePlanIsFlexConsumption;

    FunctionAppImpl(
        final String name,
        SiteInner innerObject,
        SiteConfigResourceInner siteConfig,
        SiteLogsConfigInner logConfig,
        AppServiceManager manager) {
        super(name, innerObject, siteConfig, logConfig, manager);
        if (!isInCreateMode()) {
            initializeFunctionService();
        }
    }

    private void initializeFunctionService() {
        if (functionService == null) {
            UrlBuilder urlBuilder = UrlBuilder.parse(this.defaultHostname());
            String baseUrl;
            if (urlBuilder.getScheme() == null) {
                urlBuilder.setScheme("https");
            }
            try {
                baseUrl = urlBuilder.toUrl().toString();
            } catch (MalformedURLException e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(e));
            }

            List<HttpPipelinePolicy> policies = new ArrayList<>();
            for (int i = 0, count = manager().httpPipeline().getPolicyCount(); i < count; ++i) {
                HttpPipelinePolicy policy = manager().httpPipeline().getPolicy(i);
                if (!(policy instanceof AuthenticationPolicy)
                    && !(policy instanceof ProviderRegistrationPolicy)
                    && !(policy instanceof AuxiliaryAuthenticationPolicy)) {
                    policies.add(policy);
                }
            }
            policies.add(new FunctionAuthenticationPolicy(this));
            HttpPipeline httpPipeline = new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(manager().httpPipeline().getHttpClient())
                .build();
            functionServiceHost = baseUrl;
            functionService =
                RestProxy.create(FunctionService.class, httpPipeline,
                    SerializerFactory.createDefaultManagementSerializerAdapter());
        }
    }

    @Override
    public void setInner(SiteInner innerObject) {
        super.setInner(innerObject);
    }

    @Override
    public FunctionDeploymentSlots deploymentSlots() {
        if (deploymentSlots == null) {
            deploymentSlots = new FunctionDeploymentSlotsImpl(this);
        }
        return deploymentSlots;
    }

    @Override
    public FunctionAppImpl withNewConsumptionPlan() {
        return withNewAppServicePlan(OperatingSystem.WINDOWS, new PricingTier(SkuName.DYNAMIC.toString(), "Y1"));
    }

    @Override
    public FunctionAppImpl withNewConsumptionPlan(String appServicePlanName) {
        return withNewAppServicePlan(
            appServicePlanName, OperatingSystem.WINDOWS, new PricingTier(SkuName.DYNAMIC.toString(), "Y1"));
    }

    @Override
    public FunctionAppImpl withRuntime(String runtime) {
        return withAppSetting(SETTING_FUNCTIONS_WORKER_RUNTIME, runtime);
    }

    @Override
    public FunctionAppImpl withRuntimeVersion(String version) {
        return withAppSetting(SETTING_FUNCTIONS_EXTENSION_VERSION, version.startsWith("~") ? version : "~" + version);
    }

    @Override
    public FunctionAppImpl withLatestRuntimeVersion() {
        return withRuntimeVersion("latest");
    }

    @Override
    Mono<SiteInner> submitSite(SiteInner site) {
        if (isFunctionAppOnACA()) {
            return createOrUpdateInner(site);
        } else {
            return super.submitSite(site);
        }
    }

    @Override
    Mono<SiteInner> submitSite(SitePatchResourceInner siteUpdate) {
        if (isFunctionAppOnACA()) {
            return updateInner(siteUpdate);
        } else {
            return super.submitSite(siteUpdate);
        }
    }

    @Override
    Mono<Indexable> submitAppSettings() {
        if (storageAccountCreatable != null && this.taskResult(storageAccountCreatable.key()) != null) {
            storageAccountToSet = this.taskResult(storageAccountCreatable.key());
        }
        if (storageAccountToSet == null) {
            return super.submitAppSettings();
        } else {
            return storageAccountToSet
                .getKeysAsync()
                .flatMap(storageAccountKeys -> {
                    StorageAccountKey key = storageAccountKeys.get(0);
                    String connectionString = ResourceManagerUtils
                        .getStorageConnectionString(storageAccountToSet.name(), key.value(),
                            manager().environment());
                    addAppSettingIfNotModified(SETTING_WEB_JOBS_STORAGE, connectionString);
                    if (!isFunctionAppOnACA()) {
                        // Function App on ACA only supports Application Insights as log option.
                        // https://learn.microsoft.com/en-us/azure/azure-functions/functions-app-settings#azurewebjobsdashboard
                        addAppSettingIfNotModified(SETTING_WEB_JOBS_DASHBOARD, connectionString);
                        return this.manager().appServicePlans().getByIdAsync(this.appServicePlanId())
                            .flatMap(appServicePlan -> {
                                if (appServicePlan == null
                                    || isConsumptionOrPremiumAppServicePlan(appServicePlan.pricingTier())) {

                                    addAppSettingIfNotModified(
                                        SETTING_WEBSITE_CONTENTAZUREFILECONNECTIONSTRING, connectionString);
                                    addAppSettingIfNotModified(
                                        SETTING_WEBSITE_CONTENTSHARE,
                                        this.manager().resourceManager().internalContext()
                                            .randomResourceName(name(), 32));
                                }
                                return FunctionAppImpl.super.submitAppSettings();
                            });
                    } else {
                        return FunctionAppImpl.super.submitAppSettings();
                    }
                }).then(
                    Mono
                        .fromCallable(
                            () -> {
                                currentStorageAccount = storageAccountToSet;
                                storageAccountToSet = null;
                                storageAccountCreatable = null;
                                return this;
                            }));
        }
    }

    @Override
    public OperatingSystem operatingSystem() {
        if (isFunctionAppOnACA()) {
            // TODO(xiaofei) Current Function App on ACA only supports LINUX containers.
            //  This logic will change after service supports Windows containers.
            return OperatingSystem.LINUX;
        }
        return (innerModel().reserved() == null || !innerModel().reserved())
            ? OperatingSystem.WINDOWS : OperatingSystem.LINUX;
    }

    private void addAppSettingIfNotModified(String key, String value) {
        if (!appSettingModified(key)) {
            withAppSetting(key, value);
        }
    }

    private boolean appSettingModified(String key) {
        return (appSettingsToAdd != null && appSettingsToAdd.containsKey(key))
            || (appSettingsToRemove != null && appSettingsToRemove.contains(key));
    }

    private static boolean isConsumptionOrPremiumAppServicePlan(PricingTier pricingTier) {
        if (pricingTier == null || pricingTier.toSkuDescription() == null) {
            return true;
        }
        SkuDescription description = pricingTier.toSkuDescription();
        return SkuName.DYNAMIC.toString().equalsIgnoreCase(description.tier())
            || SkuName.ELASTIC_PREMIUM.toString().equalsIgnoreCase(description.tier());
    }

    @Override
    FunctionAppImpl withNewAppServicePlan(OperatingSystem operatingSystem, PricingTier pricingTier) {
        return super.withNewAppServicePlan(operatingSystem, pricingTier).autoSetAlwaysOn(pricingTier);
    }

    @Override
    FunctionAppImpl withNewAppServicePlan(
        String appServicePlan, OperatingSystem operatingSystem, PricingTier pricingTier) {
        return super.withNewAppServicePlan(appServicePlan, operatingSystem, pricingTier).autoSetAlwaysOn(pricingTier);
    }

    @Override
    public FunctionAppImpl withExistingAppServicePlan(AppServicePlan appServicePlan) {
        super.withExistingAppServicePlan(appServicePlan);
        return autoSetAlwaysOn(appServicePlan.pricingTier());
    }

    private FunctionAppImpl autoSetAlwaysOn(PricingTier pricingTier) {
        SkuDescription description = pricingTier.toSkuDescription();
        if (description.tier().equalsIgnoreCase(SkuName.FREE.toString())
            || description.tier().equalsIgnoreCase(SkuName.SHARED.toString())
            // consumption plan
            || description.tier().equalsIgnoreCase(SkuName.DYNAMIC.toString())
            // premium plan
            || description.tier().equalsIgnoreCase(SkuName.ELASTIC_PREMIUM.toString())
            || description.tier().equalsIgnoreCase(SkuName.ELASTIC_ISOLATED.toString())) {
            return withWebAppAlwaysOn(false);
        } else {
            return withWebAppAlwaysOn(true);
        }
    }

    @Override
    public FunctionAppImpl withNewStorageAccount(String name, StorageAccountSkuType sku) {
        StorageAccount.DefinitionStages.WithGroup storageDefine =
            manager().storageManager().storageAccounts().define(name).withRegion(regionName());
        if (super.creatableGroup != null && isInCreateMode()) {
            storageAccountCreatable =
                storageDefine
                    .withNewResourceGroup(super.creatableGroup)
                    .withGeneralPurposeAccountKindV2()
                    .withSku(sku);
        } else {
            storageAccountCreatable =
                storageDefine
                    .withExistingResourceGroup(resourceGroupName())
                    .withGeneralPurposeAccountKindV2()
                    .withSku(sku);
        }
        this.addDependency(storageAccountCreatable);
        return this;
    }

    @Override
    public FunctionAppImpl withNewStorageAccount(Creatable<StorageAccount> storageAccount) {
        storageAccountCreatable = storageAccount;
        this.addDependency(storageAccountCreatable);
        return this;
    }

    @Override
    public FunctionAppImpl withExistingStorageAccount(StorageAccount storageAccount) {
        this.storageAccountToSet = storageAccount;
        return this;
    }

    @Override
    public FunctionAppImpl withDailyUsageQuota(int quota) {
        innerModel().withDailyMemoryTimeQuota(quota);
        return this;
    }

    @Override
    public FunctionAppImpl withoutDailyUsageQuota() {
        return withDailyUsageQuota(0);
    }

    @Override
    public FunctionAppImpl withNewLinuxConsumptionPlan() {
        return withNewAppServicePlan(OperatingSystem.LINUX, new PricingTier(SkuName.DYNAMIC.toString(), "Y1"));
    }

    @Override
    public FunctionAppImpl withNewLinuxConsumptionPlan(String appServicePlanName) {
        return withNewAppServicePlan(
            appServicePlanName, OperatingSystem.LINUX, new PricingTier(SkuName.DYNAMIC.toString(), "Y1"));
    }

    @Override
    public FunctionAppImpl withNewLinuxAppServicePlan(PricingTier pricingTier) {
        return super.withNewAppServicePlan(OperatingSystem.LINUX, pricingTier);
    }

    @Override
    public FunctionAppImpl withNewLinuxAppServicePlan(String appServicePlanName, PricingTier pricingTier) {
        return super.withNewAppServicePlan(appServicePlanName, OperatingSystem.LINUX, pricingTier);
    }

    @Override
    public FunctionAppImpl withNewLinuxAppServicePlan(Creatable<AppServicePlan> appServicePlanCreatable) {
        super.withNewAppServicePlan(appServicePlanCreatable);
        if (appServicePlanCreatable instanceof AppServicePlan) {
            this.autoSetAlwaysOn(((AppServicePlan) appServicePlanCreatable).pricingTier());
        }
        return this;
    }

    @Override
    public FunctionAppImpl withExistingLinuxAppServicePlan(AppServicePlan appServicePlan) {
        return super.withExistingAppServicePlan(appServicePlan).autoSetAlwaysOn(appServicePlan.pricingTier());
    }

    @Override
    public FunctionAppImpl withBuiltInImage(final FunctionRuntimeStack runtimeStack) {
        ensureLinuxPlan();
        cleanUpContainerSettings();
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        withRuntime(runtimeStack.runtime());
        withRuntimeVersion(runtimeStack.version());
        siteConfig.withLinuxFxVersion(runtimeStack.getLinuxFxVersion());
        return this;
    }

    @Override
    public FunctionAppImpl withPublicDockerHubImage(String imageAndTag) {
        ensureLinuxPlan();
        return super.withPublicDockerHubImage(imageAndTag);
    }

    @Override
    public FunctionAppImpl withPrivateDockerHubImage(String imageAndTag) {
        ensureLinuxPlan();
        return super.withPublicDockerHubImage(imageAndTag);
    }

    @Override
    public FunctionAppImpl withPrivateRegistryImage(String imageAndTag, String serverUrl) {
        ensureLinuxPlan();
        super.withPrivateRegistryImage(imageAndTag, serverUrl);
        if (isFunctionAppOnACA()) {
            try {
                URL url = new URL(serverUrl);
                // remove URL protocol, as ACA don't allow that
                withAppSetting(SETTING_REGISTRY_SERVER, url.getAuthority() + url.getFile());
            } catch (MalformedURLException e) {
                // NO-OP, server url is not in URL format
            }
        }
        return this;
    }

    @Override
    protected void cleanUpContainerSettings() {
        if (siteConfig != null && siteConfig.linuxFxVersion() != null) {
            siteConfig.withLinuxFxVersion(null);
        }
        if (siteConfig != null && siteConfig.windowsFxVersion() != null) {
            siteConfig.withWindowsFxVersion(null);
        }
        // Docker Hub
        withoutAppSetting(SETTING_DOCKER_IMAGE);
        withoutAppSetting(SETTING_REGISTRY_SERVER);
        withoutAppSetting(SETTING_REGISTRY_USERNAME);
        withoutAppSetting(SETTING_REGISTRY_PASSWORD);
    }

    @Override
    protected OperatingSystem appServicePlanOperatingSystem(AppServicePlan appServicePlan) {
        // Consumption plan or premium (elastic) plan would have "functionapp" or "elastic" in "kind" property, no
        // "linux" in it.
        return (appServicePlan.innerModel().reserved() == null || !appServicePlan.innerModel().reserved())
            ? OperatingSystem.WINDOWS
            : OperatingSystem.LINUX;
    }

    @Override
    public StorageAccount storageAccount() {
        return currentStorageAccount;
    }

    @Override
    public String getMasterKey() {
        return getMasterKeyAsync().block();
    }

    @Override
    public Mono<String> getMasterKeyAsync() {
        return this.manager().serviceClient().getWebApps().listHostKeysAsync(resourceGroupName(), name())
            .map(HostKeysInner::masterKey);
    }

    @Override
    public PagedIterable<FunctionEnvelope> listFunctions() {
        return this.manager().functionApps().listFunctions(resourceGroupName(), name());
    }

    @Override
    public Map<String, String> listFunctionKeys(String functionName) {
        return listFunctionKeysAsync(functionName).block();
    }

    @Override
    public Mono<Map<String, String>> listFunctionKeysAsync(final String functionName) {
        return functionService
            .listFunctionKeys(functionServiceHost, functionName)
            .map(
                result -> {
                    Map<String, String> keys = new HashMap<>();
                    if (result.keys != null) {
                        for (NameValuePair pair : result.keys) {
                            keys.put(pair.name(), pair.value());
                        }
                    }
                    return keys;
                });
    }

    @Override
    public NameValuePair addFunctionKey(String functionName, String keyName, String keyValue) {
        return addFunctionKeyAsync(functionName, keyName, keyValue).block();
    }

    @Override
    public Mono<NameValuePair> addFunctionKeyAsync(String functionName, String keyName, String keyValue) {
        if (keyValue != null) {
            return functionService
                .addFunctionKey(
                    functionServiceHost,
                    functionName,
                    keyName,
                    new NameValuePair().withName(keyName).withValue(keyValue));
        } else {
            return functionService.generateFunctionKey(functionServiceHost, functionName, keyName);
        }
    }

    @Override
    public void removeFunctionKey(String functionName, String keyName) {
        removeFunctionKeyAsync(functionName, keyName).block();
    }

    @Override
    public Mono<Void> removeFunctionKeyAsync(String functionName, String keyName) {
        return functionService.deleteFunctionKey(functionServiceHost, functionName, keyName);
    }

    @Override
    public void triggerFunction(String functionName, Object payload) {
        triggerFunctionAsync(functionName, payload).block();
    }

    @Override
    public Mono<Void> triggerFunctionAsync(String functionName, Object payload) {
        return functionService.triggerFunction(functionServiceHost, functionName, payload);
    }

    @Override
    public void syncTriggers() {
        syncTriggersAsync().block();
    }

    @Override
    public Mono<Void> syncTriggersAsync() {
        return manager()
            .serviceClient()
            .getWebApps()
            .syncFunctionTriggersAsync(resourceGroupName(), name())
            .onErrorResume(
                throwable -> {
                    if (throwable instanceof ManagementException
                        && ((ManagementException) throwable).getResponse().getStatusCode() == 200) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                });
    }

    @Override
    public String managedEnvironmentId() {
        return innerModel().managedEnvironmentId();
    }

    @Override
    public Integer maxReplicas() {
        if (this.siteConfig == null) {
            return null;
        }
        return this.siteConfig.functionAppScaleLimit();
    }

    @Override
    public Integer minReplicas() {
        if (this.siteConfig == null) {
            return null;
        }
        return this.siteConfig.minimumElasticInstanceCount();
    }

    @Override
    public Flux<String> streamApplicationLogsAsync() {
        return functionService
            .ping(functionServiceHost)
            .then(functionService.getHostStatus(functionServiceHost))
            .thenMany(FunctionAppImpl.super.streamApplicationLogsAsync());
    }

    @Override
    public Flux<String> streamHttpLogsAsync() {
        return functionService
            .ping(functionServiceHost)
            .then(functionService.getHostStatus(functionServiceHost))
            .thenMany(FunctionAppImpl.super.streamHttpLogsAsync());
    }

    @Override
    public Flux<String> streamTraceLogsAsync() {
        return functionService
            .ping(functionServiceHost)
            .then(functionService.getHostStatus(functionServiceHost))
            .thenMany(FunctionAppImpl.super.streamTraceLogsAsync());
    }

    @Override
    public Flux<String> streamDeploymentLogsAsync() {
        return functionService
            .ping(functionServiceHost)
            .then(functionService.getHostStatus(functionServiceHost))
            .thenMany(FunctionAppImpl.super.streamDeploymentLogsAsync());
    }

    @Override
    public Flux<String> streamAllLogsAsync() {
        return functionService
            .ping(functionServiceHost)
            .then(functionService.getHostStatus(functionServiceHost))
            .thenMany(FunctionAppImpl.super.streamAllLogsAsync());
    }

    @Override
    public Mono<Void> zipDeployAsync(File zipFile) {
        try {
            return kuduClient.zipDeployAsync(zipFile);
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    @Override
    public void zipDeploy(File zipFile) {
        zipDeployAsync(zipFile).block();
    }

    @Override
    public Mono<Void> zipDeployAsync(InputStream zipFile, long length) {
        return kuduClient.zipDeployAsync(zipFile, length);
    }

    @Override
    public void zipDeploy(InputStream zipFile, long length) {
        zipDeployAsync(zipFile, length).block();
    }

    @Override
    public void beforeGroupCreateOrUpdate() {
        // special handling for Function App on ACA
        if (isFunctionAppOnACA()) {
            adaptForFunctionAppOnACA();
        }
        super.beforeGroupCreateOrUpdate();
    }

    private void adaptForFunctionAppOnACA() {
        this.innerModel().withReserved(null);
        if (this.siteConfig != null) {
            SiteConfigInner siteConfigInner = new SiteConfigInner();
            siteConfigInner.withLinuxFxVersion(this.siteConfig.linuxFxVersion());
            siteConfigInner.withMinimumElasticInstanceCount(this.siteConfig.minimumElasticInstanceCount());
            siteConfigInner.withFunctionAppScaleLimit(this.siteConfig.functionAppScaleLimit());
            siteConfigInner.withAppSettings(this.siteConfig.appSettings() == null ? new ArrayList<>() : this.siteConfig.appSettings());
            if (!appSettingsToAdd.isEmpty() || !appSettingsToRemove.isEmpty()) {
                for (String settingToRemove : appSettingsToRemove) {
                    siteConfigInner.appSettings().removeIf(kvPair -> Objects.equals(settingToRemove, kvPair.name()));
                }
                for (Map.Entry<String, String> entry : appSettingsToAdd.entrySet()) {
                    siteConfigInner.appSettings().add(new NameValuePair().withName(entry.getKey()).withValue(entry.getValue()));
                }
            }
            this.innerModel().withSiteConfig(siteConfigInner);
        }
    }

    @Override
    public Mono<FunctionApp> createAsync() {
        if (this.isInCreateMode()) {
            if (innerModel().serverFarmId() == null && !isFunctionAppOnACA()) {
                withNewConsumptionPlan();
            }
            if (currentStorageAccount == null && storageAccountToSet == null && storageAccountCreatable == null) {
                withNewStorageAccount(
                    this.manager().resourceManager().internalContext()
                        .randomResourceName(getStorageAccountName(), 20),
                    StorageAccountSkuType.STANDARD_LRS);
            }
        }
        return super.createAsync();
    }

    @Override
    public Mono<Void> afterPostRunAsync(final boolean isGroupFaulted) {
        if (!isGroupFaulted) {
            initializeFunctionService();
        }
        return super.afterPostRunAsync(isGroupFaulted);
    }

    @Override
    public FunctionAppImpl withManagedEnvironmentId(String managedEnvironmentId) {
        this.innerModel().withManagedEnvironmentId(managedEnvironmentId);
        if (!CoreUtils.isNullOrEmpty(managedEnvironmentId)) {
            this.innerModel().withKind("functionapp,linux,container,azurecontainerapps");
            if (this.siteConfig == null) {
                this.siteConfig = new SiteConfigResourceInner().withAppSettings(new ArrayList<>());
            }
        }
        return this;
    }

    @Override
    public FunctionAppImpl withMaxReplicas(int maxReplicas) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withFunctionAppScaleLimit(maxReplicas);
        return this;
    }

    @Override
    public FunctionAppImpl withMinReplicas(int minReplicas) {
        if (siteConfig == null) {
            siteConfig = new SiteConfigResourceInner();
        }
        siteConfig.withMinimumElasticInstanceCount(minReplicas);
        return this;
    }

    @Override
    public Mono<Map<String, AppSetting>> getAppSettingsAsync() {
        if (isFunctionAppOnACA()) {
            // current function app on ACA doesn't support deployment slot, so appSettings sticky is false
            return listAppSettings()
                .map(
                    appSettingsInner ->
                        appSettingsInner
                            .properties()
                            .entrySet()
                            .stream()
                            .collect(
                                Collectors
                                    .toMap(
                                        Map.Entry::getKey,
                                        entry ->
                                            new AppSettingImpl(
                                                entry.getKey(),
                                                entry.getValue(),
                                                false))));
        } else {
            return super.getAppSettingsAsync();
        }
    }

    /**
     * Whether this Function App is on Azure Container Apps environment.
     *
     * @return whether this Function App is on Azure Container Apps environment
     */
    boolean isFunctionAppOnACA() {
        return isFunctionAppOnACA(innerModel());
    }

    static boolean isFunctionAppOnACA(SiteInner siteInner) {
        return siteInner != null && !CoreUtils.isNullOrEmpty(siteInner.managedEnvironmentId());
    }

    @Override
    Mono<SiteInner> updateInner(SitePatchResourceInner siteUpdate) {
        Mono<SiteInner> updateInner = super.updateInner(siteUpdate);
        if (isFunctionAppOnACA()) {
            return RetryUtils.backoffRetryForFunctionAppAca(updateInner);
        } else {
            return updateInner;
        }
    }

    @Override
    Mono<SiteConfigResourceInner> createOrUpdateSiteConfig(SiteConfigResourceInner siteConfig) {
        Mono<SiteConfigResourceInner> createOrUpdateSiteConfig = super.createOrUpdateSiteConfig(siteConfig);
        if (isFunctionAppOnACA()) {
            return RetryUtils.backoffRetryForFunctionAppAca(createOrUpdateSiteConfig);
        } else {
            return createOrUpdateSiteConfig;
        }
    }

    @Override
    Mono<StringDictionaryInner> updateAppSettings(StringDictionaryInner inner) {
        Mono<StringDictionaryInner> updateAppSettings = super.updateAppSettings(inner);
        if (isFunctionAppOnACA()) {
            return RetryUtils.backoffRetryForFunctionAppAca(updateAppSettings);
        } else {
            return updateAppSettings;
        }
    }

    @Override
    Mono<ConnectionStringDictionaryInner> updateConnectionStrings(ConnectionStringDictionaryInner inner) {
        Mono<ConnectionStringDictionaryInner> updateConnectionStrings = super.updateConnectionStrings(inner);
        if (isFunctionAppOnACA()) {
            return RetryUtils.backoffRetryForFunctionAppAca(updateConnectionStrings);
        } else {
            return updateConnectionStrings;
        }
    }

    @Override
    public void deploy(DeployType type, File file) {
        deployAsync(type, file).block();
    }

    @Override
    public Mono<Void> deployAsync(DeployType type, File file) {
        return deployAsync(type, file, null);
    }

    @Override
    public void deploy(DeployType type, File file, DeployOptions deployOptions) {
        deployAsync(type, file, null).block();
    }

    @Override
    public Mono<Void> deployAsync(DeployType type, File file, DeployOptions deployOptions) {
        return this.pushDeployAsync(type, file, null)
            .flatMap(result -> kuduClient.pollDeploymentStatus(result));
    }

    @Override
    public void deploy(DeployType type, InputStream file, long length) {
        deployAsync(type, file, length).block();
    }

    @Override
    public Mono<Void> deployAsync(DeployType type, InputStream file, long length) {
        return deployAsync(type, file, length, null);
    }

    @Override
    public void deploy(DeployType type, InputStream file, long length, DeployOptions deployOptions) {
        deployAsync(type, file, length, null).block();
    }

    @Override
    public Mono<Void> deployAsync(DeployType type, InputStream file, long length, DeployOptions deployOptions) {
        return this.pushDeployAsync(type, file, length, null)
            .flatMap(result -> kuduClient.pollDeploymentStatus(result));
    }

    @Override
    public KuduDeploymentResult pushDeploy(DeployType type, File file, DeployOptions deployOptions) {
        return pushDeployAsync(type, file, deployOptions).block();
    }

    @Override
    public Mono<KuduDeploymentResult> pushDeployAsync(DeployType type, File file, DeployOptions deployOptions) {
        if (type != DeployType.ZIP) {
            return Mono.error(new IllegalArgumentException("Deployment to Function App supports ZIP package."));
        }
        return getAppServicePlanIsFlexConsumptionMono().flatMap(appServiceIsFlexConsumptionPlan -> {
            try {
                if (appServiceIsFlexConsumptionPlan) {
                    return kuduClient.pushDeployFlexConsumptionAsync(file);
                } else {
                    return kuduClient.pushZipDeployAsync(file)
                        .then(Mono.just(new KuduDeploymentResult("latest")));
                }
            } catch (IOException e) {
                return Mono.error(e);
            }
        });
    }

    private Mono<KuduDeploymentResult> pushDeployAsync(DeployType type, InputStream file, long length, DeployOptions deployOptions) {
        if (type != DeployType.ZIP) {
            return Mono.error(new IllegalArgumentException("Deployment to Function App supports ZIP package."));
        }
        return getAppServicePlanIsFlexConsumptionMono().flatMap(appServiceIsFlexConsumptionPlan -> {
            try {
                if (appServiceIsFlexConsumptionPlan) {
                    return kuduClient.pushDeployFlexConsumptionAsync(file, length);
                } else {
                    return kuduClient.pushZipDeployAsync(file, length)
                        .then(Mono.just(new KuduDeploymentResult("latest")));
                }
            } catch (IOException e) {
                return Mono.error(e);
            }
        });
    }

    private Mono<Boolean> getAppServicePlanIsFlexConsumptionMono() {
        Mono<Boolean> updateAppServicePlan = Mono.justOrEmpty(appServicePlanIsFlexConsumption);
        if (appServicePlanIsFlexConsumption == null) {
            updateAppServicePlan = Mono.defer(
                () -> manager().appServicePlans()
                    .getByIdAsync(this.appServicePlanId())
                    .map(appServicePlan -> {
                        appServicePlanIsFlexConsumption = "FlexConsumption".equals(appServicePlan.pricingTier().toSkuDescription().tier());
                        return appServicePlanIsFlexConsumption;
                    }));
        }
        return updateAppServicePlan;
    }

    @Override
    public CsmDeploymentStatus getDeploymentStatus(String deploymentId) {
        return getDeploymentStatusAsync(deploymentId).block();
    }

    @Override
    public Mono<CsmDeploymentStatus> getDeploymentStatusAsync(String deploymentId) {
        // "GET" LRO is not supported in azure-core
        SerializerAdapter serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
        return this.manager().serviceClient().getWebApps()
            .getProductionSiteDeploymentStatusWithResponseAsync(this.resourceGroupName(), this.name(), deploymentId)
            .flatMap(fluxResponse -> {
                HttpResponse response = new HttpFluxBBResponse(fluxResponse);
                return response.getBodyAsString()
                    .flatMap(bodyString -> {
                        CsmDeploymentStatus status;
                        try {
                            status = serializerAdapter.deserialize(bodyString, CsmDeploymentStatus.class, SerializerEncoding.JSON);
                        } catch (IOException e) {
                            return Mono.error(new ManagementException("Deserialize failed for response body.", response));
                        }
                        return Mono.justOrEmpty(status);
                    }).doFinally(ignored -> response.close());
            });
    }

    @Host("{$host}")
    @ServiceInterface(name = "FunctionService")
    private interface FunctionService {
        @Headers({
            "Accept: application/json",
            "Content-Type: application/json; charset=utf-8"
        })
        @Get("admin/functions/{name}/keys")
        Mono<FunctionKeyListResult> listFunctionKeys(
            @HostParam("$host") String host, @PathParam("name") String functionName);

        @Headers({
            "Accept: application/json",
            "Content-Type: application/json; charset=utf-8"
        })
        @Put("admin/functions/{name}/keys/{keyName}")
        Mono<NameValuePair> addFunctionKey(
            @HostParam("$host") String host,
            @PathParam("name") String functionName,
            @PathParam("keyName") String keyName,
            @BodyParam("application/json") NameValuePair key);

        @Headers({
            "Accept: application/json",
            "Content-Type: application/json; charset=utf-8"
        })
        @Post("admin/functions/{name}/keys/{keyName}")
        Mono<NameValuePair> generateFunctionKey(
            @HostParam("$host") String host,
            @PathParam("name") String functionName,
            @PathParam("keyName") String keyName);

        @Headers({
            "Content-Type: application/json; charset=utf-8"
        })
        @Delete("admin/functions/{name}/keys/{keyName}")
        Mono<Void> deleteFunctionKey(
            @HostParam("$host") String host,
            @PathParam("name") String functionName,
            @PathParam("keyName") String keyName);

        @Headers({
            "Content-Type: application/json; charset=utf-8"
        })
        @Post("admin/host/ping")
        Mono<Void> ping(@HostParam("$host") String host);

        @Headers({
            "Content-Type: application/json; charset=utf-8"
        })
        @Get("admin/host/status")
        Mono<Void> getHostStatus(@HostParam("$host") String host);

        @Headers({
            "Content-Type: application/json; charset=utf-8"
        })
        @Post("admin/functions/{name}")
        Mono<Void> triggerFunction(
            @HostParam("$host") String host,
            @PathParam("name") String functionName,
            @BodyParam("application/json") Object payload);
    }

    private static class FunctionKeyListResult implements JsonSerializable<FunctionKeyListResult> {
        private List<NameValuePair> keys;

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter
                .writeStartObject()
                .writeArrayField("keys", keys, JsonWriter::writeJson)
                .writeEndObject();
        }

        public static FunctionKeyListResult fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                FunctionKeyListResult result = new FunctionKeyListResult();
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("keys".equals(fieldName)) {
                        List<NameValuePair> keys = reader.readArray(reader1 ->
                            reader1.readObject(NameValuePair::fromJson));
                        result.keys = keys;
                    } else {
                        reader.skipChildren();
                    }
                }
                return result;
            });
        }
    }

    private String getStorageAccountName() {
        return name().replaceAll("[^a-zA-Z0-9]", "");
    }
}
