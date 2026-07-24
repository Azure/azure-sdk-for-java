// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.samples.SampleUtils;
import com.azure.resourcemanager.sql.models.CreateMode;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlServer;

import java.util.Arrays;
import java.util.List;

/**
 * Azure SQL sample for managing SQL databases across multiple regions.
 *  - Create a master SQL Server and database in one region
 *  - Create secondary SQL Servers in other regions with read-only replicas of the master database
 *  - Add a firewall rule to every SQL Server
 */
public final class ManageSqlDatabasesAcrossRegions {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rgRSSDRE", 20);
        final String masterServerName = SampleUtils.randomResourceName(azureResourceManager, "master-sql", 20);
        final String secondary1Name = SampleUtils.randomResourceName(azureResourceManager, "slave1-sql", 20);
        final String secondary2Name = SampleUtils.randomResourceName(azureResourceManager, "slave2-sql", 20);
        final String databaseName = "mydatabase";
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = SampleUtils.password();

        try {
            // Create the master SQL Server and database.
            SqlServer masterSqlServer = azureResourceManager.sqlServers()
                .define(masterServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .create();

            SqlDatabase masterDatabase = masterSqlServer.databases().define(databaseName).withBasicEdition().create();

            // Create secondary SQL Servers, each holding a read-only replica of the master database.
            SqlServer secondarySqlServer1 = createSecondaryServer(azureResourceManager, secondary1Name, rgName,
                Region.US_EAST2, administratorLogin, administratorPassword, databaseName, masterDatabase);
            SqlServer secondarySqlServer2 = createSecondaryServer(azureResourceManager, secondary2Name, rgName,
                Region.US_SOUTH_CENTRAL, administratorLogin, administratorPassword, databaseName, masterDatabase);

            // Add a firewall rule to each SQL Server to allow access from an on-premises client.
            List<SqlServer> sqlServers = Arrays.asList(masterSqlServer, secondarySqlServer1, secondarySqlServer2);
            for (SqlServer sqlServer : sqlServers) {
                sqlServer.firewallRules().define("allowedClient").withIpAddress("10.10.10.10").create();
            }

            System.out.println("Created master and secondary SQL Servers across regions.");

            // Clean up the SQL Servers.
            for (SqlServer sqlServer : sqlServers) {
                azureResourceManager.sqlServers().deleteById(sqlServer.id());
            }
            return true;
        } finally {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    private static SqlServer createSecondaryServer(AzureResourceManager azureResourceManager, String serverName,
        String rgName, Region region, String administratorLogin, String administratorPassword, String databaseName,
        SqlDatabase masterDatabase) {
        SqlServer sqlServer = azureResourceManager.sqlServers()
            .define(serverName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAdministratorLogin(administratorLogin)
            .withAdministratorPassword(administratorPassword)
            .create();

        sqlServer.databases()
            .define(databaseName)
            .withSourceDatabase(masterDatabase)
            .withMode(CreateMode.ONLINE_SECONDARY)
            .create();

        return sqlServer;
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ManageSqlDatabasesAcrossRegions() {
    }
}
