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
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.containerregistry.models.AccessKeyType;
import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.containerregistry.models.RegistryCredentials;
import com.azure.resourcemanager.samples.SampleUtils;

/**
 * Azure App Service sample for deploying an image from Azure Container Registry to a Linux web app.
 *  - Create an Azure Container Registry
 *  - Create a Linux web app that runs an image from the registry
 * <p>
 * The image ({@code samples/tomcat:latest}) is expected to be pushed to the registry beforehand with the Docker CLI.
 */
public final class DeployImageFromAcrToLinuxWebApp {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rgACR", 15);
        final String acrName = SampleUtils.randomResourceName(azureResourceManager, "acrsample", 20);
        final String appName = SampleUtils.randomResourceName(azureResourceManager, "webapp", 20);
        final Region region = Region.US_EAST;

        try {
            // Create an Azure Container Registry with the admin user enabled.
            Registry azureRegistry = azureResourceManager.containerRegistries()
                .define(acrName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withBasicSku()
                .withRegistryNameAsAdminUser()
                .create();

            RegistryCredentials acrCredentials = azureRegistry.getCredentials();
            String privateImage = azureRegistry.loginServerUrl() + "/samples/tomcat:latest";

            // Create a Linux web app that pulls its image from the private registry.
            WebApp app = azureResourceManager.webApps()
                .define(appName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withNewLinuxPlan(PricingTier.STANDARD_S1)
                .withPrivateRegistryImage(privateImage, "https://" + azureRegistry.loginServerUrl())
                .withCredentials(acrCredentials.username(), acrCredentials.accessKeys().get(AccessKeyType.PRIMARY))
                .withAppSetting("PORT", "8080")
                .create();

            System.out.println("Deployed image " + privateImage + " to web app " + app.defaultHostname());
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

    private DeployImageFromAcrToLinuxWebApp() {
    }
}
