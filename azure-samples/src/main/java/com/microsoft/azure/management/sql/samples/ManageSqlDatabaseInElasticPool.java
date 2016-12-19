/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.sql.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.sql.DatabaseEditions;
import com.microsoft.azure.management.sql.ElasticPoolActivity;
import com.microsoft.azure.management.sql.ElasticPoolDatabaseActivity;
import com.microsoft.azure.management.sql.ElasticPoolEditions;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlElasticPool;
import com.microsoft.azure.management.sql.SqlServer;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;

/**
 * Azure Storage sample for managing SQL Database -
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
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        final String sqlServerName = Utils.createRandomName("sqlserver");
        final String rgName = Utils.createRandomName("rgRSSDEP");
        final String elasticPoolName = "myElasticPool";
        final String elasticPool2Name = "secondElasticPool";
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = "myS3cureP@ssword";
        final String database1Name = "myDatabase1";
        final String database2Name = "myDatabase2";
        final String anotherDatabaseName = "myAnotherDatabase";
        final ElasticPoolEditions elasticPoolEdition = ElasticPoolEditions.STANDARD;

        try {

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

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

                SqlDatabase database = sqlServer.databases().define("myNewDatabase")
                        .withoutElasticPool()
                        .withoutSourceDatabaseId()
                        .withEdition(DatabaseEditions.BASIC)
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
                        .withoutElasticPool()
                        .withoutSourceDatabaseId()
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
                        .withEdition(DatabaseEditions.STANDARD)
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
                    databaseInServer.delete();
                }

                // ============================================================
                // Create another elastic pool in SQL Server
                System.out.println("Create ElasticPool in existing SQL Server");
                SqlElasticPool elasticPool2 = sqlServer.elasticPools().define(elasticPool2Name)
                        .withEdition(elasticPoolEdition)
                        .withDtu(100)
                        .withDatabaseDtuMax(50)
                        .withDatabaseDtuMin(10)
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
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageSqlDatabaseInElasticPool() {

    }


}