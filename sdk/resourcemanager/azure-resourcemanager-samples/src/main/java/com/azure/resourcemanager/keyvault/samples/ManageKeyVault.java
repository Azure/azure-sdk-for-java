// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.keyvault.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.models.SecretPermissions;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

/**
 * Azure Key Vault sample for managing key vaults -
 *  - Create a key vault
 *  - Authorize an application
 *  - Update a key vault
 *    - alter configurations
 *    - change permissions
 *  - Create another key vault
 *  - List key vaults
 *  - Delete a key vault.
 */
public final class ManageKeyVault {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @param clientId client id
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager, String clientId) {
        final String vaultName1 = Utils.randomResourceName(azureResourceManager, "vault1", 20);
        final String vaultName2 = Utils.randomResourceName(azureResourceManager, "vault2", 20);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgNEMV", 24);

        try {
            //============================================================
            // Create a key vault with empty access policy

            System.out.println("Creating a key vault...");

            Vault vault1 = azureResourceManager.vaults().define(vaultName1)
                    .withRegion(Region.US_WEST)
                    .withNewResourceGroup(rgName)
                    .withEmptyAccessPolicy()
                    .create();

            System.out.println("Created key vault");
            Utils.print(vault1);

            //============================================================
            // Authorize an application

            System.out.println("Authorizing the application associated with the current service principal...");

            vault1 = vault1.update()
                    .defineAccessPolicy()
                        .forServicePrincipal(clientId)
                        .allowKeyAllPermissions()
                        .allowSecretPermissions(SecretPermissions.GET)
                        .allowSecretPermissions(SecretPermissions.LIST)
                        .attach()
                    .apply();

            System.out.println("Updated key vault");
            Utils.print(vault1);

            //============================================================
            // Update a key vault

            System.out.println("Update a key vault to enable deployments and add permissions to the application...");

            vault1 = vault1.update()
                    .withDeploymentEnabled()
                    .withTemplateDeploymentEnabled()
                    .updateAccessPolicy(vault1.accessPolicies().get(0).objectId())
                    .allowCertificatePermissions()
                        .allowSecretAllPermissions()
                        .parent()
                    .apply();

            System.out.println("Updated key vault");
            // Print the network security group
            Utils.print(vault1);


            //============================================================
            // Create another key vault

            Vault vault2 = azureResourceManager.vaults().define(vaultName2)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .defineAccessPolicy()
                        .forServicePrincipal(clientId)
                        .allowKeyPermissions(KeyPermissions.LIST)
                        .allowKeyPermissions(KeyPermissions.GET)
                        .allowKeyPermissions(KeyPermissions.DECRYPT)
                        .allowSecretPermissions(SecretPermissions.GET)
                        .attach()
                    .create();

            System.out.println("Created key vault");
            // Print the network security group
            Utils.print(vault2);


            //============================================================
            // List key vaults

            System.out.println("Listing key vaults...");

            for (Vault vault : azureResourceManager.vaults().listByResourceGroup(rgName)) {
                Utils.print(vault);
            }

            //============================================================
            // Delete key vaults
            System.out.println("Deleting the key vaults");
            azureResourceManager.vaults().deleteById(vault1.id());
            azureResourceManager.vaults().deleteById(vault2.id());
            System.out.println("Deleted the key vaults");

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
            final Configuration configuration = Configuration.getGlobalConfiguration();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager, configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageKeyVault() {
    }
}
