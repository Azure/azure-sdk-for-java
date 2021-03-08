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
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.sql.models.SqlFirewallRule;
import com.azure.resourcemanager.sql.models.SqlServer;

import java.util.List;

/**
 * Azure SQL sample for managing SQL Database -
 *  - Create a SQL Server along with 2 firewalls.
 *  - Add another firewall in the SQL Server
 *  - List all firewalls.
 *  - Get a firewall.
 *  - Update a firewall.
 *  - Delete a firewall.
 *  - Add and delete a firewall as part of update of SQL Server
 *  - Delete Sql Server
 */

public final class ManageSqlFirewallRules {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String sqlServerName = Utils.randomResourceName(azureResourceManager, "sqlserver", 20);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgRSSDFW", 20);
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = Utils.password();
        final String firewallRuleIPAddress = "10.0.0.1";
        final String firewallRuleStartIPAddress = "10.2.0.1";
        final String firewallRuleEndIPAddress = "10.2.0.10";
        final String myFirewallName = "myFirewallRule";
        final String myFirewallRuleIPAddress = "10.10.10.10";
        final String otherFirewallRuleStartIPAddress = "121.12.12.1";
        final String otherFirewallRuleEndIPAddress = "121.12.12.10";
        try {

            // ============================================================
            // Create a SQL Server, with 2 firewall rules.
            System.out.println("Create a SQL server with 2 firewall rules adding a single IP Address and a range of IP Addresses");

            SqlServer sqlServer = azureResourceManager.sqlServers().define(sqlServerName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withAdministratorLogin(administratorLogin)
                    .withAdministratorPassword(administratorPassword)
                    .defineFirewallRule("filewallRule1").withIpAddress(firewallRuleIPAddress).attach()
                    .defineFirewallRule("filewallRule2")
                        .withIpAddressRange(firewallRuleStartIPAddress, firewallRuleEndIPAddress).attach()
                    .create();

            Utils.print(sqlServer);

            // ============================================================
            // List and delete all firewall rules.
            System.out.println("Listing all firewall rules in SQL Server.");

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
            System.out.println("Creating a firewall rule in existing SQL Server");
            SqlFirewallRule firewallRule = sqlServer.firewallRules().define(myFirewallName)
                    .withIpAddress(myFirewallRuleIPAddress)
                    .create();

            Utils.print(firewallRule);

            System.out.println("Get a particular firewall rule in SQL Server");

            firewallRule = sqlServer.firewallRules().get(myFirewallName);
            Utils.print(firewallRule);

            System.out.println("Deleting and adding new firewall rules as part of SQL Server update.");
            sqlServer.update()
                    .withoutFirewallRule(myFirewallName)
                    .defineFirewallRule("filewallRule2")
                        .withIpAddressRange(otherFirewallRuleStartIPAddress, otherFirewallRuleEndIPAddress)
                        .attach()
                    .apply();

            for (SqlFirewallRule sqlFirewallRule: sqlServer.firewallRules().list()) {
                // Print information of the firewall rule.
                Utils.print(sqlFirewallRule);
            }

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

    private ManageSqlFirewallRules() {

    }
}
