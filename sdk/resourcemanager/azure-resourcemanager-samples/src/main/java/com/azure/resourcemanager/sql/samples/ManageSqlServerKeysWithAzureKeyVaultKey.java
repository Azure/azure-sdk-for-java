// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.models.SkuName;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlServerKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure SQL sample for managing SQL secrets (Server Keys) using Azure Key Vault -
 *  - Create a SQL Server with "system assigned" managed service identity.
 *  - Create an Azure Key Vault with giving access to the SQL Server
 *  - Create, get, list and delete SQL Server Keys
 *  - Delete SQL Server
 */

public class ManageSqlServerKeysWithAzureKeyVaultKey {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @param objectId the object ID of the service principal/user used to authenticate to Azure
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager, String objectId) {
        final String sqlServerName = Utils.randomResourceName(azureResourceManager, "sqlsrv", 20);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgsql", 20);
        final String vaultName = Utils.randomResourceName(azureResourceManager, "sqlkv", 20);
        final String keyName = Utils.randomResourceName(azureResourceManager, "sqlkey", 20);
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = Utils.password();

        try {

            // ============================================================
            // Create a SQL Server with system assigned managed service identity.
            System.out.println("Creating a SQL Server with system assigned managed service identity");

            SqlServer sqlServer = azureResourceManager.sqlServers().define(sqlServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .withSystemAssignedManagedServiceIdentity()
                .create();

            Utils.print(sqlServer);

            // ============================================================
            // Create an Azure Key Vault and set the access policies.
            System.out.println("Creating an Azure Key Vault and set the access policies");

            Vault vault = azureResourceManager.vaults().define(vaultName)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .defineAccessPolicy()
                    .forObjectId(sqlServer.systemAssignedManagedServiceIdentityPrincipalId())
                    .allowKeyPermissions(KeyPermissions.WRAP_KEY, KeyPermissions.UNWRAP_KEY, KeyPermissions.GET, KeyPermissions.LIST)
                    .attach()
                .defineAccessPolicy()
                    .forServicePrincipal(objectId)
                    .allowKeyAllPermissions()
                    .attach()
                .withSku(SkuName.PREMIUM)
                .withSoftDeleteEnabled()
                .create();

            ResourceManagerUtils.sleep(Duration.ofMinutes(3));

            List<KeyOperation> keyOperations = new ArrayList<>();
            for (KeyOperation operation : KeyOperation.values()) {
                if (operation != KeyOperation.IMPORT) {
                    keyOperations.add(operation);
                }
            }

            Key keyBundle = vault.keys().define(keyName)
                .withKeyTypeToCreate(KeyType.RSA_HSM)
                .withKeyOperations(keyOperations)
                .create();

            // ============================================================
            // Create a SQL server key with Azure Key Vault key.
            System.out.println("Creating a SQL server key with Azure Key Vault key");

            String keyUri = keyBundle.getJsonWebKey().getId();

            // Work around for SQL server key name must be formatted as "vault_key_version"
            String serverKeyName = String.format("%s_%s_%s", vaultName, keyName,
                keyUri.substring(keyUri.lastIndexOf("/") + 1));

            SqlServerKey sqlServerKey = sqlServer.serverKeys().define()
                .withAzureKeyVaultKey(keyUri)
                .create();

            Utils.print(sqlServerKey);


            // Validate key exists by getting key
            System.out.println("Validating key exists by getting the key");

            sqlServerKey = sqlServer.serverKeys().get(serverKeyName);

            Utils.print(sqlServerKey);


            // Validate key exists by listing keys
            System.out.println("Validating key exists by listing keys");

            List<SqlServerKey> serverKeys = sqlServer.serverKeys().list();
            for (SqlServerKey item : serverKeys) {
                Utils.print(item);
            }


            // Delete key
            System.out.println("Deleting the key");

            azureResourceManager.sqlServers().serverKeys().deleteBySqlServer(rgName, sqlServerName, serverKeyName);


            // Delete the SQL Server.
            System.out.println("Deleting a Sql Server");
            azureResourceManager.sqlServers().deleteById(sqlServer.id());
            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (Exception e) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            }
        }
    }

    /**
     * Main entry point.
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
}
