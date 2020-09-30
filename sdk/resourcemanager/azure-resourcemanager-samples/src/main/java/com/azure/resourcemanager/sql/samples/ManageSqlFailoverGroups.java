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
import com.azure.resourcemanager.sql.models.SampleName;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseStandardServiceObjective;
import com.azure.resourcemanager.sql.models.SqlFailoverGroup;
import com.azure.resourcemanager.sql.models.SqlServer;

import java.time.Duration;

/**
 * Azure SQL sample for managing SQL Failover Groups
 *  - Create a primary SQL Server with a sample database and a secondary SQL Server.
 *  - Get a failover group from the primary SQL server to the secondary SQL server.
 *  - Update a failover group.
 *  - List all failover groups.
 *  - Delete a failover group.
 *  - Delete Sql Server
 */
public class ManageSqlFailoverGroups {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String sqlPrimaryServerName = Utils.randomResourceName(azureResourceManager, "sqlpri", 20);
        final String sqlSecondaryServerName = Utils.randomResourceName(azureResourceManager, "sqlsec", 20);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgsql", 20);
        final String failoverGroupName = Utils.randomResourceName(azureResourceManager, "fog", 20);
        final String dbName = "dbSample";
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = Utils.password();

        try {

            // ============================================================
            // Create a primary SQL Server with a sample database.
            System.out.println("Creating a primary SQL Server with a sample database");

            SqlServer sqlPrimaryServer = azureResourceManager.sqlServers().define(sqlPrimaryServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .defineDatabase(dbName)
                    .fromSample(SampleName.ADVENTURE_WORKS_LT)
                    .withStandardEdition(SqlDatabaseStandardServiceObjective.S0)
                    .attach()
                .create();

            Utils.print(sqlPrimaryServer);

            // ============================================================
            // Create a secondary SQL Server with a sample database.
            System.out.println("Creating a secondary SQL Server with a sample database");

            SqlServer sqlSecondaryServer = azureResourceManager.sqlServers().define(sqlSecondaryServerName)
                .withRegion(Region.US_EAST2)
                .withExistingResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .create();

            Utils.print(sqlSecondaryServer);


            // ============================================================
            // Create a Failover Group from the primary SQL server to the secondary SQL server.
            System.out.println("Creating a Failover Group from the primary SQL server to the secondary SQL server");

            SqlFailoverGroup failoverGroup = sqlPrimaryServer.failoverGroups().define(failoverGroupName)
                .withManualReadWriteEndpointPolicy()
                .withPartnerServerId(sqlSecondaryServer.id())
                .withReadOnlyEndpointPolicyDisabled()
                .create();

            Utils.print(failoverGroup);

            // ============================================================
            // Get the Failover Group from the secondary SQL server.
            System.out.println("Getting the Failover Group from the secondary SQL server");

            sqlSecondaryServer.failoverGroups().get(failoverGroup.name());

            Utils.print(failoverGroup);


            // ============================================================
            // Update the Failover Group Endpoint policies and tags.
            System.out.println("Updating the Failover Group Endpoint policies and tags");

            failoverGroup.update()
                .withAutomaticReadWriteEndpointPolicyAndDataLossGracePeriod(120)
                .withReadOnlyEndpointPolicyEnabled()
                .withTag("tag1", "value1")
                .apply();

            Utils.print(failoverGroup);


            // ============================================================
            // Update the Failover Group to add database and change read-write endpoint's failover policy.
            System.out.println("Updating the Failover Group to add database and change read-write endpoint's failover policy");

            SqlDatabase db = sqlPrimaryServer.databases().get(dbName);

            Utils.print(db);

            failoverGroup.update()
                .withManualReadWriteEndpointPolicy()
                .withReadOnlyEndpointPolicyDisabled()
                .withNewDatabaseId(db.id())
                .apply();

            Utils.print(failoverGroup);


            // ============================================================
            // List the Failover Group on the secondary server.
            System.out.println("Listing the Failover Group on the secondary server");

            for (SqlFailoverGroup item : sqlSecondaryServer.failoverGroups().list()) {
                Utils.print(item);
            }

            // ============================================================
            // Get the database from the secondary SQL server.
            System.out.println("Getting the database from the secondary server");
            ResourceManagerUtils.sleep(Duration.ofMinutes(3));

            db = sqlSecondaryServer.databases().get(dbName);

            Utils.print(db);

            // ============================================================
            // Delete the Failover Group.
            System.out.println("Deleting the Failover Group");

            sqlPrimaryServer.failoverGroups().delete(failoverGroup.name());



            // Delete the SQL Servers.
            System.out.println("Deleting the Sql Servers");
            azureResourceManager.sqlServers().deleteById(sqlPrimaryServer.id());
            azureResourceManager.sqlServers().deleteById(sqlSecondaryServer.id());
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
