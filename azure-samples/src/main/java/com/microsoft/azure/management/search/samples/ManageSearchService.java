/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.search.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.search.AdminKeyKind;
import com.microsoft.azure.management.search.SearchService;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.Date;

/**
 * Azure Search sample for managing search service.
 *  - Create a Search service resource with a free SKU
 *  - Create a Search service resource with a standard SKU, one replica and one partition
 *  - Create a new query key and delete a query key
 *  - Update the Search service with three replicas and three partitions
 *  - Regenerate the primary and secondary admin keys
 *  - Delete the Search service
 */
public class ManageSearchService {
    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = SdkContext.randomResourceName("rgSearch", 15);
        final String searchServiceName = SdkContext.randomResourceName("search", 20);
        final Region region = Region.US_EAST;

        try {
            
            //=============================================================
            // Check if the name for the Azure Search service to be created is available

            if (!azure.searchServices().checkNameAvailability(searchServiceName).isAvailable()) {
                return false;
            }


            //=============================================================
            // Create a Azure Search service resource with a "free" SKU

            System.out.println("Creating an Azure Search service using \"free\" SKU");

            Date t1 = new Date();

            SearchService searchServiceFree = azure.searchServices().define(searchServiceName + "free")
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withFreeSku()
                .create();

            Date t2 = new Date();
            System.out.println("Created Azure Search service: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + searchServiceFree.id());
            Utils.print(searchServiceFree);


            //=============================================================
            // Create an Azure Search service resource

            System.out.println("Creating an Azure Search service");

            t1 = new Date();

            SearchService searchService = azure.searchServices().define(searchServiceName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withStandardSku()
                    .withPartitionCount(1)
                    .withReplicaCount(1)
                    .create();

            t2 = new Date();
            System.out.println("Created Azure Search service: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + searchService.id());
            Utils.print(searchService);


            //=============================================================
            // Iterate through the Azure Search service resources

            System.out.println("List all the Azure Search services for a given resource group");

            for (SearchService service : azure.searchServices().listByResourceGroup(rgName)) {
              Utils.print(service);
            }


            //=============================================================
            // Add a query key for the Search service resource

            System.out.println("Add a query key to an Azure Search service");

            searchService.createQueryKey("testKey1");


            //=============================================================
            // Regenerate the admin keys for an Azure Search service resource

            System.out.println("Regenerate the admin keys for an Azure Search service");

            searchService.regenerateAdminKeys(AdminKeyKind.PRIMARY);
            searchService.regenerateAdminKeys(AdminKeyKind.SECONDARY);


            //=============================================================
            // Update the Search service to use three replicas and three partitions and update the tags

            System.out.println("Update an Azure Search service");

            searchService = searchService.update()
                    .withTag("tag2", "value2")
                    .withTag("tag3", "value3")
                    .withoutTag("tag1")
                    .withReplicaCount(3)
                    .withPartitionCount(3)
                    .apply();

            Utils.print(searchService);


            //=============================================================
            // Delete a query key for an Azure Search service resource

            System.out.println("Delete a query key for an Azure Search service");

            searchService.deleteQueryKey(searchService.listQueryKeys().get(1).key());

            Utils.print(searchService);


            //=============================================================
            // Delete the Search service resource

            System.out.println("Delete an Azure Search service resource");

            azure.searchServices().deleteByResourceGroup(rgName, searchServiceName);

            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
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
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BODY)
                    .authenticate(credFile)
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
