/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.samples;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Azure Storage sample for managing SQL Database -
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
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String sqlServerName = Utils.createRandomName("sqlserver");
        final String rgName = Utils.createRandomName("rgRSSDFW");
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = "myS3cureP@ssword";
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
                    .withIPAddress(myFirewallRuleIPAddress)
                    .create();

            Utils.print(firewallRule);

            System.out.println("Get a particular firewall rule in SQL Server");

            firewallRule = sqlServer.firewallRules().get(myFirewallName);
            Utils.print(firewallRule);

            System.out.println("Deleting and adding new firewall rules as part of SQL Server update.");
            sqlServer.update()
                    .withoutFirewallRule(myFirewallName)
                    .withNewFirewallRule(otherFirewallRuleStartIPAddress, otherFirewallRuleEndIPAddress)
                    .apply();

            for (SqlFirewallRule sqlFirewallRule: sqlServer.firewallRules().list()) {
                // Print information of the firewall rule.
                Utils.print(sqlFirewallRule);
            }

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
                azure.resourceGroups().deleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            }
            catch (Exception e) {
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
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));


            ApplicationTokenCredentials credentials = ApplicationTokenCredentials.fromFile(credFile);
            RestClient restClient = new RestClient.Builder()
                    .withBaseUrl(AzureEnvironment.AZURE, AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                    .withReadTimeout(150, TimeUnit.SECONDS)
                    .withLogLevel(LogLevel.BODY)
                    .withCredentials(credentials).build();
            Azure azure = Azure.authenticate(restClient, credentials.domain(), credentials.defaultSubscriptionId()).withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageSqlFirewallRules() {

    }
}