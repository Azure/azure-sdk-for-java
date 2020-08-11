// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.models.SkuName;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlServerKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyType;

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
     * @param azure instance of the azure client
     * @param objectId the object ID of the service principal/user used to authenticate to Azure
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure, String objectId) {
        final String sqlServerName = azure.sdkContext().randomResourceName("sqlsrv", 20);
        final String rgName = azure.sdkContext().randomResourceName("rgsql", 20);
        final String vaultName = azure.sdkContext().randomResourceName("sqlkv", 20);
        final String keyName = azure.sdkContext().randomResourceName("sqlkey", 20);
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = Utils.password();

        try {

            // ============================================================
            // Create a SQL Server with system assigned managed service identity.
            System.out.println("Creating a SQL Server with system assigned managed service identity");

            SqlServer sqlServer = azure.sqlServers().define(sqlServerName)
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

            Vault vault = azure.vaults().define(vaultName)
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

            SdkContext.sleep(3 * 60 * 1000);

            Key keyBundle = vault.keys().define(keyName)
                .withKeyTypeToCreate(KeyType.RSA_HSM)
                .withKeyOperations(new ArrayList<>(KeyOperation.values()))
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

            azure.sqlServers().serverKeys().deleteBySqlServer(rgName, sqlServerName, serverKeyName);


            // Delete the SQL Server.
            System.out.println("Deleting a Sql Server");
            azure.sqlServers().deleteById(sqlServer.id());
            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (Exception e) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
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
            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .build();
            final Configuration configuration = Configuration.getGlobalConfiguration();

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure, configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
