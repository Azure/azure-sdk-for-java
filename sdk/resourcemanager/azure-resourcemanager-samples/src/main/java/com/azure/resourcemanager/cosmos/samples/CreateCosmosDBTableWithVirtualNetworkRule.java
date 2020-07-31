// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cosmos.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementException;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.VirtualNetworkRule;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.ServiceEndpointType;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

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
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
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
                System.out.println("\t" + vnetRule.id());
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
                System.out.println("\t" + vnetRule.id());
            }

            cosmosDBAccount.update()
                .withVirtualNetworkRules(null)
                .apply();

            azure.networks().deleteById(virtualNetwork.id());


            //============================================================
            // Delete CosmosDB
            System.out.println("Deleting the CosmosDB");
            // work around CosmosDB service issue returning 404 ManagementException on delete operation
            try {
                azure.cosmosDBAccounts().deleteById(cosmosDBAccount.id());
            } catch (ManagementException e) {
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
}
