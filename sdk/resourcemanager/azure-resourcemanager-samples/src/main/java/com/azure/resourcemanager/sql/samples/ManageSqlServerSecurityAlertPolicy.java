// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.sql.models.SampleName;
import com.azure.resourcemanager.sql.models.SecurityAlertPolicyState;
import com.azure.resourcemanager.sql.models.SqlDatabaseStandardServiceObjective;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlServerSecurityAlertPolicy;
import com.azure.resourcemanager.storage.models.StorageAccount;

/**
 * Azure SQL sample for managing SQL Server Security Alert Policy
 *  - Create a SQL Server.
 *  - Create an Azure Storage Account and get the storage account blob entry point
 *  - Create a Server Security Alert Policy
 *  - Get the Server Security Alert Policy.
 *  - Update the Server Security Alert Policy.
 *  - Delete the Sql Server
 */
public class ManageSqlServerSecurityAlertPolicy {
    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String sqlServerName = azure.sdkContext().randomResourceName("sql", 20);
        final String storageAccountName = azure.sdkContext().randomResourceName("sqlsa", 20);
        final String rgName = azure.sdkContext().randomResourceName("rgsql", 20);
        final Region region = Region.US_EAST;
        final String dbName = "dbSample";
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = Utils.password();

        try {

            // ============================================================
            // Create a primary SQL Server with a sample database.
            System.out.println("Creating a primary SQL Server with a sample database");

            SqlServer sqlServer = azure.sqlServers().define(sqlServerName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .defineDatabase(dbName)
                    .fromSample(SampleName.ADVENTURE_WORKS_LT)
                    .withStandardEdition(SqlDatabaseStandardServiceObjective.S0)
                    .attach()
                .create();

            Utils.print(sqlServer);

            // ============================================================
            // Create an Azure Storage Account and get the storage account blob entry point.
            System.out.println("Creating an Azure Storage Account and a storage account blob");
            StorageAccount storageAccount = azure.storageAccounts().define(storageAccountName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .create();
            String accountKey = storageAccount.getKeys().get(0).value();
            String blobEntrypoint = storageAccount.endPoints().primary().blob();

            // ============================================================
            // Create a Server Security Alert Policy.
            System.out.println("Creating a Server Security Alert Policy");
            sqlServer.serverSecurityAlertPolicies().define()
                .withState(SecurityAlertPolicyState.ENABLED)
                .withEmailAccountAdmins()
                .withStorageEndpoint(blobEntrypoint, accountKey)
                .withDisabledAlerts("Access_Anomaly", "Sql_Injection")
                .withRetentionDays(5)
                .create();


            // ============================================================
            // Get the Server Security Alert Policy.
            System.out.println("Getting the Server Security Alert Policy");
            SqlServerSecurityAlertPolicy sqlSecurityAlertPolicy = sqlServer.serverSecurityAlertPolicies().get();


            // ============================================================
            // Update the Server Security Alert Policy.
            System.out.println("Updating the Server Security Alert Policy");
            sqlSecurityAlertPolicy.update()
                .withoutEmailAccountAdmins()
                .withEmailAddresses("testSecurityAlert@contoso.com")
                .withRetentionDays(1)
                .apply();


            // Delete the SQL Servers.
            System.out.println("Deleting the Sql Servers");
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

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
