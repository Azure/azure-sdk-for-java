// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.search.service.SearchServiceClient;
import com.azure.search.service.customization.SearchCredentials;
import com.azure.search.service.implementation.SearchServiceClientImpl;
import com.azure.search.service.models.DataType;
import com.azure.search.service.models.Field;
import com.azure.search.service.models.Index;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.search.SearchService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

public class CreateSearchServiceAndIndexExample {

    /**
     * This is an example of using {@link SearchService} to create Search resource in Azure,
     * and {@link SearchServiceClient} to create Index in Azure Search.
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        // Required user input:
        String servicePrincipalAppId = "<service principal app id>";
        String domainId = "<domain id>";
        String servicePrincipalAppSecret = "<service principal app secret>";
        String resourceGroupName = "<resource group name>";
        String searchServiceName = "<search service name>";
        String subscriptionId = "<subscription id>";
        Region region = Region.US_EAST;

        // Creating Azure Search Resource:
        ApplicationTokenCredentials applicationTokenCredentials = new ApplicationTokenCredentials(
            servicePrincipalAppId,
            domainId,
            servicePrincipalAppSecret,
            AzureEnvironment.AZURE);

        // Create resource group
        Azure azure = Azure.configure()
            .authenticate(applicationTokenCredentials)
            .withSubscription(subscriptionId);
        System.out.println("Creating Resource Group: " + resourceGroupName);
        ResourceGroup resourceGroup = azure.resourceGroups()
            .define(resourceGroupName)
            .withRegion(region)
            .create();

        // Create search service
        System.out.println("Creating Search Service: " + searchServiceName);
        SearchService searchService = azure.searchServices()
            .define(searchServiceName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withFreeSku()
            .create();
        String adminKey = searchService.getAdminKeys().primaryKey();
        System.out.println("Admin key: " + adminKey);

        // Create index
        String indexName = "hotels";
        System.out.println("Creating Index: " + indexName);
        SearchCredentials searchCredentials = new SearchCredentials(adminKey);
        SearchServiceClient searchServiceClient = new SearchServiceClientImpl(searchCredentials)
            .withSearchServiceName(searchServiceName);
        Index index = new Index()
            .withName(indexName)
            .withFields(Arrays.asList(
                new Field()
                    .withName("HotelId")
                    .withType(DataType.EDM_STRING)
                    .withKey(Boolean.TRUE)
                    .withFilterable(Boolean.TRUE)
                    .withSortable(Boolean.TRUE)
                    .withFacetable(Boolean.TRUE),
                new Field()
                    .withName("Tags")
                    .withType(DataType.EDM_STRING_COLLECTION)
                    .withSearchable(Boolean.TRUE)
                    .withFilterable(Boolean.TRUE)
                    .withFacetable(Boolean.TRUE)
                )
            );
        searchServiceClient.indexes().create(index);

        /*
        // Alternatively, create index from json file
        try {
            Reader indexData = new InputStreamReader(CreateSearchServiceAndIndexExample.class.getResourceAsStream("HotelsIndex.json"));
            Index index = new ObjectMapper().readValue(indexData, Index.class);
            searchServiceClient.indexes().create(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }
}
