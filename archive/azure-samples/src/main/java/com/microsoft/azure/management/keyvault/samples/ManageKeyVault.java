/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault.samples;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.keyvault.KeyPermissions;
import com.microsoft.azure.management.keyvault.SecretPermissions;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;

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
     * @param azure instance of the azure client
     * @param clientId client id
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure, String clientId) {
        final String vaultName1 = SdkContext.randomResourceName("vault1", 20);
        final String vaultName2 = SdkContext.randomResourceName("vault2", 20);
        final String rgName = SdkContext.randomResourceName("rgNEMV", 24);

        try {
            //============================================================
            // Create a key vault with empty access policy

            System.out.println("Creating a key vault...");

            Vault vault1 = azure.vaults().define(vaultName1)
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
                        .allowSecretAllPermissions()
                        .parent()
                    .apply();

            System.out.println("Updated key vault");
            // Print the network security group
            Utils.print(vault1);


            //============================================================
            // Create another key vault

            Vault vault2 = azure.vaults().define(vaultName2)
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

            for (Vault vault : azure.vaults().listByResourceGroup(rgName)) {
                Utils.print(vault);
            }

            //============================================================
            // Delete key vaults
            System.out.println("Deleting the key vaults");
            azure.vaults().deleteById(vault1.id());
            azure.vaults().deleteById(vault2.id());
            System.out.println("Deleted the key vaults");

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByName(rgName);
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

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BASIC)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure, ApplicationTokenCredentials.fromFile(credFile).clientId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageKeyVault() {
    }
}
