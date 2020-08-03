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
import com.azure.resourcemanager.sql.models.ServiceObjectiveName;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlFirewallRule;
import com.azure.resourcemanager.sql.models.SqlServer;

import java.util.List;

/**
 * Azure SQL sample for managing SQL Database -
 *  - Create a SQL Server along with 2 firewalls.
 *  - Create a database in SQL server
 *  - Change performance level (SKU) of SQL Database
 *  - List and delete firewalls.
 *  - Create another firewall in the SQlServer
 *  - Delete database, firewall and SQL Server
 */

public final class ManageSqlDatabase {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String sqlServerName = azure.sdkContext().randomResourceName("sqlserver", 20);
        final String rgName = azure.sdkContext().randomResourceName("rgRSDSI", 20);
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = Utils.password();
        final String firewallRuleIPAddress = "10.0.0.1";
        final String firewallRuleStartIPAddress = "10.2.0.1";
        final String firewallRuleEndIPAddress = "10.2.0.10";
        final String databaseName = "mydatabase";
        try {

            // ============================================================
            // Create a SQL Server, with 2 firewall rules.
            SqlServer sqlServer = azure.sqlServers().define(sqlServerName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withAdministratorLogin(administratorLogin)
                    .withAdministratorPassword(administratorPassword)
                    .withNewFirewallRule(firewallRuleIPAddress)
                    .withNewFirewallRule(firewallRuleStartIPAddress, firewallRuleEndIPAddress)
                    .create();

            Utils.print(sqlServer);

            // ============================================================
            // Create a Database in SQL server created above.
            System.out.println("Creating a database");

            SqlDatabase database = sqlServer.databases()
                    .define(databaseName)
                    .create();
            Utils.print(database);

            // ============================================================
            // Update the edition of database.
            System.out.println("Updating a database");
            database = database.update()
                    .withEdition(DatabaseEdition.STANDARD)
                    .withServiceObjective(ServiceObjectiveName.S3)
                    .withMaxSizeBytes(1024 * 1024 * 1024 * 20)
                    .apply();
            Utils.print(database);

            // ============================================================
            // List and delete all firewall rules.
            System.out.println("Listing all firewall rules");

            List<SqlFirewallRule> firewallRules = sqlServer.firewallRules().list();
            for (SqlFirewallRule firewallRule: firewallRules) {
                // Print information of the firewall rule.
                Utils.print(firewallRule);

                // Delete the firewall rule.
                System.out.println("Deleting a firewall rule");
                firewallRule.delete();
            }

            // ============================================================
            // Add new firewall rules.
            System.out.println("Creating a firewall rule for SQL Server");
            SqlFirewallRule firewallRule = sqlServer.firewallRules().define("myFirewallRule")
                    .withIpAddress("10.10.10.10")
                    .create();

            Utils.print(firewallRule);

            database.listUsageMetrics();

            // Delete the database.
            System.out.println("Deleting a database");
            database.delete();

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

    private ManageSqlDatabase() {

    }


}
