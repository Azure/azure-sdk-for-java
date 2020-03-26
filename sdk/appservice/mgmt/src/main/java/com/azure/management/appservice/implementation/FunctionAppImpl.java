/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Headers;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.management.CloudException;
import com.azure.core.util.UrlBuilder;
import com.azure.management.RestClient;
import com.azure.management.appservice.AppServicePlan;
import com.azure.management.appservice.FunctionApp;
import com.azure.management.appservice.FunctionDeploymentSlots;
import com.azure.management.appservice.FunctionRuntimeStack;
import com.azure.management.appservice.NameValuePair;
import com.azure.management.appservice.OperatingSystem;
import com.azure.management.appservice.PricingTier;
import com.azure.management.appservice.SkuDescription;
import com.azure.management.appservice.SkuName;
import com.azure.management.appservice.models.SiteConfigResourceInner;
import com.azure.management.appservice.models.SiteInner;
import com.azure.management.appservice.models.SiteLogsConfigInner;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.model.Indexable;
import com.azure.management.storage.StorageAccount;
import com.azure.management.storage.StorageAccountKey;
import com.azure.management.storage.StorageAccountSkuType;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The implementation for FunctionApp.
 */
class FunctionAppImpl
    extends AppServiceBaseImpl<FunctionApp, FunctionAppImpl, FunctionApp.DefinitionStages.WithCreate, FunctionApp.Update>
    implements
        FunctionApp,
        FunctionApp.Definition,
        FunctionApp.DefinitionStages.NewAppServicePlanWithGroup,
        FunctionApp.DefinitionStages.ExistingLinuxPlanWithGroup,
        FunctionApp.Update {

    private static final String SETTING_FUNCTIONS_WORKER_RUNTIME = "FUNCTIONS_WORKER_RUNTIME";
    private static final String SETTING_FUNCTIONS_EXTENSION_VERSION = "FUNCTIONS_EXTENSION_VERSION";
    private static final String SETTING_WEBSITE_CONTENTAZUREFILECONNECTIONSTRING = "WEBSITE_CONTENTAZUREFILECONNECTIONSTRING";
    private static final String SETTING_WEBSITE_CONTENTSHARE = "WEBSITE_CONTENTSHARE";
    private static final String SETTING_WEB_JOBS_STORAGE = "AzureWebJobsStorage";
    private static final String SETTING_WEB_JOBS_DASHBOARD = "AzureWebJobsDashboard";

    private Creatable<StorageAccount> storageAccountCreatable;
    private StorageAccount storageAccountToSet;
    private StorageAccount currentStorageAccount;
    private final FunctionAppKeyService functionAppKeyService;
    private FunctionService functionService;
    private FunctionDeploymentSlots deploymentSlots;

    private Function<AppServicePlan, SiteConfigResourceInner> linuxFxVersionSetter = null;
    private Mono<AppServicePlan> cachedAppServicePlanObservable = null; // potentially shared between submitSiteConfig and submitAppSettings

    private String functionAppKeyServiceHost;
    private String functionServiceHost;

    FunctionAppImpl(final String name, SiteInner innerObject, SiteConfigResourceInner siteConfig, SiteLogsConfigInner logConfig, AppServiceManager manager) {
        super(name, innerObject, siteConfig, logConfig, manager);
        functionAppKeyServiceHost = manager.restClient().getBaseUrl().toString();
        functionAppKeyService = RestProxy.create(FunctionAppKeyService.class, manager.restClient().getHttpPipeline());
        if (!isInCreateMode()) {
            initializeFunctionService();
        }
    }

    private void initializeFunctionService() {
        if (functionService == null) {
            UrlBuilder urlBuilder = UrlBuilder.parse(defaultHostName());
            String baseUrl;
            if (urlBuilder.getScheme() == null) {
                urlBuilder.setScheme("https");
            }
            try {
                baseUrl = urlBuilder.toUrl().toString();
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
            RestClient client = manager().restClient().newBuilder()
//            RestClient client = new RestClientBuilder()
                    .withBaseUrl(baseUrl)
                    .withCredential(new FunctionCredential(this))
//                    .withPolicy(new FunctionAuthenticationPolicy(this))
                    .withHttpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                    .buildClient();
            functionServiceHost = client.getBaseUrl().toString();
            functionService = RestProxy.create(FunctionService.class, client.getHttpPipeline(), client.getSerializerAdapter());
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
        return withNewAppServicePlan(appServicePlanName, OperatingSystem.WINDOWS, new PricingTier(SkuName.DYNAMIC.toString(), "Y1"));
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
    Mono<Indexable> submitSiteConfig() {
        if (linuxFxVersionSetter != null) {
            cachedAppServicePlanObservable = this.cachedAppServicePlanObservable(); // first usage, so get a new one
            return cachedAppServicePlanObservable
                    .map(linuxFxVersionSetter)
                    .flatMap(ignored -> FunctionAppImpl.super.submitSiteConfig());
        } else {
            return super.submitSiteConfig();
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
            if (cachedAppServicePlanObservable == null) {
                cachedAppServicePlanObservable = this.cachedAppServicePlanObservable();
            }
            return Flux.concat(storageAccountToSet.getKeysAsync()
                    .map(storageAccountKeys -> storageAccountKeys.get(0))
                    .zipWith(cachedAppServicePlanObservable, (StorageAccountKey storageAccountKey, AppServicePlan appServicePlan) -> {
                        String connectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s",
                                storageAccountToSet.name(), storageAccountKey.getValue());
                        addAppSettingIfNotModified(SETTING_WEB_JOBS_STORAGE, connectionString);
                        addAppSettingIfNotModified(SETTING_WEB_JOBS_DASHBOARD, connectionString);
                        if (OperatingSystem.WINDOWS.equals(operatingSystem()) && // as Portal logic, only Windows plan would have following appSettings
                                (appServicePlan == null || isConsumptionOrPremiumAppServicePlan(appServicePlan.pricingTier()))) {
                            addAppSettingIfNotModified(SETTING_WEBSITE_CONTENTAZUREFILECONNECTIONSTRING, connectionString);
                            addAppSettingIfNotModified(SETTING_WEBSITE_CONTENTSHARE, this.manager().getSdkContext().randomResourceName(name(), 32));
                        }
                        return FunctionAppImpl.super.submitAppSettings();
                    })).last()
                    .then(Mono.fromCallable(() -> {
                        currentStorageAccount = storageAccountToSet;
                        storageAccountToSet = null;
                        storageAccountCreatable = null;
                        cachedAppServicePlanObservable = null;
                        return this;
                    }));
        }
    }

    @Override
    public OperatingSystem operatingSystem() {
        return (inner().reserved() == null || !inner().reserved())
                ? OperatingSystem.WINDOWS
                : OperatingSystem.LINUX;
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
        return SkuName.DYNAMIC.toString().equalsIgnoreCase(description.tier()) || SkuName.ELASTIC_PREMIUM.toString().equalsIgnoreCase(description.tier());
    }

    private static boolean isConsumptionPlan(PricingTier pricingTier) {
        if (pricingTier == null || pricingTier.toSkuDescription() == null) {
            return true;
        }
        SkuDescription description = pricingTier.toSkuDescription();
        return SkuName.DYNAMIC.toString().equalsIgnoreCase(description.tier());
    }

    @Override
    FunctionAppImpl withNewAppServicePlan(OperatingSystem operatingSystem, PricingTier pricingTier) {
        return super.withNewAppServicePlan(operatingSystem, pricingTier).autoSetAlwaysOn(pricingTier);
    }

    @Override
    FunctionAppImpl withNewAppServicePlan(String appServicePlan, OperatingSystem operatingSystem, PricingTier pricingTier) {
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
                || description.tier().equalsIgnoreCase(SkuName.PREMIUM_V2.toString())) {
            return withWebAppAlwaysOn(true);
        } else {
            return withWebAppAlwaysOn(false);
        }
    }

    @Override
    public FunctionAppImpl withNewStorageAccount(String name, com.azure.management.storage.SkuName sku) {
        StorageAccount.DefinitionStages.WithGroup storageDefine = manager().storageManager().storageAccounts()
            .define(name)
            .withRegion(regionName());
        if (super.creatableGroup != null && isInCreateMode()) {
            storageAccountCreatable = storageDefine.withNewResourceGroup(super.creatableGroup)
                .withGeneralPurposeAccountKind()
                .withSku(sku);
        } else {
            storageAccountCreatable = storageDefine.withExistingResourceGroup(resourceGroupName())
                .withGeneralPurposeAccountKind()
                .withSku(sku);
        }
        this.addDependency(storageAccountCreatable);
        return this;
    }

    @Override
    public FunctionAppImpl withNewStorageAccount(String name, StorageAccountSkuType sku) {
        StorageAccount.DefinitionStages.WithGroup storageDefine = manager().storageManager().storageAccounts()
                .define(name)
                .withRegion(regionName());
        if (super.creatableGroup != null && isInCreateMode()) {
            storageAccountCreatable = storageDefine.withNewResourceGroup(super.creatableGroup)
                    .withGeneralPurposeAccountKind()
                    .withSku(sku);
        } else {
            storageAccountCreatable = storageDefine.withExistingResourceGroup(resourceGroupName())
                    .withGeneralPurposeAccountKind()
                    .withSku(sku);
        }
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
        inner().withDailyMemoryTimeQuota(quota);
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
        return withNewAppServicePlan(appServicePlanName, OperatingSystem.LINUX, new PricingTier(SkuName.DYNAMIC.toString(), "Y1"));
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
        linuxFxVersionSetter = appServicePlan -> {
            if (appServicePlan == null || isConsumptionPlan(appServicePlan.pricingTier())) {
                siteConfig.withLinuxFxVersion(runtimeStack.getLinuxFxVersionForConsumptionPlan());
            } else {
                siteConfig.withLinuxFxVersion(runtimeStack.getLinuxFxVersionForDedicatedPlan());
            }
            return siteConfig;
        };
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
        linuxFxVersionSetter = null;
        if (siteConfig != null && siteConfig.linuxFxVersion() != null) {
            siteConfig.withLinuxFxVersion(null);
        }
        // Docker Hub
        withoutAppSetting(SETTING_DOCKER_IMAGE);
        withoutAppSetting(SETTING_REGISTRY_SERVER);
        withoutAppSetting(SETTING_REGISTRY_USERNAME);
        withoutAppSetting(SETTING_REGISTRY_PASSWORD);
    }

    @Override
    protected OperatingSystem appServicePlanOperatingSystem(AppServicePlan appServicePlan) {
        // Consumption plan or premium (elastic) plan would have "functionapp" or "elastic" in "kind" property, no "linux" in it.
        return (appServicePlan.inner().reserved() == null || !appServicePlan.inner().reserved())
                ? OperatingSystem.WINDOWS
                : OperatingSystem.LINUX;
    }

    private Mono<AppServicePlan> cachedAppServicePlanObservable() {
        // it could get more than one subscriber, so hot observable + caching
        return this.manager().appServicePlans().getByIdAsync(this.appServicePlanId()).cache();
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
        return functionAppKeyService.listKeys(functionAppKeyServiceHost, resourceGroupName(), name(), manager().getSubscriptionId(), "2019-08-01", manager().inner().userAgent())
                .map(ListKeysResult::getMasterKey);
    }

    @Override
    public Map<String, String> listFunctionKeys(String functionName) {
        return listFunctionKeysAsync(functionName).block();
    }

    @Override
    public Mono<Map<String, String>> listFunctionKeysAsync(final String functionName) {
        return functionService.listFunctionKeys(functionServiceHost, functionName)
                .map(result -> {
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
            return functionService.addFunctionKey(functionServiceHost, functionName, keyName, new NameValuePair().withName(keyName).withValue(keyValue));
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
    public void syncTriggers() {
        syncTriggersAsync().block();
    }

    @Override
    public Mono<Void> syncTriggersAsync() {
        return manager().inner().webApps().syncFunctionTriggersAsync(resourceGroupName(), name())
                .onErrorResume(throwable -> {
                    if (throwable instanceof CloudException && ((CloudException) throwable).getResponse().getStatusCode() == 200) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                });
    }

    @Override
    public Flux<String> streamApplicationLogsAsync() {
        return functionService.ping(functionServiceHost)
                .then(functionService.getHostStatus(functionServiceHost))
                .thenMany(FunctionAppImpl.super.streamApplicationLogsAsync());
    }

    @Override
    public Flux<String> streamHttpLogsAsync() {
        return functionService.ping(functionServiceHost)
                .then(functionService.getHostStatus(functionServiceHost))
                .thenMany(FunctionAppImpl.super.streamHttpLogsAsync());
    }

    @Override
    public Flux<String> streamTraceLogsAsync() {
        return functionService.ping(functionServiceHost)
                .then(functionService.getHostStatus(functionServiceHost))
                .thenMany(FunctionAppImpl.super.streamTraceLogsAsync());
    }

    @Override
    public Flux<String> streamDeploymentLogsAsync() {
        return functionService.ping(functionServiceHost)
                .then(functionService.getHostStatus(functionServiceHost))
                .thenMany(FunctionAppImpl.super.streamDeploymentLogsAsync());
    }

    @Override
    public Flux<String> streamAllLogsAsync() {
        return functionService.ping(functionServiceHost)
                .then(functionService.getHostStatus(functionServiceHost))
                .thenMany(FunctionAppImpl.super.streamAllLogsAsync());
    }

    @Override
    public Mono<Void> zipDeployAsync(File zipFile) {
        try {
            return zipDeployAsync(new FileInputStream(zipFile));
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    @Override
    public void zipDeploy(File zipFile) {
        zipDeployAsync(zipFile).block();
    }

    @Override
    public Mono<Void> zipDeployAsync(InputStream zipFile) {
        return kuduClient.zipDeployAsync(zipFile);
    }

    @Override
    public void zipDeploy(InputStream zipFile) {
        zipDeployAsync(zipFile).block();
    }

    @Override
    public Flux<Indexable> createAsync() {
        if (this.isInCreateMode()) {
            if (inner().serverFarmId() == null) {
                withNewConsumptionPlan();
            }
            if (currentStorageAccount == null && storageAccountToSet == null && storageAccountCreatable == null) {
                withNewStorageAccount(this.manager().getSdkContext().randomResourceName(name(), 20), com.azure.management.storage.SkuName.STANDARD_GRS);
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

    private static class ListKeysResult {
        @JsonProperty("masterKey")
        private String masterKey;

        @JsonProperty("functionKeys")
        private Map<String, String> functionKeys;

        @JsonProperty("systemKeys")
        private Map<String, String> systemKeys;

        public String getMasterKey() {
            return masterKey;
        }
    }

    @Host("{$host}")
    @ServiceInterface(name = "FunctionAppKeyService")
    private interface FunctionAppKeyService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps listKeys" })
        @Post("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/sites/{name}/host/default/listkeys")
        Mono<ListKeysResult> listKeys(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion, @HeaderParam("User-Agent") String userAgent);
    }

    @Host("{$host}")
    @ServiceInterface(name = "FunctionService")
    private interface FunctionService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps listFunctionKeys" })
        @Get("admin/functions/{name}/keys")
        Mono<FunctionKeyListResult> listFunctionKeys(@HostParam("$host") String host, @PathParam("name") String functionName);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps addFunctionKey" })
        @Put("admin/functions/{name}/keys/{keyName}")
        Mono<NameValuePair> addFunctionKey(@HostParam("$host") String host, @PathParam("name") String functionName, @PathParam("keyName") String keyName, @BodyParam("application/json") NameValuePair key);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps generateFunctionKey" })
        @Post("admin/functions/{name}/keys/{keyName}")
        Mono<NameValuePair> generateFunctionKey(@HostParam("$host") String host, @PathParam("name") String functionName, @PathParam("keyName") String keyName);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps deleteFunctionKey" })
        @Delete("admin/functions/{name}/keys/{keyName}")
        Mono<Void> deleteFunctionKey(@HostParam("$host") String host, @PathParam("name") String functionName, @PathParam("keyName") String keyName);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps ping" })
        @Post("admin/host/ping")
        Mono<Void> ping(@HostParam("$host") String host);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps getHostStatus" })
        @Get("admin/host/status")
        Mono<Void> getHostStatus(@HostParam("$host") String host);
    }

    private static class FunctionKeyListResult {
        @JsonProperty("keys")
        private List<NameValuePair> keys;
    }

    private static final class FunctionAuthenticationPolicy implements HttpPipelinePolicy {
        private final FunctionAppImpl functionApp;
        private final static String HEADER_NAME = "x-functions-key";
        private String masterKey;

        private FunctionAuthenticationPolicy(FunctionAppImpl functionApp) {
            this.functionApp = functionApp;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            Mono<String> masterKeyMono = masterKey == null
                    ? functionApp.getMasterKeyAsync().map(key -> { masterKey = key; return key; })
                    : Mono.just(masterKey);
            return masterKeyMono.flatMap(key -> {
                context.getHttpRequest().setHeader(HEADER_NAME, key);
                return next.process();
            });
        }
    }

    private static final class FunctionCredential implements TokenCredential {
        private final FunctionAppImpl functionApp;

        private FunctionCredential(FunctionAppImpl functionApp) {
            this.functionApp = functionApp;
        }

        @Override
        public Mono<AccessToken> getToken(TokenRequestContext request) {
            return functionApp.manager().inner().webApps()
                    .getFunctionsAdminTokenAsync(functionApp.resourceGroupName(), functionApp.name())
                    .map(token -> {
                        String jwt = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
                        Pattern pattern = Pattern.compile("\"exp\": *([0-9]+),");
                        Matcher matcher = pattern.matcher(jwt);
                        matcher.find();
                        long expire = Long.parseLong(matcher.group(1));
                        return new AccessToken(token, OffsetDateTime.ofInstant(Instant.ofEpochMilli(expire), ZoneOffset.UTC));
                    });
        }
    }
}
