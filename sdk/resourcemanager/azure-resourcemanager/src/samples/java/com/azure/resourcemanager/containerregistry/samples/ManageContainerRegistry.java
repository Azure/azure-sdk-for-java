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
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.samples.SampleUtils;

/**
 * Azure Container Registry sample for managing container registries.
 *  - Create an Azure Container Registry to hold private Docker images (admin user disabled)
 *  - Grant pull access to a managed identity with the {@code AcrPull} role instead of using admin credentials
 * <p>
 * Consumers (for example a web app, AKS, or a CI agent) authenticate with their own Microsoft Entra identity, so no
 * registry password needs to be stored or shared. Images are then pushed and pulled with the Docker CLI, for example:
 * {@code az acr login --name <name>}, {@code docker push <loginServer>/<repo>:<tag>}.
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
        final String identityName = SampleUtils.randomResourceName(azureResourceManager, "acrpull", 20);
        final Region region = Region.US_EAST;

        try {
            // Create an Azure Container Registry without the admin user (admin user is discouraged).
            Registry azureRegistry = azureResourceManager.containerRegistries()
                .define(acrName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withBasicSku()
                .create();

            // Create a managed identity that consumers use to pull images.
            Identity pullIdentity = azureResourceManager.identities()
                .define(identityName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .create();

            // Grant that identity the AcrPull role, scoped to the registry (passwordless, least-privilege pull access).
            azureResourceManager.accessManagement()
                .roleAssignments()
                .define(SampleUtils.randomUuid(azureResourceManager))
                .forObjectId(pullIdentity.principalId())
                .withBuiltInRole(BuiltInRole.ACR_PULL)
                .withResourceScope(azureRegistry)
                .create();

            System.out.println("Created container registry: " + azureRegistry.loginServerUrl());
            System.out.println("Granted AcrPull to managed identity: " + pullIdentity.id());
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
