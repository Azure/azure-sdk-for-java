/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.CloudException;
import com.azure.management.RestClient;
import com.azure.management.resources.core.TestUtilities;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.storage.StorageAccount;
import com.azure.management.storage.StorageAccountSkuType;
import com.azure.management.storage.implementation.StorageManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

public class FunctionAppsTests extends AppServiceTest {
    private String RG_NAME_1 = "";
    private String RG_NAME_2 = "";
    private String WEBAPP_NAME_1 = "";
    private String WEBAPP_NAME_2 = "";
    private String WEBAPP_NAME_3 = "";
    private String APP_SERVICE_PLAN_NAME_1 = "";
    private String APP_SERVICE_PLAN_NAME_2 = "";
    private String STORAGE_ACCOUNT_NAME_1 = "";

    protected StorageManager storageManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        WEBAPP_NAME_1 = generateRandomResourceName("java-func-", 20);
        WEBAPP_NAME_2 = generateRandomResourceName("java-func-", 20);
        WEBAPP_NAME_3 = generateRandomResourceName("java-func-", 20);
        APP_SERVICE_PLAN_NAME_1 = generateRandomResourceName("java-asp-", 20);
        APP_SERVICE_PLAN_NAME_2 = generateRandomResourceName("java-asp-", 20);
        STORAGE_ACCOUNT_NAME_1 = generateRandomResourceName("javastore", 20);
        RG_NAME_1 = generateRandomResourceName("javacsmrg", 20);
        RG_NAME_2 = generateRandomResourceName("javacsmrg", 20);

        storageManager = StorageManager
                .authenticate(restClient, defaultSubscription, sdkContext);

        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        if (RG_NAME_1 != null) {
            resourceManager.resourceGroups().beginDeleteByName(RG_NAME_1);
        }
        if (RG_NAME_2 != null) {
            try {
                resourceManager.resourceGroups().beginDeleteByName(RG_NAME_2);
            } catch (CloudException e) {
                // fine, RG_NAME_2 is not created
            }
        }
    }

    @Test
    public void canCRUDFunctionApp() throws Exception {
        // Create with consumption
        FunctionApp functionApp1 = appServiceManager.functionApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME_1)
                .create();
        Assertions.assertNotNull(functionApp1);
        Assertions.assertEquals(Region.US_WEST, functionApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(functionApp1.appServicePlanId());
        Assertions.assertNotNull(plan1);
        Assertions.assertEquals(Region.US_WEST, plan1.region());
        Assertions.assertEquals(new PricingTier("Dynamic", "Y1"), plan1.pricingTier());

        FunctionAppResource functionAppResource1 = getStorageAccount(storageManager, functionApp1);
        // consumption plan requires this 2 settings
        Assertions.assertTrue(functionAppResource1.appSettings.containsKey(KEY_CONTENT_AZURE_FILE_CONNECTION_STRING));
        Assertions.assertTrue(functionAppResource1.appSettings.containsKey(KEY_CONTENT_SHARE));
        Assertions.assertEquals(functionAppResource1.appSettings.get(KEY_AZURE_WEB_JOBS_STORAGE).value(), functionAppResource1.appSettings.get(KEY_CONTENT_AZURE_FILE_CONNECTION_STRING).value());
        // verify accountKey
        Assertions.assertEquals(functionAppResource1.storageAccount.getKeys().get(0).getValue(), functionAppResource1.accountKey);

        // Create with the same consumption plan
        FunctionApp functionApp2 = appServiceManager.functionApps().define(WEBAPP_NAME_2)
                .withExistingAppServicePlan(plan1)
                .withNewResourceGroup(RG_NAME_2)
                .withExistingStorageAccount(functionApp1.storageAccount())
                .create();
        Assertions.assertNotNull(functionApp2);
        Assertions.assertEquals(Region.US_WEST, functionApp2.region());

        // Create with app service plan
        FunctionApp functionApp3 = appServiceManager.functionApps().define(WEBAPP_NAME_3)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(RG_NAME_2)
                .withNewAppServicePlan(PricingTier.BASIC_B1)
                .withExistingStorageAccount(functionApp1.storageAccount())
                .create();
        Assertions.assertNotNull(functionApp2);
        Assertions.assertEquals(Region.US_WEST, functionApp2.region());

        // app service plan does not have this 2 settings
        // https://github.com/Azure/azure-libraries-for-net/issues/485
        FunctionAppResource functionAppResource3 = getStorageAccount(storageManager, functionApp3);
        Assertions.assertFalse(functionAppResource3.appSettings.containsKey(KEY_CONTENT_AZURE_FILE_CONNECTION_STRING));
        Assertions.assertFalse(functionAppResource3.appSettings.containsKey(KEY_CONTENT_SHARE));
        // verify accountKey
        Assertions.assertEquals(functionAppResource3.storageAccount.getKeys().get(0).getValue(), functionAppResource3.accountKey);

        // Get
        FunctionApp functionApp = appServiceManager.functionApps().getByResourceGroup(RG_NAME_1, functionApp1.name());
        Assertions.assertEquals(functionApp1.id(), functionApp.id());
        functionApp = appServiceManager.functionApps().getById(functionApp2.id());
        Assertions.assertEquals(functionApp2.name(), functionApp.name());

        // List
        PagedIterable<FunctionApp> functionApps = appServiceManager.functionApps().listByResourceGroup(RG_NAME_1);
        Assertions.assertEquals(1, TestUtilities.getSize(functionApps));
        functionApps = appServiceManager.functionApps().listByResourceGroup(RG_NAME_2);
        Assertions.assertEquals(2, TestUtilities.getSize(functionApps));

        // Update
        functionApp2.update()
                .withNewStorageAccount(STORAGE_ACCOUNT_NAME_1, StorageAccountSkuType.STANDARD_GRS)
                .apply();
        Assertions.assertEquals(STORAGE_ACCOUNT_NAME_1, functionApp2.storageAccount().name());

        FunctionAppResource functionAppResource2 = getStorageAccount(storageManager, functionApp2);
        Assertions.assertTrue(functionAppResource2.appSettings.containsKey(KEY_CONTENT_AZURE_FILE_CONNECTION_STRING));
        Assertions.assertTrue(functionAppResource2.appSettings.containsKey(KEY_CONTENT_SHARE));
        Assertions.assertEquals(functionAppResource2.appSettings.get(KEY_AZURE_WEB_JOBS_STORAGE).value(), functionAppResource2.appSettings.get(KEY_CONTENT_AZURE_FILE_CONNECTION_STRING).value());
        Assertions.assertEquals(STORAGE_ACCOUNT_NAME_1, functionAppResource2.storageAccount.name());
        Assertions.assertEquals(functionAppResource2.storageAccount.getKeys().get(0).getValue(), functionAppResource2.accountKey);

        // Update, verify modify AppSetting does not create new storage account
        // https://github.com/Azure/azure-libraries-for-net/issues/457
        int numStorageAccountBefore = TestUtilities.getSize(storageManager.storageAccounts().listByResourceGroup(RG_NAME_1));
        functionApp1.update()
                .withAppSetting("newKey", "newValue")
                .apply();
        int numStorageAccountAfter = TestUtilities.getSize(storageManager.storageAccounts().listByResourceGroup(RG_NAME_1));
        Assertions.assertEquals(numStorageAccountBefore, numStorageAccountAfter);
        FunctionAppResource functionAppResource1Updated = getStorageAccount(storageManager, functionApp1);
        Assertions.assertTrue(functionAppResource1Updated.appSettings.containsKey("newKey"));
        Assertions.assertEquals(functionAppResource1.appSettings.get(KEY_AZURE_WEB_JOBS_STORAGE).value(), functionAppResource1Updated.appSettings.get(KEY_AZURE_WEB_JOBS_STORAGE).value());
        Assertions.assertEquals(functionAppResource1.appSettings.get(KEY_CONTENT_AZURE_FILE_CONNECTION_STRING).value(), functionAppResource1Updated.appSettings.get(KEY_CONTENT_AZURE_FILE_CONNECTION_STRING).value());
        Assertions.assertEquals(functionAppResource1.appSettings.get(KEY_CONTENT_SHARE).value(), functionAppResource1Updated.appSettings.get(KEY_CONTENT_SHARE).value());
        Assertions.assertEquals(functionAppResource1.storageAccount.name(), functionAppResource1Updated.storageAccount.name());

        // Scale
        functionApp3.update()
                .withNewAppServicePlan(PricingTier.STANDARD_S2)
                .apply();
        Assertions.assertNotEquals(functionApp3.appServicePlanId(), functionApp1.appServicePlanId());
    }

    private static final String FUNCTION_APP_PACKAGE_URL = "https://raw.github.com/Azure/azure-libraries-for-java/master/azure-mgmt-appservice/src/test/resources/java-functions.zip";

    @Test
    public void canCRUDLinuxFunctionApp() throws Exception {
        RG_NAME_2 = null;

        // function app with consumption plan
        FunctionApp functionApp1 = appServiceManager.functionApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(RG_NAME_1)
                .withNewLinuxConsumptionPlan()
                .withBuiltInImage(FunctionRuntimeStack.JAVA_8)
                .withHttpsOnly(true)
                .withAppSetting("WEBSITE_RUN_FROM_PACKAGE", FUNCTION_APP_PACKAGE_URL)
                .create();
        Assertions.assertNotNull(functionApp1);
        assertLinuxJava8(functionApp1, FunctionRuntimeStack.JAVA_8.getLinuxFxVersionForConsumptionPlan());

        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(functionApp1.appServicePlanId());
        Assertions.assertNotNull(plan1);
        Assertions.assertEquals(Region.US_EAST, plan1.region());
        Assertions.assertEquals(new PricingTier(SkuName.DYNAMIC.toString(), "Y1"), plan1.pricingTier());
        Assertions.assertTrue(plan1.inner().reserved());
        Assertions.assertTrue(Arrays.asList(functionApp1.inner().kind().split(",")).containsAll(Arrays.asList("linux", "functionapp")));

        PagedIterable<FunctionApp> functionApps = appServiceManager.functionApps().listByResourceGroup(RG_NAME_1);
        Assertions.assertEquals(1, TestUtilities.getSize(functionApps));

        // function app with app service plan
        FunctionApp functionApp2 = appServiceManager.functionApps().define(WEBAPP_NAME_2)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(RG_NAME_1)
                .withNewLinuxAppServicePlan(PricingTier.STANDARD_S1)
                .withBuiltInImage(FunctionRuntimeStack.JAVA_8)
                .withHttpsOnly(true)
                .withAppSetting("WEBSITE_RUN_FROM_PACKAGE", FUNCTION_APP_PACKAGE_URL)
                .create();
        Assertions.assertNotNull(functionApp2);
        assertLinuxJava8(functionApp2, FunctionRuntimeStack.JAVA_8.getLinuxFxVersionForDedicatedPlan());

        AppServicePlan plan2 = appServiceManager.appServicePlans().getById(functionApp2.appServicePlanId());
        Assertions.assertNotNull(plan2);
        Assertions.assertEquals(PricingTier.STANDARD_S1, plan2.pricingTier());
        Assertions.assertTrue(plan2.inner().reserved());

        // one more function app using existing app service plan
        FunctionApp functionApp3 = appServiceManager.functionApps().define(WEBAPP_NAME_3)
                .withExistingLinuxAppServicePlan(plan2)
                .withExistingResourceGroup(RG_NAME_1)
                .withBuiltInImage(FunctionRuntimeStack.JAVA_8)
                .withHttpsOnly(true)
                .withAppSetting("WEBSITE_RUN_FROM_PACKAGE", FUNCTION_APP_PACKAGE_URL)
                .create();
        Assertions.assertNotNull(functionApp3);
        assertLinuxJava8(functionApp3, FunctionRuntimeStack.JAVA_8.getLinuxFxVersionForDedicatedPlan());

        // wait for deploy
        if (!isPlaybackMode()) {
            SdkContext.sleep(180000);
        }

        functionApps = appServiceManager.functionApps().listByResourceGroup(RG_NAME_1);
        Assertions.assertEquals(3, TestUtilities.getSize(functionApps));

        // verify deploy
        PagedIterable<FunctionEnvelope> functions = appServiceManager.functionApps().listFunctions(functionApp1.resourceGroupName(), functionApp1.name());
        Assertions.assertEquals(1, TestUtilities.getSize(functions));

        functions = appServiceManager.functionApps().listFunctions(functionApp2.resourceGroupName(), functionApp2.name());
        Assertions.assertEquals(1, TestUtilities.getSize(functions));

        functions = appServiceManager.functionApps().listFunctions(functionApp3.resourceGroupName(), functionApp3.name());
        Assertions.assertEquals(1, TestUtilities.getSize(functions));
    }

    @Test
    public void canCRUDLinuxFunctionAppPremium() {
        RG_NAME_2 = null;

        // function app with premium plan
        FunctionApp functionApp1 = appServiceManager.functionApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(RG_NAME_1)
                .withNewLinuxAppServicePlan(new PricingTier(SkuName.ELASTIC_PREMIUM.toString(), "EP1"))
                .withBuiltInImage(FunctionRuntimeStack.JAVA_8)
                .withHttpsOnly(true)
                .withAppSetting("WEBSITE_RUN_FROM_PACKAGE", FUNCTION_APP_PACKAGE_URL)
                .create();
        Assertions.assertNotNull(functionApp1);
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(functionApp1.appServicePlanId());
        Assertions.assertNotNull(plan1);
        Assertions.assertEquals(new PricingTier(SkuName.ELASTIC_PREMIUM.toString(), "EP1"), plan1.pricingTier());
        Assertions.assertTrue(plan1.inner().reserved());
        assertLinuxJava8(functionApp1, FunctionRuntimeStack.JAVA_8.getLinuxFxVersionForDedicatedPlan());

        // wait for deploy
        if (!isPlaybackMode()) {
            SdkContext.sleep(180000);
        }

        // verify deploy
        PagedIterable<FunctionEnvelope> functions = appServiceManager.functionApps().listFunctions(functionApp1.resourceGroupName(), functionApp1.name());
        Assertions.assertEquals(1, TestUtilities.getSize(functions));
    }

    @Test
    @Disabled("Need container registry")
    public void canCRUDLinuxFunctionAppPremiumDocker() {
        // function app with premium plan with private docker
        FunctionApp functionApp1 = appServiceManager.functionApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(RG_NAME_1)
                .withNewLinuxAppServicePlan(new PricingTier(SkuName.ELASTIC_PREMIUM.toString(), "EP1"))
                .withPrivateRegistryImage("weidxuregistry.azurecr.io/az-func-java:v1", "https://weidxuregistry.azurecr.io")
                .withCredentials("weidxuregistry", "PASSWORD")
                .withRuntime("java")
                .withRuntimeVersion("~3")
                .create();

        // deploy
        if (!isPlaybackMode()) {
            functionApp1.zipDeploy(new File(FunctionAppsTests.class.getResource("/java-functions.zip").getPath()));
        }
    }

    private static Map<String, AppSetting> assertLinuxJava8(FunctionApp functionApp, String linuxFxVersion) {
        Assertions.assertEquals(linuxFxVersion, functionApp.linuxFxVersion());
        Assertions.assertTrue(Arrays.asList(functionApp.inner().kind().split(",")).containsAll(Arrays.asList("linux", "functionapp")));
        Assertions.assertTrue(functionApp.inner().reserved());

        Map<String, AppSetting> appSettings = functionApp.getAppSettings();
        Assertions.assertNotNull(appSettings);
        Assertions.assertNotNull(appSettings.get(KEY_AZURE_WEB_JOBS_STORAGE));
        Assertions.assertEquals(FunctionRuntimeStack.JAVA_8.runtime(), appSettings.get(KEY_FUNCTIONS_WORKER_RUNTIME).value());
        Assertions.assertEquals(FunctionRuntimeStack.JAVA_8.version(), appSettings.get(KEY_FUNCTIONS_EXTENSION_VERSION).value());

        return appSettings;
    }

    private static String KEY_AZURE_WEB_JOBS_STORAGE = "AzureWebJobsStorage";
    private static String KEY_CONTENT_AZURE_FILE_CONNECTION_STRING = "WEBSITE_CONTENTAZUREFILECONNECTIONSTRING";
    private static String KEY_CONTENT_SHARE = "WEBSITE_CONTENTSHARE";
    private static String KEY_FUNCTIONS_WORKER_RUNTIME = "FUNCTIONS_WORKER_RUNTIME";
    private static String KEY_FUNCTIONS_EXTENSION_VERSION = "FUNCTIONS_EXTENSION_VERSION";

    private static String ACCOUNT_NAME_SEGMENT = "AccountName=";
    private static String ACCOUNT_KEY_SEGMENT = "AccountKey=";

    private static class FunctionAppResource {
        Map<String, AppSetting> appSettings;

        String accountName;
        String accountKey;

        StorageAccount storageAccount;
    }

    private static FunctionAppResource getStorageAccount(StorageManager storageManager,
                                                         FunctionApp functionApp) {
        FunctionAppResource resource = new FunctionAppResource();
        resource.appSettings = functionApp.getAppSettings();

        String storageAccountConnectionString = resource.appSettings.get(KEY_AZURE_WEB_JOBS_STORAGE).value();
        String[] segments = storageAccountConnectionString.split(";");
        for (String segment : segments) {
            if (segment.startsWith(ACCOUNT_NAME_SEGMENT)) {
                resource.accountName = segment.substring(ACCOUNT_NAME_SEGMENT.length());
            } else if (segment.startsWith(ACCOUNT_KEY_SEGMENT)) {
                resource.accountKey = segment.substring(ACCOUNT_KEY_SEGMENT.length());
            }
        }
        if (resource.accountName != null) {
            PagedIterable<StorageAccount> storageAccounts = storageManager.storageAccounts().list();
            for (StorageAccount storageAccount : storageAccounts) {
                if (resource.accountName.equals(storageAccount.name())) {
                    resource.storageAccount = storageAccount;
                    break;
                }
            }
        }

        return resource;
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int c;
        for (c = in.read(); c != '\n' && c >= 0 ; c = in.read()) {
            stream.write(c);
        }
        if (c == -1 && stream.size() == 0) {
            return null;
        }
        return stream.toString("UTF-8");
    }
}