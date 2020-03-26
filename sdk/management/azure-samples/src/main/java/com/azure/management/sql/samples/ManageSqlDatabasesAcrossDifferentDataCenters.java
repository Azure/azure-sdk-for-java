/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql.samples;


import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.ApplicationTokenCredential;
import com.azure.management.Azure;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.azure.management.compute.VirtualMachine;
import com.azure.management.compute.VirtualMachineSizeTypes;
import com.azure.management.network.Network;
import com.azure.management.network.PublicIPAddress;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.samples.Utils;
import com.azure.management.sql.CreateMode;
import com.azure.management.sql.DatabaseEdition;
import com.azure.management.sql.SqlDatabase;
import com.azure.management.sql.SqlFirewallRule;
import com.azure.management.sql.SqlServer;

import java.io.File;
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
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String sqlServerName = azure.sdkContext().randomResourceName("sqlserver", 20);
        final String rgName =  azure.sdkContext().randomResourceName("rgRSSDRE", 20);
        final String administratorLogin = "sqladmin3423";
        // [SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine", Justification="Serves as an example, not for deployment. Please change when using this in your code.")]
        final String administratorPassword = "myS3cureP@ssword";
        final String slaveSqlServer1Name =  azure.sdkContext().randomResourceName("slave1sql", 20);
        final String slaveSqlServer2Name =  azure.sdkContext().randomResourceName("slave2sql", 20);
        final String databaseName = "mydatabase";
        final String networkName =  azure.sdkContext().randomResourceName("network", 20);
        final String virtualMachineName =  azure.sdkContext().randomResourceName("samplevm", 20);
        try {

            // ============================================================
            // Create a SQL Server, with 2 firewall rules.
            SqlServer masterSqlServer = azure.sqlServers().define(sqlServerName)
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
                    .withEdition(DatabaseEdition.BASIC)
                    .create();
            Utils.print(masterDatabase);

            // ============================================================
            // Create secondary SQLServer/Database for the master database
            System.out.println("Creating server in secondary location for master SQL Server");

            SqlServer sqlServerInSecondaryLocation = azure.sqlServers()
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
            SqlServer sqlServerInEurope = azure.sqlServers()
                    .define(slaveSqlServer2Name)
                        .withRegion(Region.EUROPE_WEST)
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
            regions.add(Region.JAPAN_EAST);

            List<Creatable<Network>> creatableNetworks = new ArrayList<>();

            System.out.println("Creating virtual networks in different regions.");

            for (Region region: regions) {
                creatableNetworks.add(azure.networks().define(networkName)
                        .withRegion(region)
                        .withExistingResourceGroup(rgName));
            }
            Collection<Network> networks = azure.networks().create(creatableNetworks).values();

            // ============================================================
            // Create virtual machines attached to different virtual networks created above.
            List<Creatable<VirtualMachine>> creatableVirtualMachines = new ArrayList<>();
            System.out.println("Creating virtual machines in different regions.");

            for (Network network: networks) {
                Creatable<PublicIPAddress> publicIPAddressCreatable = azure.publicIPAddresses().define(virtualMachineName)
                        .withRegion(network.region())
                        .withExistingResourceGroup(rgName)
                        .withLeafDomainLabel(virtualMachineName);
                creatableVirtualMachines.add(azure.virtualMachines().define(virtualMachineName)
                        .withRegion(network.region())
                        .withExistingResourceGroup(rgName)
                        .withExistingPrimaryNetwork(network)
                        .withSubnet(network.subnets().values().iterator().next().name())
                        .withPrimaryPrivateIPAddressDynamic()
                        .withNewPrimaryPublicIPAddress(publicIPAddressCreatable)
                        .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                        .withAdminUsername(administratorLogin)
                        .withAdminPassword(administratorPassword)
                        .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2));
            }

            HashMap<String, String> ipAddresses = new HashMap<>();
            for (VirtualMachine virtualMachine: azure.virtualMachines().create(creatableVirtualMachines).values()) {
                ipAddresses.put(virtualMachine.name(), virtualMachine.getPrimaryPublicIPAddress().ipAddress());
            }

            System.out.println("Adding firewall rule for each of virtual network network");

            List<SqlServer> sqlServers = new ArrayList<>();
            sqlServers.add(sqlServerInSecondaryLocation);
            sqlServers.add(sqlServerInEurope);
            sqlServers.add(masterSqlServer);

            for (SqlServer sqlServer: sqlServers) {
                for (Map.Entry<String, String> ipAddress: ipAddresses.entrySet()) {
                    sqlServer.firewallRules().define(ipAddress.getKey()).withIPAddress(ipAddress.getValue()).create();
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
                azure.sqlServers().deleteById(sqlServer.id());
            }
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

            ApplicationTokenCredential credentials = ApplicationTokenCredential.fromFile(credFile);
            RestClient restClient = new RestClientBuilder()
                    .withBaseUrl(AzureEnvironment.AZURE, AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                    .withSerializerAdapter(new AzureJacksonAdapter())
//                .withReadTimeout(150, TimeUnit.SECONDS)
                    .withHttpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY))
                    .withCredential(credentials).buildClient();
            Azure azure = Azure.authenticate(restClient, credentials.getDomain(), credentials.getDefaultSubscriptionId()).withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
       } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageSqlDatabasesAcrossDifferentDataCenters() {
    }
}