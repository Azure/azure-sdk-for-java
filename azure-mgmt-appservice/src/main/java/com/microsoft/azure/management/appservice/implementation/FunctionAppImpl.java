/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.BaseEncoding;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.NameValuePair;
import com.microsoft.azure.management.appservice.OperatingSystem;
import com.microsoft.azure.management.appservice.PricingTier;
import com.microsoft.azure.management.appservice.SkuDescription;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.storage.SkuName;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import com.microsoft.rest.credentials.TokenCredentials;
import okhttp3.Request;
import org.joda.time.DateTime;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Completable;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The implementation for FunctionApp.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class FunctionAppImpl
    extends AppServiceBaseImpl<FunctionApp, FunctionAppImpl, FunctionApp.DefinitionStages.WithCreate, FunctionApp.Update>
    implements
        FunctionApp,
        FunctionApp.Definition,
        FunctionApp.DefinitionStages.NewAppServicePlanWithGroup,
        FunctionApp.Update {

    private Creatable<StorageAccount> storageAccountCreatable;
    private StorageAccount storageAccountToSet;
    private StorageAccount currentStorageAccount;
    private final FunctionAppKeyService functionAppKeyService;
    private final FunctionKuduService functionKuduService;

    FunctionAppImpl(final String name, SiteInner innerObject, SiteConfigResourceInner configObject, AppServiceManager manager) {
        super(name, innerObject, configObject, manager);
        innerObject.withKind("functionapp");
        functionAppKeyService = manager.restClient().retrofit().create(FunctionAppKeyService.class);
        String defaultHostName = defaultHostName().startsWith("http") ? defaultHostName() : "http://" + defaultHostName();
        functionKuduService = manager.restClient().newBuilder()
                .withBaseUrl(defaultHostName)
                .withCredentials(new KuduCredentials(this))
                .build()
                .retrofit().create(FunctionKuduService.class);
    }

    @Override
    public FunctionAppImpl withNewConsumptionPlan() {
        return withNewAppServicePlan(OperatingSystem.WINDOWS, new PricingTier("Dynamic", "Y1"));
    }

    @Override
    public FunctionAppImpl withRuntimeVersion(String version) {
        return withAppSetting("FUNCTIONS_EXTENSION_VERSION", version.startsWith("~") ? version : "~" + version);
    }

    @Override
    public FunctionAppImpl withLatestRuntimeVersion() {
        return withRuntimeVersion("latest");
    }

    @Override
    Observable<SiteInner> submitAppSettings(final SiteInner site) {
        if (storageAccountCreatable != null && createdResource(storageAccountCreatable.key()) != null) {
            storageAccountToSet = (StorageAccount) createdResource(storageAccountCreatable.key());
        }
        if (storageAccountToSet == null) {
            return super.submitAppSettings(site);
        } else {
            return storageAccountToSet.getKeysAsync()
                .flatMapIterable(new Func1<List<StorageAccountKey>, Iterable<StorageAccountKey>>() {
                    @Override
                    public Iterable<StorageAccountKey> call(List<StorageAccountKey> storageAccountKeys) {
                        return storageAccountKeys;
                    }
                })
                .first().flatMap(new Func1<StorageAccountKey, Observable<SiteInner>>() {
                @Override
                public Observable<SiteInner> call(StorageAccountKey storageAccountKey) {
                    String connectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s",
                        storageAccountToSet.name(), storageAccountKey.value());
                    withAppSetting("AzureWebJobsStorage", connectionString);
                    withAppSetting("AzureWebJobsDashboard", connectionString);
                    withAppSetting("WEBSITE_CONTENTAZUREFILECONNECTIONSTRING", connectionString);
                    withAppSetting("WEBSITE_CONTENTSHARE", SdkContext.randomResourceName(name(), 32));
                    return FunctionAppImpl.super.submitAppSettings(site);
                }
            }).doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        currentStorageAccount = storageAccountToSet;
                        storageAccountToSet = null;
                        storageAccountCreatable = null;
                    }
                });
        }
    }

    @Override
    FunctionAppImpl withNewAppServicePlan(OperatingSystem operatingSystem, PricingTier pricingTier) {
        return super.withNewAppServicePlan(operatingSystem, pricingTier).autoSetAlwaysOn(pricingTier);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FunctionAppImpl withExistingAppServicePlan(AppServicePlan appServicePlan) {
        super.withExistingAppServicePlan(appServicePlan);
        return autoSetAlwaysOn(appServicePlan.pricingTier());
    }

    private FunctionAppImpl autoSetAlwaysOn(PricingTier pricingTier) {
        SkuDescription description = pricingTier.toSkuDescription();
        if (description.tier().equalsIgnoreCase("Basic")
                || description.tier().equalsIgnoreCase("Standard")
                || description.tier().equalsIgnoreCase("Premium")) {
            return withWebAppAlwaysOn(true);
        } else {
            return withWebAppAlwaysOn(false);
        }
    }

    @Override
    public FunctionAppImpl withNewStorageAccount(String name, SkuName sku) {
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
        addCreatableDependency(storageAccountCreatable);
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
    public StorageAccount storageAccount() {
        return currentStorageAccount;
    }

    @Override
    public String getMasterKey() {
        return getMasterKeyAsync().toBlocking().single();
    }

    @Override
    public Observable<String> getMasterKeyAsync() {
        return functionAppKeyService.getMasterKey(resourceGroupName(), name(), manager().subscriptionId(), "2016-08-01", manager().inner().userAgent())
                .map(new Func1<Map<String, String>, String>() {
                    @Override
                    public String call(Map<String, String> stringStringMap) {
                        return stringStringMap.get("masterKey");
                    }
                });
    }

    @Override
    public Map<String, String> listFunctionKeys(String functionName) {
        return listFunctionKeysAsync(functionName).toBlocking().single();
    }

    @Override
    public Observable<Map<String, String>> listFunctionKeysAsync(final String functionName) {
        return functionKuduService.listFunctionKeys(functionName)
                .map(new Func1<FunctionKeyListResult, Map<String, String>>() {
                    @Override
                    public Map<String, String> call(FunctionKeyListResult result) {
                        Map<String, String> keys = new HashMap<String, String>();
                        if (result.keys != null) {
                            for (NameValuePair pair : result.keys) {
                                keys.put(pair.name(), pair.value());
                            }
                        }
                        return keys;
                    }
                });
    }

    @Override
    public NameValuePair addFunctionKey(String functionName, String keyName, String keyValue) {
        return addFunctionKeyAsync(functionName, keyName, keyValue).toBlocking().single();
    }

    @Override
    public Observable<NameValuePair> addFunctionKeyAsync(String functionName, String keyName, String keyValue) {
        if (keyValue != null) {
            return functionKuduService.addFunctionKey(functionName, keyName, new NameValuePair().withName(keyName).withValue(keyValue));
        } else {
            return functionKuduService.generateFunctionKey(functionName, keyName);
        }
    }

    @Override
    public void removeFunctionKey(String functionName, String keyName) {
        removeFunctionKeyAsync(functionName, keyName).toObservable().toBlocking().subscribe();
    }

    @Override
    public Completable removeFunctionKeyAsync(String functionName, String keyName) {
        return functionKuduService.deleteFunctionKey(functionName, keyName).toCompletable();
    }

    @Override
    public void syncTriggers() {
        syncTriggersAsync().toObservable().toBlocking().subscribe();
    }

    @Override
    public Completable syncTriggersAsync() {
        return manager().inner().webApps().syncFunctionTriggersAsync(resourceGroupName(), name()).toCompletable()
                .onErrorResumeNext(new Func1<Throwable, Completable>() {
                    @Override
                    public Completable call(Throwable throwable) {
                        if (throwable instanceof CloudException && ((CloudException) throwable).response().code() == 200) {
                            return Completable.complete();
                        } else {
                            return Completable.error(throwable);
                        }
                    }
                });
    }

    @Override
    public Observable<Indexable> createAsync() {
        if (inner().serverFarmId() == null) {
            withNewConsumptionPlan();
        }
        if (currentStorageAccount == null && storageAccountToSet == null && storageAccountCreatable == null) {
            withNewStorageAccount(SdkContext.randomResourceName(name(), 20), SkuName.STANDARD_GRS);
        }
        return super.createAsync();
    }

    private interface FunctionAppKeyService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps getMasterKey" })
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/sites/{name}/functions/admin/masterkey")
        Observable<Map<String, String>> getMasterKey(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("User-Agent") String userAgent);
    }

    private interface FunctionKuduService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps listFunctionKeys" })
        @GET("admin/functions/{name}/keys")
        Observable<FunctionKeyListResult> listFunctionKeys(@Path("name") String functionName);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps addFunctionKey" })
        @PUT("admin/functions/{name}/keys/{keyName}")
        Observable<NameValuePair> addFunctionKey(@Path("name") String functionName, @Path("keyName") String keyName, @Body NameValuePair key);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps generateFunctionKey" })
        @POST("admin/functions/{name}/keys/{keyName}")
        Observable<NameValuePair> generateFunctionKey(@Path("name") String functionName, @Path("keyName") String keyName);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps deleteFunctionKey" })
        @DELETE("admin/functions/{name}/keys/{keyName}")
        Observable<Void> deleteFunctionKey(@Path("name") String functionName, @Path("keyName") String keyName);
    }

    private static class FunctionKeyListResult {
        @JsonProperty("keys")
        private List<NameValuePair> keys;
    }

    private static final class KuduCredentials extends TokenCredentials {
        private String token;
        private long expire;
        private final FunctionAppImpl functionApp;

        private KuduCredentials(FunctionAppImpl functionApp) {
            super("Bearer", null);
            this.functionApp = functionApp;
        }

        @Override
        public String getToken(Request request) {
            if (token == null || expire < DateTime.now().getMillis()) {
                token = functionApp.manager().inner().webApps()
                        .getFunctionsAdminToken(functionApp.resourceGroupName(), functionApp.name());
                String jwt = new String(BaseEncoding.base64Url().decode(token.split("\\.")[1]));
                Pattern pattern = Pattern.compile("\"exp\": *([0-9]+),");
                Matcher matcher = pattern.matcher(jwt);
                matcher.find();
                expire = Long.parseLong(matcher.group(1));
            }
            return token;
        }
    }
}
