// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.sql.models.CreateMode;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlFirewallRule;
import com.azure.resourcemanager.sql.models.SqlServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Azure SQL sample for managing SQL Database -
 *  - Create 3 SQL Servers in different region.
 *  - Create a master database in master SQL Server.
 *  - Create 2 more SQL Servers in different azure regions
 *  - Create secondary read only databases in these server with source as database in server created in step 1.
 *  - Create 5 virtual networks in different regions.
 *  - Create one VM in each of the virtual network.
 *  - Update all three databases to have firewall rules with range of each of the virtual network.
 */

public final class ManageSqlDatabasesAcrossDifferentDataCenters {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String sqlServerName = Utils.randomResourceName(azureResourceManager, "sqlserver", 20);
        final String rgName =  Utils.randomResourceName(azureResourceManager, "rgRSSDRE", 20);
        final String administratorLogin = "sqladmin3423";
        final String administratorPassword = Utils.password();
        final String slaveSqlServer1Name =  Utils.randomResourceName(azureResourceManager, "slave1sql", 20);
        final String slaveSqlServer2Name =  Utils.randomResourceName(azureResourceManager, "slave2sql", 20);
        final String databaseName = "mydatabase";
        final String networkPrefix =  "network";
        final String virtualMachinePrefix =  "samplevm";
        try {

            // ============================================================
            // Create a SQL Server, with 2 firewall rules.
            SqlServer masterSqlServer = azureResourceManager.sqlServers().define(sqlServerName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withAdministratorLogin(administratorLogin)
                    .withAdministratorPassword(administratorPassword)
                    .create();

            Utils.print(masterSqlServer);

            // ============================================================
            // Create a Database in master SQL server created above.
            System.out.println("Creating a database");

            SqlDatabase masterDatabase = masterSqlServer.databases().define(databaseName)
                    .withBasicEdition()
                    .create();
            Utils.print(masterDatabase);

            // ============================================================
            // Create secondary SQLServer/Database for the master database
            System.out.println("Creating server in secondary location for master SQL Server");

            SqlServer sqlServerInSecondaryLocation = azureResourceManager.sqlServers()
                    .define(slaveSqlServer1Name)
                        .withRegion(Region.US_EAST2)
                        .withExistingResourceGroup(rgName)
                        .withAdministratorLogin(administratorLogin)
                        .withAdministratorPassword(administratorPassword)
                        .create();
            Utils.print(sqlServerInSecondaryLocation);

            System.out.println("Creating database in slave SQL Server.");
            SqlDatabase secondaryDatabase = sqlServerInSecondaryLocation.databases().define(databaseName)
                    .withSourceDatabase(masterDatabase)
                    .withMode(CreateMode.ONLINE_SECONDARY)
                    .create();
            Utils.print(secondaryDatabase);

            // ============================================================
            // Create another slave SQLServer/Database for the master database
            System.out.println("Creating server in another location for master SQL Server");
            SqlServer sqlServerInEurope = azureResourceManager.sqlServers()
                    .define(slaveSqlServer2Name)
                        .withRegion(Region.US_SOUTH_CENTRAL)
                        .withExistingResourceGroup(rgName)
                        .withAdministratorLogin(administratorLogin)
                        .withAdministratorPassword(administratorPassword)
                        .create();
            Utils.print(sqlServerInEurope);

            System.out.println("Creating database in second slave SQL Server.");
            SqlDatabase secondaryDatabaseInEurope = sqlServerInEurope.databases().define(databaseName)
                    .withSourceDatabase(masterDatabase)
                    .withMode(CreateMode.ONLINE_SECONDARY)
                    .create();
            Utils.print(secondaryDatabaseInEurope);

            // ============================================================
            // Create Virtual Networks in different regions
            List<Region> regions = new ArrayList<>();

            regions.add(Region.US_EAST);
            regions.add(Region.US_WEST);
            regions.add(Region.EUROPE_NORTH);
            regions.add(Region.ASIA_SOUTHEAST);
            regions.add(Region.US_WEST2);

            List<Creatable<Network>> creatableNetworks = new ArrayList<>();

            System.out.println("Creating virtual networks in different regions.");

            for (Region region: regions) {
                creatableNetworks.add(azureResourceManager.networks().define(Utils.randomResourceName(azureResourceManager, networkPrefix, 20))
                        .withRegion(region)
                        .withExistingResourceGroup(rgName));
            }
            Collection<Network> networks = azureResourceManager.networks().create(creatableNetworks).values();

            // ============================================================
            // Create virtual machines attached to different virtual networks created above.
            List<Creatable<VirtualMachine>> creatableVirtualMachines = new ArrayList<>();
            System.out.println("Creating virtual machines in different regions.");

            for (Network network: networks) {
                String virtualMachineName = Utils.randomResourceName(azureResourceManager, virtualMachinePrefix, 20);
                Creatable<PublicIpAddress> publicIPAddressCreatable = azureResourceManager.publicIpAddresses().define(virtualMachineName)
                        .withRegion(network.region())
                        .withExistingResourceGroup(rgName)
                        .withLeafDomainLabel(virtualMachineName);
                creatableVirtualMachines.add(azureResourceManager.virtualMachines().define(virtualMachineName)
                        .withRegion(network.region())
                        .withExistingResourceGroup(rgName)
                        .withExistingPrimaryNetwork(network)
                        .withSubnet(network.subnets().values().iterator().next().name())
                        .withPrimaryPrivateIPAddressDynamic()
                        .withNewPrimaryPublicIPAddress(publicIPAddressCreatable)
                        .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                        .withAdminUsername(administratorLogin)
                        .withAdminPassword(administratorPassword)
                        .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4")));
            }

            HashMap<String, String> ipAddresses = new HashMap<>();
            for (VirtualMachine virtualMachine: azureResourceManager.virtualMachines().create(creatableVirtualMachines).values()) {
                ipAddresses.put(virtualMachine.name(), virtualMachine.getPrimaryPublicIPAddress().ipAddress());
            }

            System.out.println("Adding firewall rule for each of virtual network network");

            List<SqlServer> sqlServers = new ArrayList<>();
            sqlServers.add(sqlServerInSecondaryLocation);
            sqlServers.add(sqlServerInEurope);
            sqlServers.add(masterSqlServer);

            for (SqlServer sqlServer: sqlServers) {
                for (Map.Entry<String, String> ipAddress: ipAddresses.entrySet()) {
                    sqlServer.firewallRules().define(ipAddress.getKey()).withIpAddress(ipAddress.getValue()).create();
                }
            }

            for (SqlServer sqlServer: sqlServers) {
                System.out.println("Print firewall rules in Sql Server in " + sqlServer.regionName());

                List<SqlFirewallRule> firewallRules = sqlServer.firewallRules().list();
                for (SqlFirewallRule firewallRule: firewallRules) {
                    Utils.print(firewallRule);
                }
            }

            // Delete the SQL Server.
            System.out.println("Deleting all Sql Servers");
            for (SqlServer sqlServer: sqlServers) {
                azureResourceManager.sqlServers().deleteById(sqlServer.id());
            }
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

    private ManageSqlDatabasesAcrossDifferentDataCenters() {
    }
}
