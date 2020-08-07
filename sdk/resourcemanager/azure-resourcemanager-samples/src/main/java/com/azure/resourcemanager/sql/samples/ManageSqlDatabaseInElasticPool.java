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
import com.azure.resourcemanager.sql.models.DatabaseEdition;
import com.azure.resourcemanager.sql.models.ElasticPoolActivity;
import com.azure.resourcemanager.sql.models.ElasticPoolDatabaseActivity;
import com.azure.resourcemanager.sql.models.ElasticPoolEdition;
import com.azure.resourcemanager.sql.models.ServiceObjectiveName;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlElasticPool;
import com.azure.resourcemanager.sql.models.SqlServer;

/**
 * Azure SQL sample for managing SQL Database -
 *  - Create a SQL Server with elastic pool and 2 databases
 *  - Create another database and add it to elastic pool through database update
 *  - Create one more database and add it to elastic pool through elastic pool update.
 *  - List and print databases in the elastic pool
 *  - Remove a database from elastic pool.
 *  - List and print elastic pool activities
 *  - List and print elastic pool database activities
 *  - Add another elastic pool in existing SQL Server.
 *  - Delete database, elastic pools and SQL Server
 */

public final class ManageSqlDatabaseInElasticPool {
    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String sqlServerName = azure.sdkContext().randomResourceName("sqlserver", 20);
        final String rgName = azure.sdkContext().randomResourceName("rgRSSDEP", 20);
        final String elasticPoolName = "myElasticPool";
        final String elasticPool2Name = "secondElasticPool";
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = Utils.password();
        final String database1Name = "myDatabase1";
        final String database2Name = "myDatabase2";
        final String anotherDatabaseName = "myAnotherDatabase";
        final ElasticPoolEdition elasticPoolEdition = ElasticPoolEdition.STANDARD;

        try {
            // ============================================================
            // Create a SQL Server, with 2 firewall rules.

            SqlServer sqlServer = azure.sqlServers().define(sqlServerName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withAdministratorLogin(administratorLogin)
                    .withAdministratorPassword(administratorPassword)
                    .withNewElasticPool(elasticPoolName, elasticPoolEdition, database1Name, database2Name)
                    .create();

            Utils.print(sqlServer);

            // ============================================================
            // List and prints the elastic pools
            for (SqlElasticPool elasticPool: sqlServer.elasticPools().list()) {
                Utils.print(elasticPool);
            }

            // ============================================================
            // Get and prints the elastic pool
            SqlElasticPool elasticPool = sqlServer.elasticPools().get(elasticPoolName);
            Utils.print(elasticPool);

            // ============================================================
            // Change DTUs in the elastic pools.
            elasticPool = elasticPool.update()
                    .withDtu(200)
                    .withStorageCapacity(204800 * 1024 * 1024L)
                    .withDatabaseDtuMin(10)
                    .withDatabaseDtuMax(50)
                    .apply();

            Utils.print(elasticPool);

            System.out.println("Start ------- Current databases in the elastic pool");
            for (SqlDatabase databaseInElasticPool: elasticPool.listDatabases()) {
                Utils.print(databaseInElasticPool);
            }
            System.out.println("End --------- Current databases in the elastic pool");

            // ============================================================
            // Create a Database in SQL server created above.
            System.out.println("Creating a database");

            SqlDatabase database = sqlServer.databases()
                    .define("myNewDatabase")
                    .create();
            Utils.print(database);

            System.out.println("Start ------- Current databases in the elastic pool");
            for (SqlDatabase databaseInElasticPool: elasticPool.listDatabases()) {
                Utils.print(databaseInElasticPool);
            }
            System.out.println("End --------- Current databases in the elastic pool");

            // ============================================================
            // Move newly created database to the pool.
            System.out.println("Updating a database");
            database = database.update()
                    .withExistingElasticPool(elasticPoolName)
                    .apply();
            Utils.print(database);

            // ============================================================
            // Create another database and move it in elastic pool as update to the elastic pool.
            SqlDatabase anotherDatabase = sqlServer.databases().define(anotherDatabaseName)
                    .create();

            // ============================================================
            // Update the elastic pool to have newly created database.
            elasticPool.update()
                    .withExistingDatabase(anotherDatabase)
                    .apply();

            System.out.println("Start ------- Current databases in the elastic pool");
            for (SqlDatabase databaseInElasticPool: elasticPool.listDatabases()) {
                Utils.print(databaseInElasticPool);
            }
            System.out.println("End --------- Current databases in the elastic pool");

            // ============================================================
            // Remove the database from the elastic pool.
            System.out.println("Remove the database from the pool.");
            anotherDatabase = anotherDatabase.update()
                    .withoutElasticPool()
                    .withEdition(DatabaseEdition.STANDARD)
                    .withServiceObjective(ServiceObjectiveName.S3)
                    .withMaxSizeBytes(1024 * 1024 * 1024 * 20)
                    .apply();
            Utils.print(anotherDatabase);

            System.out.println("Start ------- Current databases in the elastic pool");
            for (SqlDatabase databaseInElasticPool: elasticPool.listDatabases()) {
                Utils.print(databaseInElasticPool);
            }
            System.out.println("End --------- Current databases in the elastic pool");


            // ============================================================
            // Get list of elastic pool's activities and print the same.
            System.out.println("Start ------- Activities in a elastic pool");
            for (ElasticPoolActivity activity: elasticPool.listActivities()) {
                Utils.print(activity);
            }
            System.out.println("End ------- Activities in a elastic pool");

            // ============================================================
            // Get list of elastic pool's database activities and print the same.

            System.out.println("Start ------- Activities in a elastic pool");
            for (ElasticPoolDatabaseActivity databaseActivity: elasticPool.listDatabaseActivities()) {
                Utils.print(databaseActivity);
            }
            System.out.println("End ------- Activities in a elastic pool");

            // ============================================================
            // List databases in the sql server and delete the same.
            System.out.println("List and delete all databases from SQL Server");
            for (SqlDatabase databaseInServer: sqlServer.databases().list()) {
                Utils.print(databaseInServer);
                // Can not delete reserved database "master"
                if (!databaseInServer.name().equals("master")) {
                    databaseInServer.delete();
                }
            }

            // ============================================================
            // Create another elastic pool in SQL Server
            System.out.println("Create ElasticPool in existing SQL Server");
            SqlElasticPool elasticPool2 = sqlServer.elasticPools().define(elasticPool2Name)
                    .withEdition(elasticPoolEdition)
                    .create();

            Utils.print(elasticPool2);

            // ============================================================
            // Deletes the elastic pool.
            System.out.println("Delete the elastic pool from the SQL Server");
            sqlServer.elasticPools().delete(elasticPoolName);
            sqlServer.elasticPools().delete(elasticPool2Name);

            // ============================================================
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

    private ManageSqlDatabaseInElasticPool() {

    }


}
