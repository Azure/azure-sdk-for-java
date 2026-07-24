// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.appservice.models.ConnectionStringType;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.samples.SampleUtils;
import com.azure.resourcemanager.storage.models.StorageAccount;

/**
 * Azure App Service sample for connecting a web app to a storage account.
 *  - Create a storage account
 *  - Create a web app
 *  - Add the storage account connection string to the web app settings
 */
public final class ConnectWebAppToStorageAccount {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rg1NEMV_", 24);
        final String appName = SampleUtils.randomResourceName(azureResourceManager, "webapp-", 20);
        final String storageName = SampleUtils.randomResourceName(azureResourceManager, "jsdkstore", 20);
        final String containerName = SampleUtils.randomResourceName(azureResourceManager, "jcontainer", 20);

        try {
            // Create a storage account for the web app to use.
            StorageAccount storageAccount = azureResourceManager.storageAccounts()
                .define(storageName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .create();

            String accountKey = storageAccount.getKeys().get(0).value();
            String connectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s",
                storageAccount.name(), accountKey);

            // Create a web app and store the storage connection string in its settings.
            WebApp app = azureResourceManager.webApps()
                .define(appName)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(rgName)
                .withNewWindowsPlan(PricingTier.STANDARD_S1)
                .withJavaVersion(JavaVersion.JAVA_11)
                .withWebContainer(WebContainer.TOMCAT_9_0_NEWEST)
                .withConnectionString("storage.connectionString", connectionString, ConnectionStringType.CUSTOM)
                .withAppSetting("storage.containerName", containerName)
                .create();

            System.out.println("Connected web app " + app.name() + " to storage account " + storageAccount.name());
            return true;
        } finally {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ConnectWebAppToStorageAccount() {
    }
}
