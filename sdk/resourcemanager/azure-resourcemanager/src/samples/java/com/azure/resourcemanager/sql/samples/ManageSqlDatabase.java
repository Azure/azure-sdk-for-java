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
import com.azure.resourcemanager.sql.models.PrincipalType;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseStandardServiceObjective;
import com.azure.resourcemanager.sql.models.SqlFirewallRule;
import com.azure.resourcemanager.sql.models.SqlServer;

/**
 * Azure SQL sample for managing SQL Database.
 *  - Create a Microsoft Entra-only SQL Server (administered by a managed identity) with two firewall rules
 *  - Create a database and change its performance level (SKU)
 *  - List and delete firewall rules, then create a new one
 *  - Delete the database and the SQL Server
 */
public final class ManageSqlDatabase {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rgRSDSI", 20);
        final String sqlServerName = SampleUtils.randomResourceName(azureResourceManager, "sqlserver", 20);
        final String identityName = SampleUtils.randomResourceName(azureResourceManager, "sqladmin", 20);
        final String databaseName = "mydatabase";

        try {
            // Create a managed identity to act as the SQL Server's Microsoft Entra administrator (no password needed).
            Identity adminIdentity = azureResourceManager.identities()
                .define(identityName)
                .withRegion(Region.US_WEST3)
                .withNewResourceGroup(rgName)
                .create();

            // Create a Microsoft Entra-only SQL Server with two firewall rules.
            SqlServer sqlServer = azureResourceManager.sqlServers()
                .define(sqlServerName)
                .withRegion(Region.US_WEST3)
                .withExistingResourceGroup(rgName)
                .withAzureActiveDirectoryOnlyAuthentication()
                .withExternalActiveDirectoryAdministrator(identityName, adminIdentity.principalId(),
                    PrincipalType.APPLICATION)
                .defineFirewallRule("firewallRule1")
                .withIpAddress("10.0.0.1")
                .attach()
                .defineFirewallRule("firewallRule2")
                .withIpAddressRange("10.2.0.1", "10.2.0.10")
                .attach()
                .create();

            // Create a database in the SQL Server.
            SqlDatabase database = sqlServer.databases().define(databaseName).create();

            // Change the performance level (SKU) of the database.
            database.update()
                .withStandardEdition(SqlDatabaseStandardServiceObjective.S3)
                .withMaxSizeBytes(1024L * 1024L * 1024L * 20L)
                .apply();

            // List and delete all firewall rules.
            for (SqlFirewallRule firewallRule : sqlServer.firewallRules().list()) {
                firewallRule.delete();
            }

            // Add a new firewall rule.
            sqlServer.firewallRules().define("myFirewallRule").withIpAddress("10.10.10.10").create();

            // Delete the database and the SQL Server.
            database.delete();
            azureResourceManager.sqlServers().deleteById(sqlServer.id());
            return true;
        } finally {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        }
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

    private ManageSqlDatabase() {
    }
}
