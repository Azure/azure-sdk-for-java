// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.sql.models.SampleName;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.storage.models.StorageAccount;

/**
 * Azure SQL sample for managing import/export SQL Database -
 *  - Create a SQL Server with one database from a pre-existing sample.
 *  - Create a storage account and export a database
 *  - Create a new database from a backup using the import functionality
 *  - Update an empty database with a backup database using the import functionality
 *  - Delete storage account, databases and SQL Server
 */
public final class ManageSqlImportExportDatabase {
    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String sqlServerName = azure.sdkContext().randomResourceName("sqlserver", 20);
        final String rgName = azure.sdkContext().randomResourceName("rgsql", 20);
        String storageName = azure.sdkContext().randomResourceName(sqlServerName, 23);
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = Utils.password();
        final String dbFromSampleName = "db-from-sample";
        try {

            // ============================================================
            // Create a SQL Server with one database from a sample.
            SqlServer sqlServer = azure.sqlServers().define(sqlServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .defineDatabase(dbFromSampleName)
                    .fromSample(SampleName.ADVENTURE_WORKS_LT)
                    .withBasicEdition()
                    .attach()
                .create();
            Utils.print(sqlServer);

            SqlDatabase dbFromSample = sqlServer.databases()
                .get(dbFromSampleName);
            Utils.print(dbFromSample);

            // ============================================================
            // Export a database from a SQL server created above to a new storage account within the same resource group.
            System.out.println("Exporting a database from a SQL server created above to a new storage account within the same resource group.");

            Creatable<StorageAccount> storageAccountCreatable = azure.storageAccounts()
                .define(storageName)
                .withRegion(sqlServer.regionName())
                .withExistingResourceGroup(sqlServer.resourceGroupName());

            dbFromSample.exportTo(storageAccountCreatable, "container-name", "dbfromsample.bacpac")
                .withSqlAdministratorLoginAndPassword(administratorLogin, administratorPassword)
                .execute();
            StorageAccount storageAccount = azure.storageAccounts().getByResourceGroup(sqlServer.resourceGroupName(), storageName);

            // ============================================================
            // Import a database within a new elastic pool from a storage account container created above.
            System.out.println("Importing a database within a new elastic pool from a storage account container created above.");

            SqlDatabase dbFromImport = sqlServer.databases()
                .define("db-from-import1")
                    .defineElasticPool("epi")
                        .withStandardPool()
                        .attach()
                    .importFrom(storageAccount, "container-name", "dbfromsample.bacpac")
                        .withSqlAdministratorLoginAndPassword(administratorLogin, administratorPassword)
                    .create();
            Utils.print(dbFromImport);

            // Delete the database.
            System.out.println("Deleting a database");
            dbFromImport.delete();

            // ============================================================
            // Create an empty database within an elastic pool.
            SqlDatabase dbEmpty = sqlServer.databases()
                .define("db-from-import2")
                .withExistingElasticPool("epi")
                .create();

            // ============================================================
            // Import data from a BACPAC to an empty database within an elastic pool.
            System.out.println("Importing data from a BACPAC to an empty database within an elastic pool.");

            dbEmpty
                .importBacpac(storageAccount, "container-name", "dbfromsample.bacpac")
                .withSqlAdministratorLoginAndPassword(administratorLogin, administratorPassword)
                .execute();
            Utils.print(dbFromImport);

            // Delete the storage account.
            System.out.println("Deleting the storage account");
            azure.storageAccounts().deleteById(storageAccount.id());

            // Delete the databases.
            System.out.println("Deleting the databases");
            dbEmpty.delete();
            dbFromSample.delete();

            // Delete the elastic pool.
            System.out.println("Deleting the elastic pool");
            sqlServer.elasticPools().delete("epi");

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
