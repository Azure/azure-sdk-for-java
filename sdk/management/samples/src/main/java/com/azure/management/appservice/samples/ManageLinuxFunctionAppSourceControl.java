/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.samples;

import com.azure.management.Azure;
import com.azure.management.appservice.FunctionApp;
import com.azure.management.appservice.FunctionRuntimeStack;
import com.azure.management.appservice.PricingTier;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.samples.Utils;
import com.azure.management.storage.StorageAccountSkuType;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import org.apache.commons.lang.time.StopWatch;

import java.io.File;


/**
 * Azure App Service basic sample for managing function apps.
 *  - Create 2 linux function apps.
 *    - Deploy 1 under new dedicated app service plan, run from a package.
 *    - Deploy 1 under new consumption plan, run from a package.
 */
public class ManageLinuxFunctionAppSourceControl {

    private static final String FUNCTION_APP_PACKAGE_URL = "https://raw.github.com/Azure/azure-libraries-for-java/master/azure-mgmt-appservice/src/test/resources/java-functions.zip";
    private static final long TIMEOUT_IN_SECONDS = 5 * 60;

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String suffix         = ".azurewebsites.net";
        final String app1Name       = azure.sdkContext().randomResourceName("webapp1-", 20);
        final String app2Name       = azure.sdkContext().randomResourceName("webapp2-", 20);
        final String app1Url        = app1Name + suffix;
        final String app2Url        = app2Name + suffix;
        final String plan1Name      = azure.sdkContext().randomResourceName("plan1-", 20);
        final String plan2Name      = azure.sdkContext().randomResourceName("plan2-", 20);
        final String storage1Name   = azure.sdkContext().randomResourceName("storage1", 20);
        final String rgName         = azure.sdkContext().randomResourceName("rg1NEMV_", 24);

        try {

            //============================================================
            // Create a function app with a new dedicated app service plan, configure as run from a package

            System.out.println("Creating function app " + app1Name + " in resource group " + rgName + "...");

            FunctionApp app1 = azure.appServices().functionApps().define(app1Name)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withNewLinuxAppServicePlan(plan1Name, PricingTier.STANDARD_S1)
                    .withBuiltInImage(FunctionRuntimeStack.JAVA_8)
                    .withNewStorageAccount(storage1Name, StorageAccountSkuType.STANDARD_RAGRS)
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
                String response = Utils.get("https://" + app1UrlFunction);
                if (response != null && response.contains("Hello")) {
                    break;
                }
                SdkContext.sleep(10 * 1000);
            }

            // call function
            System.out.println("CURLing " + app1UrlFunction + "...");
            System.out.println("Response is " + Utils.get("https://" + app1UrlFunction));
            // response would be "Hello, ..."


            //============================================================
            // Create a function app with a new consumption plan, configure as run from a package

            System.out.println("Creating function app " + app2Name + " in resource group " + rgName + "...");

            FunctionApp app2 = azure.appServices().functionApps().define(app2Name)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withNewLinuxConsumptionPlan(plan2Name)
                    .withBuiltInImage(FunctionRuntimeStack.JAVA_8)
                    .withExistingStorageAccount(azure.storageAccounts().getByResourceGroup(rgName, storage1Name))
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
                String response = Utils.get("https://" + app2UrlFunction);
                if (response != null && response.contains("Hello")) {
                    break;
                }
                SdkContext.sleep(10 * 1000);
            }

            // call function
            System.out.println("CURLing " + app2UrlFunction + "...");
            System.out.println("Response is " + Utils.get("https://" + app2UrlFunction));
            // response would be "Hello, ..."

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());
            runSample(azure);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}