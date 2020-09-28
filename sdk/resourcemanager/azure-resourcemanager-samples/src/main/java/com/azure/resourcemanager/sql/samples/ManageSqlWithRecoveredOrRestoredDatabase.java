// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.sql.models.RestorePoint;
import com.azure.resourcemanager.sql.models.SampleName;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseStandardServiceObjective;
import com.azure.resourcemanager.sql.models.SqlRestorableDroppedDatabase;
import com.azure.resourcemanager.sql.models.SqlServer;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Azure SQL sample for managing point in time restore and recover a deleted SQL Database -
 *  - Create a SQL Server with two database from a pre-existing sample.
 *  - Create a new database from a point in time restore
 *  - Delete a database then restore it from a recoverable dropped database automatic backup
 *  - Delete databases and SQL Server
 */
public final class ManageSqlWithRecoveredOrRestoredDatabase {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String sqlServerName = Utils.randomResourceName(azureResourceManager, "sqlserver", 20);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgsql", 20);
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = Utils.password();
        final String dbToDeleteName = "db-to-delete";
        final String dbToRestoreName = "db-to-restore";
        try {

            // ============================================================
            // Create a SQL Server with two databases from a sample.
            System.out.println("Creating a SQL Server with two databases from a sample.");
            SqlServer sqlServer = azureResourceManager.sqlServers().define(sqlServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .defineDatabase(dbToDeleteName)
                    .fromSample(SampleName.ADVENTURE_WORKS_LT)
                    .withStandardEdition(SqlDatabaseStandardServiceObjective.S0)
                    .attach()
                .defineDatabase(dbToRestoreName)
                    .fromSample(SampleName.ADVENTURE_WORKS_LT)
                    .withStandardEdition(SqlDatabaseStandardServiceObjective.S0)
                    .attach()
                .create();
            Utils.print(sqlServer);

            // Sleep for 5 minutes to allow for the service to be aware of the new server and databases
            ResourceManagerUtils.sleep(Duration.ofMinutes(5));

            SqlDatabase dbToBeDeleted = sqlServer.databases()
                .get(dbToDeleteName);
            Utils.print(dbToBeDeleted);
            SqlDatabase dbToRestore = sqlServer.databases()
                .get(dbToRestoreName);
            Utils.print(dbToRestore);

            // ============================================================
            // Loop until a point in time restore is available.
            System.out.println("Loop until a point in time restore is available.");

            int retries = 50;
            while (retries > 0 && dbToRestore.listRestorePoints().size() == 0) {
                retries--;
                // Sleep for about 3 minutes
                ResourceManagerUtils.sleep(Duration.ofMinutes(3));
            }
            if (retries == 0) {
                return false;
            }

            RestorePoint restorePointInTime = dbToRestore.listRestorePoints().get(0);
            // Restore point might not be ready right away and we will have to wait for it.
            OffsetDateTime currentTime = OffsetDateTime.now();
            long waitForRestoreToBeReady = ChronoUnit.MILLIS.between(currentTime, restorePointInTime.earliestRestoreDate())
                    + 5 * 60 * 1000;
            System.out.printf("waitForRestoreToBeReady %d%n", waitForRestoreToBeReady);
            if (waitForRestoreToBeReady > 0) {
                ResourceManagerUtils.sleep(Duration.ofMillis(waitForRestoreToBeReady));
            }

            SqlDatabase dbRestorePointInTime = sqlServer.databases()
                .define("db-restore-pit")
                .fromRestorePoint(restorePointInTime)
                .create();
            Utils.print(dbRestorePointInTime);
            dbRestorePointInTime.delete();

            // ============================================================
            // Restore the database form a point in time restore which is 5 minutes ago.
            dbRestorePointInTime = sqlServer.databases()
                .define("db-restore-pit-2")
                .fromRestorePoint(restorePointInTime, OffsetDateTime.now().minusMinutes(5))
                .create();
            Utils.print(dbRestorePointInTime);
            dbRestorePointInTime.delete();

            // ============================================================
            // Delete the database than loop until the restorable dropped database backup is available.
            System.out.println("Deleting the database than loop until the restorable dropped database backup is available.");

            dbToBeDeleted.delete();
            retries = 24;
            while (retries > 0 && sqlServer.listRestorableDroppedDatabases().size() == 0) {
                retries--;
                // Sleep for about 5 minutes
                ResourceManagerUtils.sleep(Duration.ofMinutes(5));
            }
            SqlRestorableDroppedDatabase restorableDroppedDatabase = sqlServer.listRestorableDroppedDatabases().get(0);
            SqlDatabase dbRestoreDeleted = sqlServer.databases()
                .define("db-restore-deleted")
                .fromRestorableDroppedDatabase(restorableDroppedDatabase)
                .create();
            Utils.print(dbRestoreDeleted);

            // Delete databases
            dbToRestore.delete();
            dbRestoreDeleted.delete();

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

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
