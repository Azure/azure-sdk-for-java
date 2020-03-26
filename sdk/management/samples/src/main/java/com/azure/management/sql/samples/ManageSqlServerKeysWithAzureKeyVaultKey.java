/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql.samples;


import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.ApplicationTokenCredential;
import com.azure.management.Azure;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.keyvault.Key;
import com.azure.management.keyvault.KeyPermissions;
import com.azure.management.keyvault.Vault;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.samples.Utils;
import com.azure.management.sql.SqlServer;
import com.azure.management.sql.SqlServerKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyType;

import java.io.File;
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
        // [SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine", Justification="Serves as an example, not for deployment. Please change when using this in your code.")]
        final String administratorPassword = "myS3cureP@ssword";

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
                .withSoftDeleteEnabled()
                .create();

            SdkContext.sleep(3 * 60 * 1000);

            Key keyBundle = vault.keys().define(keyName)
                .withKeyTypeToCreate(KeyType.RSA)
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
                azure.resourceGroups().deleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            }
            catch (Exception e) {
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
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            ApplicationTokenCredential credentials = ApplicationTokenCredential.fromFile(credFile);
            RestClient restClient = new RestClientBuilder()
                    .withBaseUrl(AzureEnvironment.AZURE, AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                    .withSerializerAdapter(new AzureJacksonAdapter())
//                .withReadTimeout(150, TimeUnit.SECONDS)
                    .withHttpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY))
                    .withCredential(credentials).buildClient();
            Azure azure = Azure.authenticate(restClient, credentials.getDomain(), credentials.getDefaultSubscriptionId()).withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure, credentials.getClientId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
