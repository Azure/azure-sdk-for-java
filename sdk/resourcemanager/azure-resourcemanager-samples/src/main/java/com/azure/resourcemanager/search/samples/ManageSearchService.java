// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.search.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.search.models.AdminKeyKind;
import com.azure.resourcemanager.search.models.SearchService;
import com.azure.resourcemanager.search.models.SkuName;

import java.util.Date;

/**
 * Azure Cognitive Search sample for managing search service.
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
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgSearch", 15);
        final String searchServiceName = Utils.randomResourceName(azureResourceManager, "ssrv", 20);
        final Region region = Region.US_EAST;

        try {

            //=============================================================
            // Check if the name for the Azure Search service to be created is available

            if (!azureResourceManager.searchServices().checkNameAvailability(searchServiceName).isNameAvailable()) {
                return false;
            }
            Date t1, t2;

            // Azure limits the number of free Search service resource to one per subscription
            // List all Search services in the subscription and skip if there is already one resource of type free SKU
            boolean createFreeService = true;
            PagedIterable<SearchService> resources = azureResourceManager.searchServices().list();
            for (SearchService item : resources) {
                if (item.sku().name() == SkuName.FREE) {
                    createFreeService = false;
                    break;
                }
            }

            if (createFreeService) {
                //=============================================================
                // Create a Azure Search service resource with a "free" SKU

                System.out.println("Creating an Azure Search service using \"free\" SKU");

                t1 = new Date();

                SearchService searchServiceFree = azureResourceManager.searchServices().define(searchServiceName + "free")
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withFreeSku()
                    .create();

                t2 = new Date();
                System.out.println("Created Azure Search service: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + searchServiceFree.id());
                Utils.print(searchServiceFree);
            }

            //=============================================================
            // Create an Azure Search service resource

            System.out.println("Creating an Azure Search service");

            t1 = new Date();

            SearchService searchService = azureResourceManager.searchServices().define(searchServiceName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
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

            for (SearchService service : azureResourceManager.searchServices().listByResourceGroup(rgName)) {
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
                .withReplicaCount(2)
                .withPartitionCount(2)
                .apply();

            Utils.print(searchService);


            //=============================================================
            // Delete a query key for an Azure Search service resource

            System.out.println("Delete a query key for an Azure Search service");

            searchService.deleteQueryKey(searchService.listQueryKeys().iterator().next().key());

            Utils.print(searchService);


            //=============================================================
            // Delete the Search service resource

            System.out.println("Delete an Azure Search service resource");

            azureResourceManager.searchServices().deleteByResourceGroup(rgName, searchServiceName);

            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
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
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
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
