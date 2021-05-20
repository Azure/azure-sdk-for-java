// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.keyvault.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.security.keyvault.keys.models.KeyType;

/**
 * Azure Storage sample for managing storage accounts with customer-managed key.
 * - Create a storage account with system assigned managed service identity
 * - Create a key vault with purge protection enabled and access policy for managed service identity of storage account
 * - Create a RSA key
 * - Update storage account to enable encryption with customer-managed key
 *
 * {@see http://aka.ms/storagecmkconfiguration}
 */
public final class ManageStorageAccountCustomerManagedKey {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @param clientId the client ID
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager, String clientId) {
        final Region region = Region.US_EAST;
        final String storageAccountName = Utils.randomResourceName(azureResourceManager, "sa", 8);
        final String vaultName = Utils.randomResourceName(azureResourceManager, "kv", 8);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rg", 8);

        try {
            //============================================================
            // Create a storage account with system assigned managed service identity

            StorageAccount storageAccount = azureResourceManager.storageAccounts().define(storageAccountName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withInfrastructureEncryption()
                .withTableAccountScopedEncryptionKey()
                .withQueueAccountScopedEncryptionKey()
                .withSystemAssignedManagedServiceIdentity()
                .create();

            //============================================================
            // Create a key vault with purge protection enabled and access policy for managed service identity of storage account

            Vault vault = azureResourceManager.vaults().define(vaultName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .defineAccessPolicy()   // access policy for this sample client to generate key
                    .forServicePrincipal(clientId)
                    .allowKeyAllPermissions()
                    .attach()
                .defineAccessPolicy()   // access policy for storage account managed service identity
                    .forObjectId(storageAccount.systemAssignedManagedServiceIdentityPrincipalId())
                    .allowKeyPermissions(KeyPermissions.GET, KeyPermissions.WRAP_KEY, KeyPermissions.UNWRAP_KEY)
                    .attach()
                .withPurgeProtectionEnabled()
                .create();

            //============================================================
            // Create a key for storage account, RSA 2048, 3072 or 4096

            vault.keys().define("sakey")
                .withKeyTypeToCreate(KeyType.RSA)
                .withKeySize(2048)
                .create();

            //============================================================
            // Enable customer-managed key in storage account

            storageAccount.update()
                .withEncryptionKeyFromKeyVault(vault.vaultUri(), "sakey", null)
                .apply();

        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (Exception e) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            }
        }

        return true;
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
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

    private ManageStorageAccountCustomerManagedKey() {
    }
}
