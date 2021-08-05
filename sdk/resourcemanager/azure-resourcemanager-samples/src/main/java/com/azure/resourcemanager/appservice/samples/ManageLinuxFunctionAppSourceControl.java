// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionRuntimeStack;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import com.azure.core.http.policy.HttpLogDetailLevel;
import org.apache.commons.lang.time.StopWatch;

import java.time.Duration;

/**
 * Azure App Service basic sample for managing function apps.
 *  - Create 2 linux function apps.
 *    - Deploy 1 under new dedicated app service plan, run from a package.
 *    - Deploy 1 under new consumption plan, run from a package.
 */
public class ManageLinuxFunctionAppSourceControl {

    private static final String FUNCTION_APP_PACKAGE_URL = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/resourcemanager/azure-resourcemanager-appservice/src/test/resources/java-functions.zip";
    private static final long TIMEOUT_IN_SECONDS = 5 * 60;

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String suffix         = ".azurewebsites.net";
        final String app1Name       = Utils.randomResourceName(azureResourceManager, "webapp1-", 20);
        final String app2Name       = Utils.randomResourceName(azureResourceManager, "webapp2-", 20);
        final String app1Url        = app1Name + suffix;
        final String app2Url        = app2Name + suffix;
        final String plan1Name      = Utils.randomResourceName(azureResourceManager, "plan1-", 20);
        final String plan2Name      = Utils.randomResourceName(azureResourceManager, "plan2-", 20);
        final String storage1Name   = Utils.randomResourceName(azureResourceManager, "storage1", 20);
        final String rgName         = Utils.randomResourceName(azureResourceManager, "rg1NEMV_", 24);

        try {

            //============================================================
            // Create a function app with a new dedicated app service plan, configure as run from a package

            System.out.println("Creating function app " + app1Name + " in resource group " + rgName + "...");

            FunctionApp app1 = azureResourceManager.functionApps().define(app1Name)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withNewLinuxAppServicePlan(plan1Name, PricingTier.STANDARD_S1)
                    .withBuiltInImage(FunctionRuntimeStack.JAVA_8)
                    .withNewStorageAccount(storage1Name, StorageAccountSkuType.STANDARD_LRS)
                    .withHttpsOnly(true)
                    .withAppSetting("WEBSITE_RUN_FROM_PACKAGE", FUNCTION_APP_PACKAGE_URL)
                    .create();

            System.out.println("Created function app " + app1.name());
            Utils.print(app1);

            // warm up
            String app1UrlFunction = app1Url + "/api/HttpTrigger-Java?name=linux_function_app1";
            System.out.println("Warming up " + app1UrlFunction + "...");
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            while (stopWatch.getTime() < TIMEOUT_IN_SECONDS * 1000) {
                String response = Utils.sendGetRequest("https://" + app1UrlFunction);
                if (response != null && response.contains("Hello")) {
                    break;
                }
                ResourceManagerUtils.sleep(Duration.ofSeconds(10));
            }

            // call function
            System.out.println("CURLing " + app1UrlFunction + "...");
            System.out.println("Response is " + Utils.sendGetRequest("https://" + app1UrlFunction));
            // response would be "Hello, ..."


            //============================================================
            // Create a function app with a new consumption plan, configure as run from a package

            System.out.println("Creating function app " + app2Name + " in resource group " + rgName + "...");

            FunctionApp app2 = azureResourceManager.functionApps().define(app2Name)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withNewLinuxConsumptionPlan(plan2Name)
                    .withBuiltInImage(FunctionRuntimeStack.JAVA_8)
                    .withExistingStorageAccount(azureResourceManager.storageAccounts().getByResourceGroup(rgName, storage1Name))
                    .withHttpsOnly(true)
                    .withAppSetting("WEBSITE_RUN_FROM_PACKAGE", FUNCTION_APP_PACKAGE_URL)
                    .create();

            System.out.println("Created function app " + app2.name());
            Utils.print(app2);

            // warm up
            String app2UrlFunction = app2Url + "/api/HttpTrigger-Java?name=linux_function_app2";
            System.out.println("Warming up " + app2UrlFunction + "...");
            stopWatch = new StopWatch();
            stopWatch.start();
            while (stopWatch.getTime() < TIMEOUT_IN_SECONDS * 1000) {
                String response = Utils.sendGetRequest("https://" + app2UrlFunction);
                if (response != null && response.contains("Hello")) {
                    break;
                }
                ResourceManagerUtils.sleep(Duration.ofSeconds(10));
            }

            // call function
            System.out.println("CURLing " + app2UrlFunction + "...");
            System.out.println("Response is " + Utils.sendGetRequest("https://" + app2UrlFunction));
            // response would be "Hello, ..."

            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());
            runSample(azureResourceManager);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
