/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.cosmosdb.samples;


import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.CloudException;
import com.azure.management.ApplicationTokenCredential;
import com.azure.management.Azure;
import com.azure.management.cosmosdb.CosmosDBAccount;
import com.azure.management.cosmosdb.VirtualNetworkRule;
import com.azure.management.network.Network;
import com.azure.management.network.ServiceEndpointType;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.samples.Utils;

import java.io.File;
import java.util.List;

/**
 * Azure CosmosDB sample for using Virtual Network ACL rules.
 *  - Create a Virtual Network with two subnets.
 *  - Create an Azure Table CosmosDB account configured with a Virtual Network Rule
 *  - Add another virtual network rule in the CosmosDB account
 *  - List all virtual network rules.
 *  - Delete a virtual network.
 *  - Delete the CosmosDB.
 */
public class CreateCosmosDBTableWithVirtualNetworkRule {
    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @param clientId client id
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure, String clientId) {
        final String docDBName = azure.sdkContext().randomResourceName("cosmosdb", 15);
        final String rgName = azure.sdkContext().randomResourceName("rgcosmosdb", 24);
        final String vnetName = azure.sdkContext().randomResourceName("vnetcosmosdb", 20);

        try {
            // ============================================================
            // Create a virtual network with two subnets.
            System.out.println("Create a virtual network with two subnets: subnet1 and subnet2");

            Network virtualNetwork = azure.networks().define(vnetName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAddressSpace("192.168.0.0/16")
                .defineSubnet("subnet1")
                    .withAddressPrefix("192.168.1.0/24")
                    .withAccessFromService(ServiceEndpointType.MICROSOFT_AZURECOSMOSDB)
                    .attach()
                .defineSubnet("subnet2")
                    .withAddressPrefix("192.168.2.0/24")
                    .withAccessFromService(ServiceEndpointType.MICROSOFT_AZURECOSMOSDB)
                    .attach()
                .create();

            System.out.println("Created a virtual network");
            // Print the virtual network details
            Utils.print(virtualNetwork);


            //============================================================
            // Create a CosmosDB
            System.out.println("Creating a CosmosDB...");
            CosmosDBAccount cosmosDBAccount = azure.cosmosDBAccounts().define(docDBName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withDataModelAzureTable()
                .withEventualConsistency()
                .withWriteReplication(Region.US_WEST)
                .withVirtualNetwork(virtualNetwork.id(), "subnet1")
                .create();

            System.out.println("Created CosmosDB");
            Utils.print(cosmosDBAccount);


            // ============================================================
            // Get the virtual network rule created above.
            List<VirtualNetworkRule> vnetRules = cosmosDBAccount.virtualNetworkRules();

            System.out.println("CosmosDB Virtual Network Rules:");
            for (VirtualNetworkRule vnetRule : vnetRules) {
                System.out.println("\t" + vnetRule.getId());
            }


            // ============================================================
            // Add new virtual network rules.
            cosmosDBAccount.update()
                .withVirtualNetwork(virtualNetwork.id(), "subnet2")
                .apply();


            // ============================================================
            // List then remove all virtual network rules.
            System.out.println("Listing all virtual network rules in CosmosDB account.");

            vnetRules = cosmosDBAccount.virtualNetworkRules();

            System.out.println("CosmosDB Virtual Network Rules:");
            for (VirtualNetworkRule vnetRule : vnetRules) {
                System.out.println("\t" + vnetRule.getId());
            }

            cosmosDBAccount.update()
                .withVirtualNetworkRules(null)
                .apply();

            azure.networks().deleteById(virtualNetwork.id());


            //============================================================
            // Delete CosmosDB
            System.out.println("Deleting the CosmosDB");
            // work around CosmosDB service issue returning 404 CloudException on delete operation
            try {
                azure.cosmosDBAccounts().deleteById(cosmosDBAccount.id());
            } catch (CloudException e) {
            }
            System.out.println("Deleted the CosmosDB");

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                System.out.println("Deleting resource group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted resource group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
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

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
                .authenticate(credFile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure, ApplicationTokenCredential.fromFile(credFile).getClientId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
