// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.containerregistry.models.AccessKeyType;
import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.containerregistry.models.RegistryCredentials;
import com.azure.resourcemanager.samples.SampleUtils;

/**
 * Azure Container Registry sample for managing container registries.
 *  - Create an Azure Container Registry to hold private Docker images
 *  - Retrieve the admin credentials used to push and pull images
 * <p>
 * Once the registry exists, images are pushed and pulled with the Docker CLI, for example:
 * {@code docker login <loginServer>}, {@code docker push <loginServer>/<repo>:<tag>}.
 */
public final class ManageContainerRegistry {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rgACR", 15);
        final String acrName = SampleUtils.randomResourceName(azureResourceManager, "acrsample", 20);
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

            // Retrieve the credentials used to authenticate against the registry.
            RegistryCredentials credentials = azureRegistry.getCredentials();

            System.out.println("Created container registry: " + azureRegistry.loginServerUrl());
            System.out.println("Admin user: " + credentials.username());
            System.out.println("Primary access key available: "
                + (credentials.accessKeys().get(AccessKeyType.PRIMARY) != null));
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

    private ManageContainerRegistry() {
    }
}
