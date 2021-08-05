// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.ServiceEndpointType;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlVirtualNetworkRule;

import java.util.List;

/**
 * Azure SQL sample for managing SQL Virtual Network Rules
 *  - Create a Virtual Network with two subnets.
 *  - Create a SQL Server along with one virtual network rule.
 *  - Add another virtual network rule in the SQL Server
 *  - Get a virtual network rule.
 *  - Update a virtual network rule.
 *  - List all virtual network rules.
 *  - Delete a virtual network.
 *  - Delete Sql Server
 */

public class ManageSqlVirtualNetworkRules {
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
        final String vnetName = Utils.randomResourceName(azureResourceManager, "vnetsql", 20);

        try {

            // ============================================================
            // Create a virtual network with two subnets.
            System.out.println("Create a virtual network with two subnets: subnet1 and subnet2");

            Network virtualNetwork = azureResourceManager.networks().define(vnetName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAddressSpace("192.168.0.0/16")
                .defineSubnet("subnet1")
                    .withAddressPrefix("192.168.1.0/24")
                    .withAccessFromService(ServiceEndpointType.MICROSOFT_SQL)
                    .attach()
                .withSubnet("subnet2", "192.168.2.0/24")
                .create();

            System.out.println("Created a virtual network");
            // Print the virtual network details
            Utils.print(virtualNetwork);

            // ============================================================
            // Create a SQL Server, with one virtual network rule.
            System.out.println("Create a SQL server with one virtual network rule");

            SqlServer sqlServer = azureResourceManager.sqlServers().define(sqlServerName)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .withoutAccessFromAzureServices()
                .defineVirtualNetworkRule("virtualNetworkRule1")
                    .withSubnet(virtualNetwork.id(), "subnet1")
                    .attach()
                .create();

            Utils.print(sqlServer);


            // ============================================================
            // Get the virtual network rule created above.
            SqlVirtualNetworkRule virtualNetworkRule = azureResourceManager.sqlServers().virtualNetworkRules()
                .getBySqlServer(rgName, sqlServerName, "virtualNetworkRule1");

            Utils.print(virtualNetworkRule);


            // ============================================================
            // Add new virtual network rules.
            System.out.println("adding another virtual network rule in existing SQL Server");
            virtualNetworkRule = sqlServer.virtualNetworkRules()
                .define("virtualNetworkRule2")
                .withSubnet(virtualNetwork.id(), "subnet2")
                .ignoreMissingSqlServiceEndpoint()
                .create();

            Utils.print(virtualNetworkRule);


            // ============================================================
            // Update a virtual network rules.
            System.out.println("Updating an existing virtual network rules in SQL Server.");
            virtualNetworkRule.update()
                .withSubnet(virtualNetwork.id(), "subnet1")
                .apply();

            Utils.print(virtualNetworkRule);


            // ============================================================
            // List and delete all virtual network rules.
            System.out.println("Listing all virtual network rules in SQL Server.");

            List<SqlVirtualNetworkRule> virtualNetworkRules = sqlServer.virtualNetworkRules().list();
            for (SqlVirtualNetworkRule vnetRule : virtualNetworkRules) {
                // Delete the virtual network rule.
                System.out.println("Deleting a virtual network rule");
                vnetRule.delete();
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
}
