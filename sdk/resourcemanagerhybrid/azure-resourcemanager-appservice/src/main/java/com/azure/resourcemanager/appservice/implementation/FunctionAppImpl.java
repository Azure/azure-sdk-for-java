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
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.models.HostKeysInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteLogsConfigInner;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionAuthenticationPolicy;
import com.azure.resourcemanager.appservice.models.FunctionDeploymentSlots;
import com.azure.resourcemanager.appservice.models.FunctionEnvelope;
import com.azure.resourcemanager.appservice.models.FunctionRuntimeStack;
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
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The implementation for FunctionApp. */
class FunctionAppImpl
    extends AppServiceBaseImpl<
        FunctionApp, FunctionAppImpl, FunctionApp.DefinitionStages.WithCreate, FunctionApp.Update>
    implements FunctionApp,
        FunctionApp.Definition,
        FunctionApp.DefinitionStages.NewAppServicePlanWithGroup,
        FunctionApp.DefinitionStages.ExistingLinuxPlanWithGroup,
        FunctionApp.Update {

    private final ClientLogger logger = new ClientLogger(getClass());

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
                throw logger.logExceptionAsError(new IllegalStateException(e));
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
    Mono<Indexable> submitAppSettings() {
        if (storageAccountCreatable != null && this.taskResult(storageAccountCreatable.key()) != null) {
            storageAccountToSet = this.taskResult(storageAccountCreatable.key());
        }
        if (storageAccountToSet == null) {
            return super.submitAppSettings();
        } else {
            return Flux
                .concat(
                    storageAccountToSet
                        .getKeysAsync()
                        .map(storageAccountKeys -> storageAccountKeys.get(0))
                        .zipWith(
                            this.manager().appServicePlans().getByIdAsync(this.appServicePlanId()),
                            (StorageAccountKey storageAccountKey, AppServicePlan appServicePlan) -> {
                                String connectionString = ResourceManagerUtils
                                    .getStorageConnectionString(storageAccountToSet.name(), storageAccountKey.value(),
                                        manager().environment());
                                addAppSettingIfNotModified(SETTING_WEB_JOBS_STORAGE, connectionString);
                                addAppSettingIfNotModified(SETTING_WEB_JOBS_DASHBOARD, connectionString);
                                if (OperatingSystem.WINDOWS.equals(operatingSystem())
                                    && // as Portal logic, only Windows plan would have following appSettings
                                    (appServicePlan == null
                                        || isConsumptionOrPremiumAppServicePlan(appServicePlan.pricingTier()))) {
                                    addAppSettingIfNotModified(
                                        SETTING_WEBSITE_CONTENTAZUREFILECONNECTIONSTRING, connectionString);
                                    addAppSettingIfNotModified(
                                        SETTING_WEBSITE_CONTENTSHARE,
                                        this.manager().resourceManager().internalContext()
                                            .randomResourceName(name(), 32));
                                }
                                return FunctionAppImpl.super.submitAppSettings();
                            }))
                .last()
                .then(
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
        if (description.tier().equalsIgnoreCase(SkuName.BASIC.toString())
            || description.tier().equalsIgnoreCase(SkuName.STANDARD.toString())
            || description.tier().equalsIgnoreCase(SkuName.PREMIUM.toString())
            || description.tier().equalsIgnoreCase(SkuName.PREMIUM_V2.toString())
            || description.tier().equalsIgnoreCase(PricingTier.PREMIUM_P1V3.toSkuDescription().tier()) // PremiumV3
        ) {
            return withWebAppAlwaysOn(true);
        } else {
            return withWebAppAlwaysOn(false);
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
        return super.withPrivateRegistryImage(imageAndTag, serverUrl);
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
    public Mono<FunctionApp> createAsync() {
        if (this.isInCreateMode()) {
            if (innerModel().serverFarmId() == null) {
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

    private static class FunctionKeyListResult {
        @JsonProperty("keys")
        private List<NameValuePair> keys;
    }

    private String getStorageAccountName() {
        return name().replaceAll("[^a-zA-Z0-9]", "");
    }

    /*
    private static final class FunctionCredential implements TokenCredential {
        private final FunctionAppImpl functionApp;

        private FunctionCredential(FunctionAppImpl functionApp) {
            this.functionApp = functionApp;
        }

        @Override
        public Mono<AccessToken> getToken(TokenRequestContext request) {
            return functionApp.manager().inner().getWebApps()
                    .getFunctionsAdminTokenAsync(functionApp.resourceGroupName(), functionApp.name())
                    .map(token -> {
                        String jwt = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
                        Pattern pattern = Pattern.compile("\"exp\": *([0-9]+),");
                        Matcher matcher = pattern.matcher(jwt);
                        matcher.find();
                        long expire = Long.parseLong(matcher.group(1));
                        return new AccessToken(token, OffsetDateTime.ofInstant(
                            Instant.ofEpochMilli(expire), ZoneOffset.UTC));
                    });
        }
    }
    */
}
