// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.LogLevel;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;
import org.apache.commons.lang.time.StopWatch;
import reactor.core.publisher.BaseSubscriber;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Azure App Service basic sample for managing function apps.
 *  - Create a function app under the same new app service plan:
 *    - Deploy to app using FTP
 *    - stream logs synchronously for 30 seconds
 *    - stream logs asynchronously until 3 requests are completed
 */
public final class ManageFunctionAppLogs {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) throws IOException {
        // New resources
        final String suffix         = ".azurewebsites.net";
        final String appName       = Utils.randomResourceName(azureResourceManager, "webapp1-", 20);
        final String appUrl        = appName + suffix;
        final String rgName         = Utils.randomResourceName(azureResourceManager, "rg1NEMV_", 24);

        try {


            //============================================================
            // Create a function app with a new app service plan

            System.out.println("Creating function app " + appName + " in resource group " + rgName + "...");

            FunctionApp app = azureResourceManager.functionApps().define(appName)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .defineDiagnosticLogsConfiguration()
                        .withApplicationLogging()
                        .withLogLevel(LogLevel.VERBOSE)
                        .withApplicationLogsStoredOnFileSystem()
                        .attach()
                    .create();

            System.out.println("Created function app " + app.name());
            Utils.print(app);

            //============================================================
            // Deploy to app 1 through FTP

            System.out.println("Deploying a function app to " + appName + " through FTP...");

            Utils.uploadFileForFunctionViaFtp(app.getPublishingProfile(), "host.json", ManageFunctionAppLogs.class.getResourceAsStream("/square-function-app/host.json"));
            Utils.uploadFileForFunctionViaFtp(app.getPublishingProfile(), "square/function.json", ManageFunctionAppLogs.class.getResourceAsStream("/square-function-app/square/function.json"));
            Utils.uploadFileForFunctionViaFtp(app.getPublishingProfile(), "square/index.js", ManageFunctionAppLogs.class.getResourceAsStream("/square-function-app/square/index.js"));

            // sync triggers
            app.syncTriggers();

            System.out.println("Deployment square app to function app " + app.name() + " completed");
            Utils.print(app);

            // warm up
            System.out.println("Warming up " + appUrl + "/api/square...");
            Utils.sendPostRequest("http://" + appUrl + "/api/square", "625");
            ResourceManagerUtils.sleep(Duration.ofSeconds(5));

            //============================================================
            // Listen to logs synchronously for 30 seconds

            final InputStream stream = app.streamApplicationLogs();
            System.out.println("Streaming logs from function app " + appName + "...");
            String line = readLine(stream);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            new Thread(() ->  {
                Utils.sendPostRequest("http://" + appUrl + "/api/square", "625");
                ResourceManagerUtils.sleep(Duration.ofSeconds(10));
                Utils.sendPostRequest("http://" + appUrl + "/api/square", "725");
                ResourceManagerUtils.sleep(Duration.ofSeconds(10));
                Utils.sendPostRequest("http://" + appUrl + "/api/square", "825");
            }).start();
            while (line != null && stopWatch.getTime() < 90000) {
                System.out.println(line);
                line = readLine(stream);
            }
            stream.close();

            //============================================================
            // Listen to logs asynchronously until 3 requests are completed

            new Thread(() ->  {
                ResourceManagerUtils.sleep(Duration.ofSeconds(5));
                System.out.println("Starting hitting");
                Utils.sendPostRequest("http://" + appUrl + "/api/square", "625");
                ResourceManagerUtils.sleep(Duration.ofSeconds(10));
                Utils.sendPostRequest("http://" + appUrl + "/api/square", "725");
                ResourceManagerUtils.sleep(Duration.ofSeconds(10));
                Utils.sendPostRequest("http://" + appUrl + "/api/square", "825");
            }).start();

            final AtomicInteger count = new AtomicInteger(0);
            app.streamApplicationLogsAsync().subscribe(new BaseSubscriber<String>() {
                @Override
                protected void hookOnNext(String value) {
                    System.out.println(value);
                    if (value.contains("Function completed")) {
                        if (count.incrementAndGet() >= 3) {
                            this.dispose();
                        }
                    }
                    super.hookOnNext(value);
                }

                @Override
                protected void hookOnError(Throwable throwable) {
                    throwable.printStackTrace();
                    super.hookOnError(throwable);
                }
            });

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

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int c;
        for (c = in.read(); c != '\n' && c >= 0; c = in.read()) {
            stream.write(c);
        }
        if (c == -1 && stream.size() == 0) {
            return null;
        }
        return stream.toString("UTF-8");
    }
}
