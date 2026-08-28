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
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.samples.SampleUtils;
import com.azure.resourcemanager.sql.models.CreateMode;
import com.azure.resourcemanager.sql.models.PrincipalType;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlServer;

import java.util.Arrays;
import java.util.List;

/**
 * Azure SQL sample for managing SQL databases across multiple regions.
 *  - Create a master Microsoft Entra-only SQL Server and database in one region
 *  - Create a secondary SQL Server in another region with a read-only replica of the master database
 *  - Add a firewall rule to every SQL Server
 * <p>
 * All servers are administered by a single managed identity (Microsoft Entra-only authentication), so no SQL login or
 * password is created.
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
        final String secondaryName = SampleUtils.randomResourceName(azureResourceManager, "slave-sql", 20);
        final String identityName = SampleUtils.randomResourceName(azureResourceManager, "sqladmin", 20);
        final String databaseName = "mydatabase";

        try {
            // Create a managed identity that administers every SQL Server (Microsoft Entra-only, no password).
            Identity adminIdentity = azureResourceManager.identities()
                .define(identityName)
                .withRegion(Region.US_WEST3)
                .withNewResourceGroup(rgName)
                .create();

            // Create the master SQL Server and database.
            SqlServer masterSqlServer = azureResourceManager.sqlServers()
                .define(masterServerName)
                .withRegion(Region.US_WEST3)
                .withExistingResourceGroup(rgName)
                .withAzureActiveDirectoryOnlyAuthentication()
                .withExternalActiveDirectoryAdministrator(identityName, adminIdentity.principalId(),
                    PrincipalType.APPLICATION)
                .create();

            SqlDatabase masterDatabase = masterSqlServer.databases().define(databaseName).withBasicEdition().create();

            // Create a secondary SQL Server in another region holding a read-only replica of the master database.
            SqlServer secondarySqlServer = createSecondaryServer(azureResourceManager, secondaryName, rgName,
                Region.US_EAST2, identityName, adminIdentity, databaseName, masterDatabase);

            // Add a firewall rule to each SQL Server to allow access from an on-premises client.
            List<SqlServer> sqlServers = Arrays.asList(masterSqlServer, secondarySqlServer);
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
        String rgName, Region region, String adminLogin, Identity adminIdentity, String databaseName,
        SqlDatabase masterDatabase) {
        SqlServer sqlServer = azureResourceManager.sqlServers()
            .define(serverName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAzureActiveDirectoryOnlyAuthentication()
            .withExternalActiveDirectoryAdministrator(adminLogin, adminIdentity.principalId(),
                PrincipalType.APPLICATION)
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
